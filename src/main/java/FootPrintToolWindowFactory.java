import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

/**
 * Custom ToolWindowFactory to create FootPrint's ToolWindow.
 */
public class FootPrintToolWindowFactory implements ToolWindowFactory {
    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        FootPrintToolWindow fToolWindow = FootPrintToolWindow.getInstance();
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content = contentFactory.createContent(fToolWindow.getContent(), "", false);
        toolWindow.getContentManager().addContent(content);
        toolWindow.hide(null);
    }

    /**
     * The ToolWindow should be available to all projects
     * @param project
     * @return
     */
    @Override
    public boolean shouldBeAvailable(@NotNull Project project) {
        return true;
    }

    /**
     * The tool window is not created on start
     * @return
     */
    @Override
    public boolean isDoNotActivateOnStart() {
        return true;
    }
}
