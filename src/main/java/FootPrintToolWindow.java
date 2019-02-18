import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.lang.Object.*;
import javax.swing.table.DefaultTableModel;

public class FootPrintToolWindow {
    private static final FootPrintToolWindow INSTANCE = new FootPrintToolWindow();

    private DebugCache cache;

    private GridLayout layout;
    private JPanel content;
    private JScrollPane leftScrollPane;
    private JTable leftTable;
    private JScrollPane rightScrollPane;
    private JTable rightTable;

    public static FootPrintToolWindow getInstance() {return INSTANCE;}

    private FootPrintToolWindow () {
        initComponents();
    }

    private void initComponents() {
        layout = new GridLayout(0,2);
        content = new JPanel();
        content.setLayout(layout);


        leftTable = new JBTable(new DefaultTableModel(new Object[]{"Variables"}, 0));
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

        rightTable = new JBTable(new DefaultTableModel(new Object[]{"Line","Value"}, 0));
        rightScrollPane = new JBScrollPane(rightTable);
        content.add(rightScrollPane);

    }

    /**
     * Set the DebugCache
     * @param cache
     */
    public void setCache(DebugCache cache) {
        this.cache = cache;
    }

    /**
     * The set DebugCache should calls this method to notify this class of a change in the cache and to update accordingly
     **/
    public void cacheChanged() {
        DefaultTableModel leftTableModel = (DefaultTableModel)leftTable.getModel();
        leftTable.setVisible(true);
       leftTableModel.setRowCount(0);
        for(int i=0; i<cache.getAllVariables().size();i++) {
            Object[] rowData = {cache.getAllVariables().get(i)};
            leftTableModel.addRow(rowData);
        }

        //the mostRecentUpdats should reflect the updates from a specific variable, and later appened into the indexed row
    }


    private void updateRightTable(int leftTableRow) {
        System.out.println("update right table with row: " + leftTableRow);
        //link the leftTableRow to the Variable it is referring to
        DefaultTableModel rightTableModel = (DefaultTableModel)rightTable.getModel();
        rightTableModel.setRowCount(0);
        String leftTableVariable = cache.getAllVariables().get(leftTableRow);
        for(int i=0; i<cache.getHistory(leftTableVariable).size();i++) {
            Object[] rowData = {cache.getHistory(leftTableVariable).get(i).getLine(),cache.getHistory(leftTableVariable).get(i).getValue()};
            rightTableModel.addRow(rowData);
        }
        //Updating from getMostRecentUpdate
        //Object[] rowData = {cache.getHistory(leftTableVariable).get(i).getLine(),cache.getHistory(leftTableVariable).get(i).getValue()};
        //rightTableModel.addRow(cache.getMostRecentUpdate());

    }

   public JPanel getContent() {
        return content;
   }
}
