import com.intellij.debugger.InstanceFilter;
import com.intellij.debugger.engine.*;
import com.intellij.debugger.engine.evaluation.EvaluationContextImpl;
import com.intellij.debugger.engine.events.SuspendContextCommandImpl;
import com.intellij.debugger.engine.managerThread.DebuggerCommand;
import com.intellij.debugger.engine.requests.RequestManagerImpl;
import com.intellij.debugger.impl.DebuggerContextImpl;
import com.intellij.debugger.impl.DebuggerContextListener;
import com.intellij.debugger.impl.DebuggerSession;
import com.intellij.debugger.impl.PrioritizedTask;
import com.intellij.debugger.jdi.StackFrameProxyImpl;
import com.intellij.debugger.jdi.VirtualMachineProxyImpl;
import com.intellij.debugger.requests.ClassPrepareRequestor;
import com.intellij.debugger.ui.breakpoints.FilteredRequestor;
import com.intellij.ui.classFilter.ClassFilter;
import com.sun.jdi.*;
import com.sun.jdi.event.BreakpointEvent;
import com.sun.jdi.event.LocatableEvent;
import com.sun.jdi.event.ModificationWatchpointEvent;
import com.sun.jdi.request.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Listens for when the debugger session is paused (i.e. it is at a breakpoint)
 */
public class DebugListener implements DebuggerContextListener, DebugProcessListener, ClassPrepareRequestor, FilteredRequestor {

    private String[] excludes;

    /**
     * Creates a DebugListener
     * @param excludes classes matching in excludes are excluded from being tracked
     */
    public DebugListener(String[] excludes) {
        this.excludes = excludes;
    }

    /**
     * If the debugger is paused, tells the manager thread to run the extractor
     * @param newContext
     * @param event
     */
    @Override
    public void changeEvent(@NotNull DebuggerContextImpl newContext, DebuggerSession.Event event) {
        System.out.println(event.toString());
        // new debug started, clear the cache
        if (event == DebuggerSession.Event.ATTACHED) {
            MasterCache.getInstance().clear();
            FootPrintToolWindow.getInstance().reset();
        }

//        if (event == DebuggerSession.Event.PAUSE
//                || event == DebuggerSession.Event.CONTEXT
//                || event == DebuggerSession.Event.REFRESH
//                || event == DebuggerSession.Event.REFRESH_WITH_STACK
//                && debuggerSession.isPaused()) {
//            final SuspendContextImpl newSuspendContext = newContext.getSuspendContext();
//            final StackFrameProxyImpl sfProxy = newContext.getFrameProxy();
//
//            if (newSuspendContext != null) {
//                DebugProcess process = debuggerSession.getProcess();
//                DebugExtractor extractor = new DebugExtractor(sfProxy, process);
//                DebuggerManagerThreadImpl managerThread = newSuspendContext.getDebugProcess()
//                        .getManagerThread();
//                managerThread.invokeCommand(extractor);
//            }
//        }
    }

    /**
     * Creates a class prepare request and registers it with the virtual machine.
     * @param vmp
     */
    private void setClassPrepareRequest(VirtualMachineProxyImpl vmp) {
        EventRequestManager mgr = vmp.eventRequestManager();

        ClassPrepareRequest cpr = mgr.createClassPrepareRequest();
        for (int i=0; i<excludes.length; ++i) {
            cpr.addClassExclusionFilter(excludes[i]);
        }
        cpr.setSuspendPolicy(EventRequest.SUSPEND_ALL);
        cpr.enable();

        ((RequestManagerImpl)vmp.getDebugProcess().getRequestsManager()).registerRequestInternal
                (this, cpr);
    }

    /**
     * Callback for thread started events. Registers class prepare requests with the virtual machine.
     * @param proc
     * @param thread
     */
    @Override
    public void threadStarted(@NotNull DebugProcess proc, ThreadReference thread) {
        proc.getManagerThread().invokeCommand(new DebuggerCommand() {
            @Override
            public void action() {
                thread.suspend();
                VirtualMachineProxyImpl vmp = (VirtualMachineProxyImpl) proc.getVirtualMachineProxy();
                System.out.println("threadstarted vm: " + vmp);
                setClassPrepareRequest(vmp);
                thread.resume();
            }

            @Override
            public void commandCancelled() {}
        });
    }

