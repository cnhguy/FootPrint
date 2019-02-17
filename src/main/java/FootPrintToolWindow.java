import com.intellij.openapi.wm.ToolWindow;

import javax.swing.*;
import java.awt.*;

public class FootPrintToolWindow {
    private GridLayout layout;
    private JPanel content;
    private JLabel label;


    public FootPrintToolWindow (ToolWindow toolWindow) {
        initComponents();
    }

    private void initComponents() {
        layout = new GridLayout(0,2);
        content = new JPanel();
        content.setLayout(layout);

        label =  new JLabel("hi");
        content.add(label);

        content.add(new JButton("fdasfjdas"));

    }

   public JPanel getContent() {
        return content;
   }
}
