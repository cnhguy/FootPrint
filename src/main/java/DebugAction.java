import com.intellij.execution.*;
import com.intellij.execution.impl.RunManagerImpl;
import com.intellij.execution.runners.ExecutionEnvironmentBuilder;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

public class DebugAction extends AnAction{

    public DebugAction() {
        super("","start FootPrint", AllIcons.General.IjLogo);
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        RunnerAndConfigurationSettings config = RunManagerImpl.getInstanceEx(e.getProject())
                .getSelectedConfiguration();
        if (config != null) {
            System.out.println(config.getName()); // prints out the method's name

            this.executeConfiguration(config, FootPrint_Executor.getMyExecutorInstance());
        }
    }

    private static void executeConfiguration(@NotNull RunnerAndConfigurationSettings configuration,
                                             @NotNull Executor executor) {
        ExecutionEnvironmentBuilder builder;
        try {
            builder = ExecutionEnvironmentBuilder.create(executor, configuration);
            builder.runner(new ProgramRunner());
        }
        catch (ExecutionException e) {
            e.printStackTrace();
            return;
        }
        ProgramRunnerUtil.executeConfiguration(builder
                .contentToReuse(null)
                .dataContext(null)
                .activeTarget()
                .build(), true, true);
    }

}
