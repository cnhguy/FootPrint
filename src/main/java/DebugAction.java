import com.intellij.execution.*;
import com.intellij.execution.impl.RunManagerImpl;
import com.intellij.execution.runners.ExecutionEnvironmentBuilder;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Class called when a user clicks on the FootPrint Debug icon to initiate FootPrint during debugging.
 */
public class DebugAction extends AnAction{

    /**
     * Creates a DebugAction
     */
    public DebugAction() {
        super("","start FootPrint", AllIcons.General.IjLogo);
    }

    /**
     * Callback for button clicked. Initiates the debugging with FootPrint.
     * @param e
     */
    @Override
    public void actionPerformed(AnActionEvent e) {
        RunnerAndConfigurationSettings config = RunManagerImpl.getInstanceEx(e.getProject())
                .getSelectedConfiguration();
        if (config != null) {
            System.out.println(config.getName()); // prints out the method's name

            ProgramRunnerUtil.executeConfiguration(config, FootPrint_Executor.getMyExecutorInstance());
        }
    }

}
