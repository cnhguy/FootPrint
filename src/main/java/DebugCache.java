

import com.sun.jdi.Field;
import com.sun.jdi.LocalVariable;

import java.util.*;

/**
 * Cache that holds info about local variables and fields on the stack. Maps the field or variable
 * to a list of VariableInfo object that holds its previous values
 * and the line number at which those values were assigned. Note that
 * values are only cached if they have changed.
 */
public class DebugCache {

    private static DebugCache INSTANCE;

    /**
     * Map of local variables on the stack
     */
    private Map<LocalVariable, LinkedList<VariableInfo>> vars;

    /**
     * Map of fields
     */
    private Map<Field, LinkedList<VariableInfo>> fields;

    private DebugCache() {
        vars = new HashMap<>();
        fields = new HashMap<>();
    }

    /**
     * Returns an instance of the cache
     * @return an instance of the cache
     */
    public static DebugCache getInstance() {
        synchronized (DebugCache.class) {
            if (INSTANCE == null)
                INSTANCE = new DebugCache();
            return INSTANCE;
        }
    }


    /**
     * Returns the history of the var
     * @param var field or local variable
     * @return the history of var's values
     */
    public List<VariableInfo> getHistory(Object var) {
        if (var instanceof LocalVariable) {
            return (List<VariableInfo>) vars.get(var).clone();
        } else if (var instanceof Field) {
            return (List<VariableInfo>) fields.get(var).clone();
        }
        return null;
    }

    /**
     * Returns all the variables in the cache
     * @return all the variables in the cache
     */
    public List<LocalVariable> getAllVariables() {
        return new ArrayList<>(vars.keySet());
    }

    /**
     * Returns all the fields in the cache
     * @return all the fields in the cache
     */
    public List<Field> getAllFields() {
        return new ArrayList<>(fields.keySet());
    }

    /**
     * Returns the most recent update that was made to the variable
     * @param var the variable
     * @return the most recent update that was made to the variable
     */
    public VariableInfo getMostRecentUpdate(LocalVariable var) {
        if (vars.containsKey(var)) {
            return vars.get(var).getLast();
        }
        return null;
    }

    /**
     * Adds the given information to the cache.
     * @param var variable
     * @param line line number
     * @param value variable's value
     */
    public void put(LocalVariable var, Integer line, String value) {
        LinkedList<VariableInfo> info = vars.get(var);
        if (info == null) {
            info = new LinkedList<>();
        }
        VariableInfo update = new VariableInfo(line, value);
        if (info.size() == 0 || !update.equals(info.getLast())) {
            info.add(update);
            vars.put(var, info);
        }
    }

    /**
     * Adds the given information to the cache.
     * @param field field
     * @param line line number
     * @param value variable's value
     */
    public void put(Field field, Integer line, String value) {
        LinkedList<VariableInfo> info = fields.get(field);
        if (info == null) {
            info = new LinkedList<>();
        }
        VariableInfo update = new VariableInfo(line, value);
        if (info.size() == 0 || !update.equals(info.getLast())) {
            info.add(update);
            fields.put(field, info);
        }
    }

    /**
     * Notify the UI of changes to the cache.
     */
    public void pushChangeToUI() {
        FootPrintToolWindow.getInstance().cacheChanged();
    }

    /**
     * Clears the cache
     */
    public void clear() {
        vars.clear();
        fields.clear();
    }

    @Override
    public String toString() {
        String res = "CACHE:\n\n";
        for(LocalVariable var : vars.keySet()) {
            res += var.name() + "\n";
            res += vars.get(var).toString() + "\n\n";
        }
        for(Field f : fields.keySet()) {
            res += f.name() + "\n";
            res += fields.get(f).toString() + "\n\n";
        }
        return res;
    }
}
