import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.lang.Object.*;
import java.util.List;
import javax.swing.table.DefaultTableModel;

/**
 * Manages the ToolWindow's content, i.e. what is displayed in the tool window.
 */
public class FootPrintToolWindow {
    private static FootPrintToolWindow INSTANCE;

    private final DebugCache cache;

    private GridLayout layout;
    private JPanel content;
    private JScrollPane leftScrollPane;
    private JTable leftTable;
    private JScrollPane rightScrollPane;
    private JTable rightTable;

    /**
     * Returns an instance of this
     * @return
     */
    public static FootPrintToolWindow getInstance() {
        synchronized (FootPrintToolWindow.class) {
            if (INSTANCE == null)
                INSTANCE = new FootPrintToolWindow();
            return INSTANCE;
        }
    }

    private FootPrintToolWindow () {
        cache = DebugCache.getInstance();
        initComponents();
    }

    private void initComponents() {
        layout = new GridLayout(0,2);
        content = new JPanel();
        content.setLayout(layout);


        leftTable = new JBTable(new DefaultTableModel(new String[]{"Variables"}, 0));
        leftTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        leftTable.setRowSelectionAllowed(true);
        leftTable.getSelectionModel().addListSelectionListener(e -> updateRightTable(leftTable
                .getSelectedRow()));

        leftScrollPane = new JBScrollPane(leftTable);
        content.add(leftScrollPane);

        rightTable = new JBTable(new DefaultTableModel(new String[]{"Line","Value"}, 0));
        rightScrollPane = new JBScrollPane(rightTable);
        content.add(rightScrollPane);
    }

    /**
     * Reset the content to the intial state.
     */
    public void reset() {
        leftTable.setVisible(false);
        ((DefaultTableModel)leftTable.getModel()).setRowCount(0);

        rightTable.setVisible(false);
        ((DefaultTableModel)rightTable.getModel()).setRowCount(0);

        leftTable.getSelectionModel().clearSelection();
        rightTable.getSelectionModel().clearSelection();

        leftTable.setVisible(true);
        rightTable.setVisible(true);
    }

    private List<String> vars;
    /**
     * The set DebugCache should call this method to notify this class of a change in the cache and to update accordingly
     **/
    public void cacheChanged() {
        DefaultTableModel leftTableModel = (DefaultTableModel)leftTable.getModel();
        int leftTableSelection = leftTable.getSelectedRow();
        //set visible to false to avoid a race condition
        leftTable.setVisible(false);
        leftTableModel.setRowCount(0);
        vars = cache.getAllVariables();

        for(int i = 0; i < vars.size(); i++) {
            String[] rowData = {vars.get(i)};
            leftTableModel.addRow(rowData);
        }
        leftTable.setVisible(true);

        if (leftTableSelection != -1) {
            leftTable.setRowSelectionInterval(leftTableSelection, leftTableSelection);
        }
        //the mostRecentUpdats should reflect the updates from a specific variable, and later appened into the indexed row
    }


    private void updateRightTable(int leftTableVarIndex) {
        if (leftTableVarIndex == -1)
            return;
//        System.out.println("update right table with row: " + leftTableRow);
        //link the leftTableRow to the Variable it is referring to
        DefaultTableModel rightTableModel = (DefaultTableModel)rightTable.getModel();
        //set visible to false to avoid a race condition
        rightTable.setVisible(false);
        rightTableModel.setRowCount(0);
        String leftTableVariable = vars.get(leftTableVarIndex);

        List<VariableInfo> history = cache.getHistory(leftTableVariable);
        for(int i = 0; i < history.size(); i++) {
            Object[] rowData = {history.get(i).getLine(), history.get(i).getValue()};
            rightTableModel.addRow(rowData);
        }
        rightTable.setVisible(true);
        //Updating from getMostRecentUpdate
        //Object[] rowData = {cache.getHistory(leftTableVariable).get(i).getLine(),cache.getHistory(leftTableVariable).get(i).getValue()};
        //rightTableModel.addRow(cache.getMostRecentUpdate());

    }

    /**
     * returns the content to be displayed by the ToolWindow
     * @return
     */
   public JPanel getContent() {
        return content;
   }
}