    /**
     * Callback for ClassPrepare events. Registers ModificationWatchPoint requests for all fields in the class.
     * @param debuggerProcess
     * @param referenceType
     */
    @Override
    public void processClassPrepare(DebugProcess debuggerProcess, ReferenceType referenceType) {
        System.out.println("process ClassPrepare");
        registerBreakPointRequests(debuggerProcess, referenceType);
    }

    //sets a breakpoint on every line of executable source code
    private void registerBreakPointRequests(DebugProcess debuggerProcess, ReferenceType referenceType) {
        List<Location> executableLines;
        try {
            executableLines = referenceType.allLineLocations();
        } catch (AbsentInformationException exc) {
            exc.printStackTrace();
            return;
        }
        RequestManagerImpl requestManager = (RequestManagerImpl)debuggerProcess.getRequestsManager();
        for (Location loc : executableLines) {
            BreakpointRequest req = requestManager.createBreakpointRequest(this, loc);
            req.setSuspendPolicy(EventRequest.SUSPEND_ALL);
            req.enable();
        }
    }


    // used to keep track of all places where StepRequests where initiated and their requests
    private List<StepInfo> steps = new ArrayList<>();
    private Location prevLocation = null;

    /**
     * Callback for Locatable events.
     * @param action
     * @param event
     * @return
     * @throws EventProcessingException
     */
    @Override
    public boolean processLocatableEvent(SuspendContextCommandImpl action, LocatableEvent event) throws EventProcessingException {
//        System.out.println("\nprocess LocatableEvent suspended: " + event.thread().isSuspended());
        if (event instanceof BreakpointEvent) {
            BreakpointEvent breakpointEvent = (BreakpointEvent)event;
            System.out.println("\nBreakPointEvent line: " + breakpointEvent.location().lineNumber());

            final SuspendContextImpl suspendContext = action.getSuspendContext();
            final StackFrameProxyImpl sfProxy = action.getSuspendContext().getFrameProxy();

            if (suspendContext != null && event.thread().isAtBreakpoint()) {
//                System.out.println("extractor run");
                DebugExtractor extractor = new DebugExtractor(sfProxy, suspendContext, steps);
                DebuggerManagerThreadImpl managerThread = suspendContext.getDebugProcess()
                        .getManagerThread();
                managerThread.invokeCommand(extractor);
            }
            try {
                prevLocation = sfProxy.location();
            } catch (Exception e) {
//                e.printStackTrace();
            }
        }
        return true;
    }

    /**
     * Ccallback for when a thread is resumed, after suspension. e.g. after a breakpoint is resumed
     * @param suspendContext
     */
    @Override
    public void resumed(SuspendContext suspendContext) {
        System.out.println("DEBUG PROCESS RESUMED");
        SuspendContextImpl suspendContext1 = (SuspendContextImpl) suspendContext;
        EventRequestManager eventRequestManager =
                suspendContext1.getDebugProcess().getVirtualMachineProxy().eventRequestManager();
        // check if we have step requests. If so, track them
        for (StepRequest stepRequest : eventRequestManager.stepRequests()) {
            System.out.println("StepRequest: size: " + stepRequest.size() + " depth: " + stepRequest.depth());
            steps.add(new StepInfo(prevLocation, stepRequest));
        }
    }

    /**
     * Holds information about a step request including the location it was created at and the
     * request itself.
     */
    public class StepInfo {
        public final Location originalLocation;
        public final StepRequest stepRequest;

        public StepInfo(Location originalLocation, StepRequest stepRequest) {
            this.originalLocation = originalLocation;
            this.stepRequest = stepRequest;
        }
    }

    /**
     * Returns the suspend policy SUSPEND_ALL
     * @return
     */
    @Override
    public String getSuspendPolicy() {
        return "SUSPEND_ALL";
    }

    /* The following methods are unused, their values are set when the reqests are created. However, they are required
     * because of the FilteredRequestor interface.
     */

    @Override
    public boolean isInstanceFiltersEnabled() {
        return false;
    }

    @Override
    public InstanceFilter[] getInstanceFilters() {
        return new InstanceFilter[0];
    }

    @Override
    public boolean isCountFilterEnabled() {
        return false;
    }

    @Override
    public int getCountFilter() {
        return 0;
    }

    @Override
    public boolean isClassFiltersEnabled() {
        return false;
    }

    @Override
    public ClassFilter[] getClassFilters() {
        return new ClassFilter[0];
    }

    @Override
    public ClassFilter[] getClassExclusionFilters() {
        return new ClassFilter[0];
    }

    @Override
    public boolean isConditionEnabled() {
        return false;
    }
}
