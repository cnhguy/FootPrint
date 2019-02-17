import com.sun.jdi.Value;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Cache that holds info about local variables on the stack. Maps the name
 * of the variable to a list of VariableInfo object that holds its previous values
 * and the line number at which those values were assigned. Note that
 * values are only cached if they have changed.
 */
public class DebugCache {

    private static DebugCache debugCache;

    private Map<String, LinkedList<VariableInfo>> vars;

    private DebugCache() {
        vars = new HashMap<String, LinkedList<VariableInfo>>();
    }

    public static DebugCache getInstance() {
        if (debugCache == null) {
            debugCache = new DebugCache();
        }
        return debugCache;
    }
    public List<VariableInfo> get(String var) {
        return vars.get(var);
    }

    public void put(String name, Integer line, Value value) {
        LinkedList<VariableInfo> info = vars.get(name);
        if(info == null) {
            info = new LinkedList<VariableInfo>();
        }
        VariableInfo update = new VariableInfo(line, value);
        if(info.size() == 0 || !update.equals(info.getLast())) {
            info.add(update);
            vars.put(name, info);
        }
    }

    public void clear() {
        vars = new HashMap<String, LinkedList<VariableInfo>>();
    }

    public String toString() {
        String res = "CACHE:\n\n";
        for(String var : vars.keySet()) {
            res += var + "\n";
            res += vars.get(var).toString() + "\n\n";
        }
        return res;
    }
}
