import com.intellij.execution.Executor;
import com.intellij.execution.ExecutorRegistry;
import com.intellij.execution.executors.DefaultDebugExecutor;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class FootPrint_Executor extends DefaultDebugExecutor{
    @NonNls
    public static final String EXECUTOR_ID = "EXECUTOR_FOOTPRINT";

    public static Executor getMyExecutorInstance() {
        return ExecutorRegistry.getInstance().getExecutorById(EXECUTOR_ID);
    }

    @Override
    public String getContextActionId() {
        return "FootPrintDebugClass";
    }

    @Override
    @NotNull
    public String getId() {
        return EXECUTOR_ID;
    }
}
