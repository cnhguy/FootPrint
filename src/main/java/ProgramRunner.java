import com.intellij.debugger.DebugEnvironment;
import com.intellij.debugger.DebuggerManagerEx;
import com.intellij.debugger.DefaultDebugEnvironment;
import com.intellij.debugger.engine.*;
import com.intellij.debugger.impl.DebuggerSession;
import com.intellij.debugger.impl.GenericDebuggerRunner;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.*;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.xdebugger.XDebuggerManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public class ProgramRunner extends GenericDebuggerRunner {

    public ProgramRunner() {
        super();
    }

    public static final String RUNNER_ID = "MyDebugRunner";

    @Override
    @NotNull
    public String getRunnerId() {
        return RUNNER_ID;
    }

    @Override
    public boolean canRun(@NotNull String executorId, @NotNull RunProfile profile) {
        boolean r =  executorId.equals(FootPrint_Executor.EXECUTOR_ID) && profile instanceof ModuleRunProfile
                && !(profile instanceof RunConfigurationWithSuppressedDefaultDebugAction);
        return r;
    }

    @Nullable
    @Override
    protected RunContentDescriptor attachVirtualMachine(RunProfileState state,
                                                        @NotNull ExecutionEnvironment env,
                                                        RemoteConnection connection,
                                                        long pollTimeout) throws ExecutionException {
        DebugEnvironment environment = new DefaultDebugEnvironment(env, state, connection, pollTimeout);
        final DebuggerSession debuggerSession = DebuggerManagerEx.getInstanceEx(env.getProject()).attachVirtualMachine(environment);

        if (debuggerSession == null) {
            return null;
        }

        DebugListener debugListener = new DebugListener(debuggerSession);

        debuggerSession.getContextManager().addListener(debugListener);

        final DebugProcessImpl debugProcess = debuggerSession.getProcess();
        DebugProcessStarter debugProcessStarter = new DebugProcessStarter(debugProcess, debuggerSession);

        return XDebuggerManager.getInstance(env.getProject())
                .startSession(env, debugProcessStarter)
                .getRunContentDescriptor();
    }
}
