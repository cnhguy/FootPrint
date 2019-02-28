import com.sun.jdi.Field;
import com.sun.jdi.LocalVariable;

import java.util.HashMap;
import java.util.Map;

public class MasterCache {
    private Map<String, Map<String, DebugCache>> objects;
    private Map<String, DebugCache> fields;

    private static MasterCache INSTANCE;

    private MasterCache() {
        objects = new HashMap<String, Map<String, DebugCache>>();
    }

    public static MasterCache getInstance() {
        synchronized (MasterCache.class) {
            if (INSTANCE == null)
                INSTANCE = new MasterCache();
            return INSTANCE;
        }
    }

    public void put(String object, String method, LocalVariable var, VariableInfo info) {
        Map<String, DebugCache> objectCache = objects.get(object);
        if(objectCache == null) {
            objectCache = new HashMap<String, DebugCache>();
        }
        DebugCache methodCache = objectCache.get(method);
        if(methodCache == null) {
            methodCache = new DebugCache();
        }
        methodCache.put(var, info);
        objectCache.put(method, methodCache);
        objects.put(object, objectCache);
    }

    public void put(String object, String method, Field f, VariableInfo info) {
        DebugCache fieldCache = fields.get(object);
        if(fieldCache == null) {
            fieldCache = new DebugCache();
        }
        fieldCache.put(f, info);
        fields.put(object, fieldCache);
    }

    public Map<String, DebugCache> getMethodCache(String object) {
        Map<String, DebugCache> methodCache = objects.get(object);
        return methodCache;
    }

    public DebugCache getVarsCache(String object, String method) {
        Map<String, DebugCache> methodCache = this.getMethodCache(object);
        return methodCache.get(method);
    }

    public DebugCache getFieldsCache(String object) {
        return fields.get(object);
    }
}
