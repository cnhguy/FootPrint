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
import static com.intellij.icons.AllIcons.Graph.Layout;


    /**
    * Manages the ToolWindow's content and display the content of User Interface
    */
public class FootPrintToolWindow {
    private static FootPrintToolWindow INSTANCE;
    //initilize a cache
    private final MasterCache cache;
    /* Construct the variables of the four tables and the according scrollpane. layout and content are the fields to display
    objectTable, methodTable, variableTable and infoTable are the four tables displayed in the UI content
    objectScrollPane, methodsScrollPane, variableScrollPane, infoScrollPane are the according components in UI
    */
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

    /*
     * Initialize the lists to store the objects, methods, localvariables, fields, variables, and history
     */
    private List<String> objects;
    private List<Method> methods;
    private List<LocalVariable> localVars;
    private List<Field> fields;
    private List<Object> vars;
    private List<VariableInfo> history;

    /**
     * @return Returns an instance of the toolwindow
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
    //Setup the components of the user interface
    private void initComponents() {
        layout = new GridLayout(0,4);
        content = new JPanel();
        content.setLayout(layout);
    // Set the Object table with a listener passing value to the updateMethodTable
        objectTable= new JBTable(new DefaultTableModel(new String[]{"Object"}, 0));
        objectTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        objectTable.setRowSelectionAllowed(true);
        objectTable.getSelectionModel().addListSelectionListener(e -> updateMethodTable(objectTable.getSelectedRow()));
        objectScrollPane=new JBScrollPane(objectTable);
        content.add(objectScrollPane);
    //Set the Method Table with a listener passing value to the updateVariableTable
        methodTable= new JBTable(new DefaultTableModel(new String[]{"Method"},0));
        methodTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        methodTable.setRowSelectionAllowed(true);
        methodTable.getSelectionModel().addListSelectionListener(e -> updateVariableTable(objectTable.getSelectedRow(),methodTable.getSelectedRow()));
        methodScrollPane=new JBScrollPane(methodTable);
        content.add(methodScrollPane);

    //Set the Variable Table with a listener passing value to the updateInforTable
        variableTable = new JBTable(new DefaultTableModel(new String[]{"Variables"}, 0));
        variableTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        variableTable.setRowSelectionAllowed(true);
        variableTable.getSelectionModel().addListSelectionListener(e -> updateInfoTable(objectTable.getSelectedRow(), methodTable.getSelectedRow(), variableTable.getSelectedRow()));
        variableScrollPane = new JBScrollPane(variableTable);
        content.add(variableScrollPane);

    //Set the Info Table to display the ultimate line numbner and the variable value
        infoTable = new JBTable(new DefaultTableModel(new String[]{"Line","Value"}, 0));
        infoScrollPane = new JBScrollPane(infoTable);
        content.add(infoScrollPane);
    }
    /**
     * Reset the content to the initial state.
     * Before display the tables, the user interface will first clean all the table contents, the logic here is to first hide the tables, clean the tables and then dispaly
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
    /**
     * The set DebugCache should call this method to notify this class of a change in the cache and to update accordingly
     * The cacheChanged() will load the object table.
     **/
    public void cacheChanged() {
        //Initialize the tablemodel to store and display the information
        DefaultTableModel objectTableModel = (DefaultTableModel)objectTable.getModel();
        //set the selection action
        int objectTableSelection = objectTable.getSelectedRow();
       //set visible to false to avoid a race condition
        objectTable.setVisible(false);
        objectTableModel.setRowCount(0);
        //store the objects value to the list objects
        objects = cache.getAllObjects();
        //display the object table using a loop
        for(int i = 0; i < objects.size(); i++) {
            String[] rowData = {objects.get(i)};
            objectTableModel.addRow(rowData);
        }
        //Display the objectTable
        objectTable.setVisible(true);
        if (objectTableSelection != -1) {
            objectTable.setRowSelectionInterval(objectTableSelection, objectTableSelection);
        }

    }
    /**
     * Update the Method Table according to the object selection
     * @param omTableIndex The index of user selection of Object Table
     */
    private void updateMethodTable(int omTableIndex) {
        // User can generate the table only when there is a selection in the object table
        if (omTableIndex == -1)
            return;
        //Initialize the tablemodel to store and display the information
        DefaultTableModel methodTableModel = (DefaultTableModel)methodTable.getModel();
        //set selection of the method table
        int methodTableSelection = methodTable.getSelectedRow();
        //set visible to false to avoid a race condition
        methodTable.setVisible(false);
        methodTableModel.setRowCount(0);
        //get the object selected and store the methods accordingly
        String omObjectID = objects.get(omTableIndex);
        methods=cache.getAllMethods(omObjectID);
        //build the methodstable through a loop
        for(int i = 0; i < methods.size(); i++) {
            String[] rowData = {methods.get(i).name()};
            methodTableModel.addRow(rowData);
        }
        //display the method table
        methodTable.setVisible(true);
        if (methodTableSelection != -1 && methodTableSelection < methodTable.getRowCount()) {
            methodTable.setRowSelectionInterval(methodTableSelection, methodTableSelection);
        }
    }
    /**
     * UPDATE THE VARIABLE TABLE ACCORDING TO OBJECTID AND METHODS
     * @param ovTableIndex The index of user selection of Object Table
     * @param mvTableIndex The index of user selection of Methods Table
     */
    private void updateVariableTable(int ovTableIndex, int mvTableIndex) {
        //The variable table is visiable only when the users made selections on the objects and methods
        if (ovTableIndex == -1 || mvTableIndex==-1)
            return;
        //Initialize the tablemodel to store and display the information
        DefaultTableModel variableTableModel = (DefaultTableModel)variableTable.getModel();
        //get the selection of the variable from users
        int variableTableSelection = variableTable.getSelectedRow();
        //set visible to false to avoid a race condition
        variableTable.setVisible(false);
        variableTableModel.setRowCount(0);
        //listen to the selection both from the objects and the methods
        String ovObjectID = objects.get(ovTableIndex);
        Method omMethod = methods.get(mvTableIndex);
        //get local variables and fields seperately
        localVars = cache.getAllLocalVariables(ovObjectID,omMethod);
        fields = cache.getAllFields(ovObjectID);
        vars = new ArrayList<>();
        for (Field f : fields) {
            String[] rowData = {f.name() + " (field)"};
            variableTableModel.addRow(rowData);
            vars.add(f);
        }
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
        //display the variable table
        variableTable.setVisible(true);
        if (variableTableSelection != -1 && variableTableSelection < variableTable.getRowCount()) {
            variableTable.setRowSelectionInterval(variableTableSelection, variableTableSelection);
        }

    }
    /**
     * Update the info table according to the selected object, method, and variable
     * @param oiTableIndex The index of the user selection from Object Table
     * @param miTableIndex The index of the user selection from Methods Table
     * @param viTableIndex The index of the user selection from Variable Table
     */
    private void updateInfoTable(int oiTableIndex, int miTableIndex, int viTableIndex) {
        if (oiTableIndex==-1 || miTableIndex==-1)
            return;
        // The info table is only visible only when the users made selections on the objects, methods, and variables
        if (viTableIndex == -1) {
            DefaultTableModel infoTableModel = (DefaultTableModel) infoTable.getModel();
            infoTable.setVisible(false);
            infoTableModel.setRowCount(0);
            return;
        }
        DefaultTableModel infoTableModel = (DefaultTableModel)infoTable.getModel();
        //set visible to false to avoid a race condition
        infoTable.setVisible(false);
        infoTableModel.setRowCount(0);
        //get the index
        String oiObject = objects.get(oiTableIndex);
        Method miMethod = methods.get(miTableIndex);
        Object viVariable = vars.get(viTableIndex);
        //store the history list
        history = cache.getHistory(oiObject,miMethod,viVariable);
        //display
        for(int i = 0; i < history.size(); i++) {
            if(history.get(i).getValue().contains("\n")){
                // If the string contains escape symbol, divide the string into multiple lines
                String[] splitString= history.get(i).getValue().split("\\\n");
                StringBuffer fatString = null;
                for(int j=0; j<splitString.length;j++ ){
                    fatString.append(splitString[j]);
                }
                //char[] charValue = history.get(i).getValue().toCharArray();
                for(int j=0; j< splitString.length; j++) {
                    Object[] rowDataSpecial = {history.get(i).getLine(), splitString[j]};
                    infoTableModel.addRow(rowDataSpecial);
                }
            }
            else{
                Object[] rowData = {history.get(i).getLine(), history.get(i).getValue()};
                infoTableModel.addRow(rowData);
            }
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