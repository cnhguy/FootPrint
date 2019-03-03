import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;
import com.sun.jdi.Field;
import com.sun.jdi.LocalVariable;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.DefaultTableModel;

/**
 * Manages the ToolWindow's content, i.e. what is displayed in the tool window.
 */
public class FootPrintToolWindow {
    private static FootPrintToolWindow INSTANCE;
//initilize a cache
    private final MasterCache cache;
//Construct the variables
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
//initilize the cache, and the initComponents
    private FootPrintToolWindow () {
        cache = MasterCache.getInstance();
        initComponents();
    }
//Setup the components
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
     * Reset the content to the initial state.
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

    private List<LocalVariable> localVars;
    private List<Field> fields;
    private List<Object> vars;

    /**
     * The set DebugCache should call this method to notify this class of a change in the cache and to update accordingly
     * The cacheChanges() will load the left table.
     **/
    public void cacheChanged() {
        DefaultTableModel leftTableModel = (DefaultTableModel)leftTable.getModel();
        int leftTableSelection = leftTable.getSelectedRow();
        //set visible to false to avoid a race condition
        leftTable.setVisible(false);
        leftTableModel.setRowCount(0);
        localVars = cache.getAllVariables();
        fields = cache.getAllFields();
        vars = new ArrayList<>();


        for (int i = localVars.size() - 1; i >= 0; i--) {
            LocalVariable v = localVars.get(i);
            String[] rowData = {v.name()};
            leftTableModel.addRow(rowData);
            vars.add(v);
        }

        for (int i = fields.size() - 1; i >= 0; i--) {
            Field f = fields.get(i);
            String[] rowData = {f.name()};
            leftTableModel.addRow(rowData);
            vars.add(f);
        }
//        for (LocalVariable v : localVars) {
//            String[] rowData = {v.name()};
//            leftTableModel.addRow(rowData);
//            vars.add(v);
//        }
//
//        for (Field f : fields) {
//            String[] rowData = {f.name()};
//            leftTableModel.addRow(rowData);
//            vars.add(f);
//        }

        leftTable.setVisible(true);

        if (leftTableSelection != -1) {
            leftTable.setRowSelectionInterval(leftTableSelection, leftTableSelection);
        }

    }



    /**
     * The righttable is displaying the line number and values according to the selection in the left table
     * @param leftTableVarIndex
     */
    private void updateRightTable(int leftTableVarIndex) {
        if (leftTableVarIndex == -1)
            return;
        DefaultTableModel rightTableModel = (DefaultTableModel)rightTable.getModel();
        //set visible to false to avoid a race condition
        rightTable.setVisible(false);
        rightTableModel.setRowCount(0);
        Object leftTableVariable = vars.get(leftTableVarIndex);

        List<VariableInfo> history = cache.getHistory(leftTableVariable);
        for(int i = 0; i < history.size(); i++) {
            Object[] rowData = {history.get(i).getLine(), history.get(i).getValue()};
            rightTableModel.addRow(rowData);
        }
        rightTable.setVisible(true);


    }

    /**
     *     Displays notification everytime there is a mostrecentupdate called
     *     Object[] rowData = {cache.getHistory(leftTableVariable).get(i).getLine(),cache.getHistory(leftTableVariable).get(i).getValue()};
     *     rightTableModel.addRow(cache.getMostRecentUpdate());
     */

    /**
     * returns the content to be displayed by the ToolWindow
     * @return
     */
   public JPanel getContent() {
        return content;
   }
}
