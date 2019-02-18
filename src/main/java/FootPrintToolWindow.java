import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;

public class FootPrintToolWindow {
    private GridLayout layout;
    private JPanel content;
    private JScrollPane leftScrollPane;
    private JTable leftTable;
    private JScrollPane rightScrollPane;
    private JTable rightTable;


    public FootPrintToolWindow (ToolWindow toolWindow) {
        initComponents();
    }

    private void initComponents() {
        layout = new GridLayout(0,2);
        content = new JPanel();
        content.setLayout(layout);

        leftTable = new JBTable();
        leftTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        leftTable.setRowSelectionAllowed(true);
        leftTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                updateRightTable(leftTable.getSelectedRow());
            }
        });
        leftScrollPane = new JBScrollPane(leftTable);
        content.add(leftScrollPane);

        rightTable = new JBTable();
        rightScrollPane = new JBScrollPane(rightTable);
        content.add(rightScrollPane);

    }

    private void updateRightTable(int leftTableRow) {
        System.out.println("update right table with row: " + leftTableRow);
    }

   public JPanel getContent() {
        return content;
   }
}
