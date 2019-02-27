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
import com.sun.jdi.event.LocatableEvent;
import com.sun.jdi.event.ModificationWatchpointEvent;
import com.sun.jdi.request.ClassPrepareRequest;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.EventRequestManager;
import com.sun.jdi.request.ModificationWatchpointRequest;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Listens for when the debugger session is paused (i.e. it is at a breakpoint)
 */
public class DebugListener implements DebuggerContextListener, DebugProcessListener {

    private DebuggerSession debuggerSession;

    /**
     * Creates a DebugListener
     * @param debuggerSession
     */
    public DebugListener(DebuggerSession debuggerSession) {
        this.debuggerSession = debuggerSession;
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
        if (event == DebuggerSession.Event.PAUSE
                || event == DebuggerSession.Event.CONTEXT
                || event == DebuggerSession.Event.REFRESH
                || event == DebuggerSession.Event.REFRESH_WITH_STACK
                && debuggerSession.isPaused()) {
            final SuspendContextImpl newSuspendContext = newContext.getSuspendContext();
            final StackFrameProxyImpl sfProxy = newContext.getFrameProxy();

            if (newSuspendContext != null) {
                DebugProcess process = debuggerSession.getProcess();
                DebugExtractor extractor = new DebugExtractor(sfProxy, process);
                DebuggerManagerThreadImpl managerThread = newSuspendContext.getDebugProcess()
                        .getManagerThread();
                managerThread.invokeCommand(extractor);
            }
        }
    }
}
