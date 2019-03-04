import com.sun.jdi.Field;
import com.sun.jdi.LocalVariable;
import com.sun.jdi.Method;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MasterCache {
    private Map<String, Map<Method, DebugCache>> objects;
    private Map<String, DebugCache> fields;

    private static MasterCache INSTANCE;

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

//    /**
//     * Returns the object's method cache. Method cache maps a method's name to another
//     * lower level cache (called DebugCache). The De
//     * @param object
//     * @return
//     */
//    public Map<String, DebugCache> getMethodCache(String object) {
//        Map<String, DebugCache> methodCache = objects.get(object);
//        return methodCache;
//    }
//
//    public DebugCache getVarsCache(String object, String method) {
//        Map<String, DebugCache> methodCache = this.getMethodCache(object);
//        return methodCache.get(method);
//    }
//
//    public DebugCache getFieldsCache(String object) {
//        return fields.get(object);
//    }

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
