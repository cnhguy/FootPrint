import com.intellij.debugger.DebugEnvironment;
import com.intellij.debugger.DebuggerManagerEx;
import com.intellij.debugger.DefaultDebugEnvironment;
import com.intellij.debugger.engine.*;
import com.intellij.debugger.engine.managerThread.DebuggerCommand;
import com.intellij.debugger.engine.requests.RequestManagerImpl;
import com.intellij.debugger.impl.DebuggerSession;
import com.intellij.debugger.impl.GenericDebuggerRunner;
import com.intellij.debugger.jdi.VirtualMachineProxyImpl;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.*;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.xdebugger.XDebuggerManager;
import com.sun.jdi.StackFrame;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.request.ClassPrepareRequest;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.EventRequestManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.PrintWriter;

/**
 * Custom ProgramRunner in conjunction with FootPrint_Executor. Registers listeners in the VM for certain events.
 */
public class ProgramRunner extends GenericDebuggerRunner {

    public ProgramRunner() {
        super();
    }

    public static final String RUNNER_ID = "MyDebugRunner";

    private String[] excludes = {"java.*", "javax.*", "sun.*", "com.sun.*"};

    @Override
    @NotNull
    public String getRunnerId() {
        return RUNNER_ID;
    }

    /**
     * Returns true iff the executor is the FootPrint_Executor
     * @param executorId
     * @param profile
     * @return
     */
    @Override
    public boolean canRun(@NotNull String executorId, @NotNull RunProfile profile) {
        boolean r =  executorId.equals(FootPrint_Executor.EXECUTOR_ID) && profile instanceof ModuleRunProfile
                && !(profile instanceof RunConfigurationWithSuppressedDefaultDebugAction);
        return r;
    }

    /**
     * Registers the listeners, among other Intellij actions.
     * @param state
     * @param env
     * @param connection
     * @param pollTimeout
     * @return
     * @throws ExecutionException
     */
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

        DebugListener debugListener = new DebugListener(debuggerSession, excludes);
        debuggerSession.getContextManager().addListener(debugListener);

        final DebugProcessImpl debugProcess = debuggerSession.getProcess();
        debugProcess.addDebugProcessListener(debugListener);

        DebugProcessStarter debugProcessStarter = new DebugProcessStarter(debugProcess, debuggerSession);

        return XDebuggerManager.getInstance(env.getProject())
                .startSession(env, debugProcessStarter)
                .getRunContentDescriptor();
    }


}
