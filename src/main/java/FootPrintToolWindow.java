import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;
import com.sun.jdi.Field;
import com.sun.jdi.LocalVariable;
import com.sun.jdi.Method;

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
    private JScrollPane objectScrollPane;
    private JTable objectTable;
    private JScrollPane methodScrollPane;
    private JTable methodTable;
    private JScrollPane variableScrollPane;
    private JTable variableTable;
    private JScrollPane infoScrollPane;
    private JTable infoTable;


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
        layout = new GridLayout(0,4);
        content = new JPanel();
        content.setLayout(layout);

        objectTable= new JBTable(new DefaultTableModel(new String[]{"Object"}, 0));
        objectTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        objectTable.setRowSelectionAllowed(true);
        objectTable.getSelectionModel().addListSelectionListener(om -> updateMethodTable(objectTable.getSelectedRow()));
        objectTable.getSelectionModel().addListSelectionListener(ov -> updateVariableTable(objectTable.getSelectedRow(),methodTable.getSelectedRow()));
        objectTable.getSelectionModel().addListSelectionListener(oi-> updateInfoTable(objectTable.getSelectedRow(), methodTable.getSelectedRow(), variableTable.getSelectedRow()));
        objectScrollPane=new JBScrollPane(objectTable);
        content.add(objectScrollPane);


        methodTable= new JBTable(new DefaultTableModel(new String[]{"Method"},0));
        methodTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        methodTable.setRowSelectionAllowed(true);
        methodTable.getSelectionModel().addListSelectionListener(mv -> updateVariableTable(objectTable.getSelectedRow(),methodTable.getSelectedRow()));
        methodTable.getSelectionModel().addListSelectionListener(mi -> updateInfoTable(objectTable.getSelectedRow(),methodTable.getSelectedRow(),variableTable.getSelectedRow()));
        methodScrollPane=new JBScrollPane(methodTable);
        content.add(methodScrollPane);


        variableTable = new JBTable(new DefaultTableModel(new String[]{"Variables"}, 0));
        variableTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        variableTable.setRowSelectionAllowed(true);
        variableTable.getSelectionModel().addListSelectionListener(vi -> updateInfoTable(objectTable.getSelectedRow(), methodTable.getSelectedRow(), variableTable.getSelectedRow()));
        variableScrollPane = new JBScrollPane(variableTable);
        content.add(variableScrollPane);


        infoTable = new JBTable(new DefaultTableModel(new String[]{"Line","Value"}, 0));
        infoScrollPane = new JBScrollPane(infoTable);
        content.add(infoScrollPane);


    }

    /**
     * Reset the content to the initial state.
     */
    public void reset() {
        objectTable.setVisible(false);
        ((DefaultTableModel)objectTable.getModel()).setRowCount(0);

        methodTable.setVisible(false);
        ((DefaultTableModel)methodTable.getModel()).setRowCount(0);

        variableTable.setVisible(false);
        ((DefaultTableModel)variableTable.getModel()).setRowCount(0);

        infoTable.setVisible(false);
        ((DefaultTableModel)infoTable.getModel()).setRowCount(0);

        objectTable.getSelectionModel().clearSelection();
        methodTable.getSelectionModel().clearSelection();
        variableTable.getSelectionModel().clearSelection();
        infoTable.getSelectionModel().clearSelection();

        objectTable.setVisible(true);
        methodTable.setVisible(true);
        variableTable.setVisible(true);
        infoTable.setVisible(true);
    }

    //Initilize the lists
    private List<String> objects;
    private List<Method> methods;
    private List<LocalVariable> localVars;
    private List<Field> fields;
    private List<Object> vars;
    private List<VariableInfo> history;

    /**
     * The set DebugCache should call this method to notify this class of a change in the cache and to update accordingly
     * The cacheChanges() will load the left table.
     **/
    public void cacheChanged() {
        DefaultTableModel objectTableModel = (DefaultTableModel)objectTable.getModel();
        //set selection
        int objectTableSelection = objectTable.getSelectedRow();
       //set visible to false to avoid a race condition
        objectTable.setVisible(false);
        objectTableModel.setRowCount(0);
        //store
        objects = cache.getAllObjects();
        //display
        for(int i = 0; i < objects.size(); i++) {
            String[] rowData = {objects.get(i)};
            objectTableModel.addRow(rowData);
        }
        variableTable.setVisible(true);
        if (objectTableSelection != -1) {
            objectTable.setRowSelectionInterval(objectTableSelection, objectTableSelection);
        }

    }

    private void updateMethodTable(int omTableIndex) {
        if (omTableIndex == -1)
            return;
        DefaultTableModel methodTableModel = (DefaultTableModel)methodTable.getModel();
        //set selection
        int methodTableSelection = methodTable.getSelectedRow();
        //set visible to false to avoid a race condition
        methodTable.setVisible(false);
        methodTableModel.setRowCount(0);
        //get and store
        String omObjectID = objects.get(omTableIndex);
        methods=cache.getAllMethods(omObjectID);
        //display
        for(int i = 0; i < methods.size(); i++) {
            String[] rowData = {methods.get(i).toString()};
            methodTableModel.addRow(rowData);
        }
        methodTable.setVisible(true);
        if (methodTableSelection != -1) {
            methodTable.setRowSelectionInterval(methodTableSelection, methodTableSelection);
        }
    }


    private void updateVariableTable(int ovTableIndex, int mvTableIndex) {
        if (ovTableIndex == -1 || mvTableIndex==-1)
            return;
        DefaultTableModel variableTableModel = (DefaultTableModel)variableTable.getModel();
        //set selection
        int variableTableSelection = variableTable.getSelectedRow();
        //set visible to false to avoid a race condition
        variableTable.setVisible(false);
        variableTableModel.setRowCount(0);
        //get, store and display
        String ovObjectID = objects.get(ovTableIndex);
        Method omMethod = methods.get(mvTableIndex);
        localVars = cache.getAllLocalVariables(ovObjectID,omMethod);
        fields = cache.getAllFields(ovObjectID);
        vars = new ArrayList<>();
        for (LocalVariable v : localVars) {
            String[] rowData = {v.name()};
            variableTableModel.addRow(rowData);
            vars.add(v);
        }
        for (Field f : fields) {
            String[] rowData = {f.name()};
            variableTableModel.addRow(rowData);
            vars.add(f);
        }

        variableTable.setVisible(true);

        if (variableTableSelection != -1) {
            variableTable.setRowSelectionInterval(variableTableSelection, variableTableSelection);
        }

    }


    private void updateInfoTable(int oiTableIndex, int miTableIndex, int viTableIndex) {
        if (oiTableIndex==-1 || miTableIndex==-1 || viTableIndex == -1)
            return;
        DefaultTableModel infoTableModel = (DefaultTableModel)infoTable.getModel();
//        //set visible to false to avoid a race condition
        infoTable.setVisible(false);
        infoTableModel.setRowCount(0);
        String oiObject = objects.get(oiTableIndex);
        Method miMethod = methods.get(miTableIndex);
        Object viVariable = vars.get(viTableIndex);
//
        history = cache.getHistory(oiObject,miMethod,viVariable);
        for(int i = 0; i < history.size(); i++) {
            Object[] rowData = {history.get(i).getLine(), history.get(i).getValue()};
            infoTableModel.addRow(rowData);
        }
        infoTable.setVisible(true);
    }



    /**
     * returns the content to be displayed by the ToolWindow
     * @return
     */
   public JPanel getContent() {
        return content;
   }
}
