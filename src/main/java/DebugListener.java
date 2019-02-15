import com.intellij.debugger.engine.DebuggerManagerThreadImpl;
import com.intellij.debugger.engine.SuspendContextImpl;
import com.intellij.debugger.impl.DebuggerContextImpl;
import com.intellij.debugger.impl.DebuggerContextListener;
import com.intellij.debugger.impl.DebuggerSession;
import com.intellij.debugger.jdi.StackFrameProxyImpl;
import org.jetbrains.annotations.NotNull;

/**
 * Listens for when the debugger session is paused (i.e. it is at a breakpoint)
 */
public class DebugListener implements DebuggerContextListener {

    private DebuggerSession debuggerSession;

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
            if (event == DebuggerSession.Event.PAUSE
                    || event == DebuggerSession.Event.CONTEXT
                    || event == DebuggerSession.Event.REFRESH
                    || event == DebuggerSession.Event.REFRESH_WITH_STACK
                    && debuggerSession.isPaused()) {
                final SuspendContextImpl newSuspendContext = newContext.getSuspendContext();
                final StackFrameProxyImpl sfProxy = newContext.getFrameProxy();

                if (newSuspendContext != null) {
                    DebugExtractor extractor = new DebugExtractor(sfProxy);
                    DebuggerManagerThreadImpl managerThread = newSuspendContext.getDebugProcess()
                            .getManagerThread();
                    managerThread.invokeCommand(extractor);
                }
            }
    }
}
