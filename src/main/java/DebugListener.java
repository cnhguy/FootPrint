import com.intellij.debugger.InstanceFilter;
import com.intellij.debugger.engine.DebugProcess;
import com.intellij.debugger.engine.DebugProcessListener;
import com.intellij.debugger.engine.DebuggerManagerThreadImpl;
import com.intellij.debugger.engine.SuspendContextImpl;
import com.intellij.debugger.engine.events.SuspendContextCommandImpl;
import com.intellij.debugger.engine.managerThread.DebuggerCommand;
import com.intellij.debugger.engine.requests.RequestManagerImpl;
import com.intellij.debugger.impl.DebuggerContextImpl;
import com.intellij.debugger.impl.DebuggerContextListener;
import com.intellij.debugger.impl.DebuggerSession;
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

import java.util.List;

/**
 * Listens for when the debugger session is paused (i.e. it is at a breakpoint)
 */
public class DebugListener implements DebuggerContextListener, DebugProcessListener, ClassPrepareRequestor, FilteredRequestor {

    private DebuggerSession debuggerSession;
    private VirtualMachineProxyImpl vmp;
    private String[] excludes;

    /**
     * Creates a DebugListener
     * @param debuggerSession
     * @param excludes
     */
    public DebugListener(DebuggerSession debuggerSession, String[] excludes) {
        this.debuggerSession = debuggerSession;
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
            DebugCache.getInstance().clear();
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
//                try {
//                    while(thread.frameCount() == 0) {
//                        thread.resume();
//                        thread.suspend();
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    return;
//                }
                vmp = (VirtualMachineProxyImpl) proc.getVirtualMachineProxy();
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
        registerModificationWatchPointRequests(debuggerProcess, referenceType);
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
            req.setSuspendPolicy(EventRequest.SUSPEND_NONE);
            req.enable();
        }
    }

    private void registerModificationWatchPointRequests(DebugProcess debuggerProcess, ReferenceType referenceType) {
        List<Field> fields = referenceType.visibleFields();
        RequestManagerImpl requestManager = (RequestManagerImpl)debuggerProcess.getRequestsManager();
        for (Field field : fields) {
            System.out.println(field);
            ModificationWatchpointRequest req =
                    requestManager.createModificationWatchpointRequest(this, field);
            for (int i=0; i<excludes.length; ++i) {
                req.addClassExclusionFilter(excludes[i]);
            }
            req.setSuspendPolicy(EventRequest.SUSPEND_NONE);
            req.enable();
        }
    }


    private ThreadReference thread;
    /**
     * Callback for Locatable events. If they are ModificationWatchPoint events, then creates an extractor to get the
     * field's value.
     * @param action
     * @param event
     * @return
     * @throws EventProcessingException
     */
    @Override
    public boolean processLocatableEvent(SuspendContextCommandImpl action, LocatableEvent event) throws EventProcessingException {
        System.out.println("\nprocess LocatableEvent suspended: " + event.thread().isSuspended());
        if (event instanceof ModificationWatchpointEvent) {
            DebugExtractor extractor = new DebugExtractor();
            extractor.processModificationWatchPointEvent((ModificationWatchpointEvent) event);
        } else if (event instanceof BreakpointEvent) {
            BreakpointEvent breakpointEvent = (BreakpointEvent)event;
            System.out.println("BreakPointEvent line: " + breakpointEvent.location().lineNumber());
//            final SuspendContextImpl newSuspendContext = action.getSuspendContext();
//            final StackFrameProxyImpl sfProxy = action.getSuspendContext().getFrameProxy();
//
//            if (newSuspendContext != null && event.thread().isAtBreakpoint()) {
//                System.out.println("extractor run");
//                DebugProcess process = debuggerSession.getProcess();
//                DebugExtractor extractor = new DebugExtractor(sfProxy, process, this);
//                DebuggerManagerThreadImpl managerThread = newSuspendContext.getDebugProcess()
//                        .getManagerThread();
//                managerThread.invokeCommand(extractor);
//            }
            try {
                ThreadReference thread = event.thread();
//                for (StackFrame frame : thread.frames()) {
//                    System.out.println(frame.visibleVariables());
//                }
                StackFrame stackFrame = thread.frame(0); //current stack frame
                DebugCache cache = DebugCache.getInstance();
                for (LocalVariable var : stackFrame.visibleVariables()) {
                    Value val = stackFrame.getValue(var);
                    if (val instanceof PrimitiveValue) {
                        if (val instanceof IntegerValue) {
                            IntegerValue integerValue = (IntegerValue) val;
                            int intval = integerValue.value();
                            System.out.println(breakpointEvent.location().lineNumber() + "\t" + var + ": " + intval);
                            cache.put(var.name(), breakpointEvent.location().lineNumber(), String.valueOf(intval));
                        }
                    } else {
                        //do something
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
//        if (event.thread().isSuspended())
//            event.thread().resume();
        return true;
    }

    /**
     * Returns the suspend policy SUSPEND_NONE
     * @return
     */
    @Override
    public String getSuspendPolicy() {
        return "SUSPEND_NONE";
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
