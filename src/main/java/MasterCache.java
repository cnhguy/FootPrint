import com.sun.jdi.Field;
import com.sun.jdi.LocalVariable;
import com.sun.jdi.Method;

import java.util.*;

/**
 * Master cache maps object instances to methods that were called. Each methods
 * has their own DebugCache that stores their local variables and histories.
 * Master Cache also keeps track of objects and their fields
 */
public class MasterCache {
    /**
     * Maps objects to methods to DebugCache containing local variables and their histories
     */
    private Map<String, Map<Method, DebugCache>> objects;

    /**
     * Maps objects to fields and their histories
     */
    private Map<String, DebugCache> fields;

    /**
     * Singleton instance of MasterCache
     */
    private static MasterCache INSTANCE;

    // private constructor for MasterCache
    private MasterCache() {
        objects = new HashMap<>();
        fields = new HashMap<>();
    }

    /**
     * Get a singleton instance of the master cache
     * @return singleton instance of the master cache
     */
    public static MasterCache getInstance() {
        synchronized (MasterCache.class) {
            if (INSTANCE == null)
                INSTANCE = new MasterCache();
            return INSTANCE;
        }
    }

    /**
     * Put a new local variable info into the cache
     * @param object loaded class/object the variable is in
     * @param method method the variable is in
     * @param var local variable
     * @param info variable's info
     */
    public void put(String object, Method method, LocalVariable var, VariableInfo info) {
        Map<Method, DebugCache> methodCache = objects.get(object);
        if(methodCache == null) {
            methodCache = new HashMap<>();
        }
        DebugCache varCache = methodCache.get(method);
        if(varCache == null) {
            varCache = new DebugCache();
        }
        varCache.put(var, info);
        methodCache.put(method, varCache);
        objects.put(object, methodCache);
    }

    /**
     * Put a new field variable info into the cache
     * @param object the field's loaded class/object
     * @param field field
     * @param info field's info
     */
    public void put(String object, Field field, VariableInfo info) {
        DebugCache fieldCache = fields.get(object);
        if(fieldCache == null) {
            fieldCache = new DebugCache();
        }
        fieldCache.put(field, info);
        fields.put(object, fieldCache);
    }

    /**
     * Returns a list of loaded class/object ids
     * @return list of loaded class/object ids
     */
    public List<String> getAllObjects() {
        return new ArrayList<>(objects.keySet());
    }

    /**
     * Returns a list of methods associated with an objectID
     * @param objectID objectID
     * @return list of methods associated with an objectID
     */
    public List<Method> getAllMethods(String objectID) {
        Map<Method, DebugCache> methodCache = objects.get(objectID);
        return new ArrayList<>(methodCache.keySet());
    }

    /**
     * Returns a list of local variables associated with an objectID and method
     * @param objectID objectID
     * @param method method
     * @return list of local variables associated with an objectID and method
     */
    public List<LocalVariable> getAllLocalVariables(String objectID, Method method) {
        return objects.get(objectID).get(method).getAllVariables();
    }

    /**
     * Returns a list of fields associated with an objectID
     * @param objectID objectID
     * @return list of fields associated with an objectID
     */
    public List<Field> getAllFields(String objectID) {
        DebugCache localfield = fields.get(objectID);
        if ( localfield==null) return Collections.emptyList();
        return localfield.getAllFields();
    }

    /**
     * Returns the history of a local variable or field
     * @param objectID object the local variable is in
     * @param method method the local variable is in
     * @param var local variable or field
     * @return the history of a local variable or field
     */
    public List<VariableInfo> getHistory(String objectID, Method method, Object var) {
        if (var instanceof LocalVariable) {
            return objects.get(objectID).get(method).getHistory(var);
        } else {
            return fields.get(objectID).getHistory(var);
        }
    }

    // use the following methods if you want to interact with the lower level cache yourself

    /**
     * Returns a lower level cache that maps local variables to their histories
     * @param objectID object the local variables are in
     * @param method method the local variables are in
     * @return a lower level cache that maps local variables to their histories
     */
    public DebugCache getVarsCache(String objectID, Method method) {
        return objects.get(objectID).get(method);
    }

    /**
     * Returns a lower level cache that maps fields to their histories
     * @param objectID object whose field cache you want to get
     * @return a lower level cache that maps fields to their histories
     */
    public DebugCache getFieldsCache(String objectID) {
        return fields.get(objectID);
    }

    /**
     * Clears the cache
     */
    public void clear() {
        objects.clear();
        fields.clear();
    }

    /**
     * Returns size of the MasterCache (objects + fields)
     * @return size of this
     */
    public int size() {
        return objects.size() + fields.size();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Map<Method, DebugCache>> e : objects.entrySet()) {
            sb.append(e.getKey() + ":\n");
            sb.append("\t\t" + fields.get(e.getKey()));

            for (Map.Entry<Method, DebugCache> m : e.getValue().entrySet()) {
                sb.append("\t" + m.getKey().name() + "\n");
                sb.append("\t\t\t" + m.getValue());
            }
        }
        return sb.toString();
    }
}
