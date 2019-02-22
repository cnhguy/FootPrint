import com.intellij.debugger.engine.DebugProcessImpl;
import com.intellij.debugger.engine.JavaDebugProcess;
import com.intellij.debugger.impl.DebuggerSession;
import com.intellij.execution.DefaultExecutionResult;
import com.intellij.execution.ExecutionResult;
import com.intellij.xdebugger.XDebugProcess;
import com.intellij.xdebugger.XDebugProcessStarter;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.impl.XDebugSessionImpl;
import org.jetbrains.annotations.NotNull;

/**
 * Custom debug process starter.
 */
public class DebugProcessStarter extends XDebugProcessStarter {

    private DebugProcessImpl debugProcess;
    private DebuggerSession debuggerSession;

    public DebugProcessStarter(DebugProcessImpl debugProcess, DebuggerSession debuggerSession) {
        this.debugProcess = debugProcess;
        this.debuggerSession = debuggerSession;
    }

    /**
     * Returns a Java debugger process
     * @param session the current debugging session
     * @return Java debugger process
     */
    @Override
    @NotNull
    public XDebugProcess start(@NotNull XDebugSession session) {
        XDebugSessionImpl sessionImpl = (XDebugSessionImpl)session;
        ExecutionResult executionResult = debugProcess.getExecutionResult();
        sessionImpl.addExtraActions(executionResult.getActions());
        if (executionResult instanceof DefaultExecutionResult) {
            sessionImpl.addRestartActions(((DefaultExecutionResult)executionResult).getRestartActions());
        }
        return JavaDebugProcess.create(session, debuggerSession);
    }
}
