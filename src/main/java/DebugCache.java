import java.util.HashMap;
import java.util.Map;

/**
 * Cache that holds info about local variables on the stack. Maps the name
 * of the variable to a VariableInfo object that holds its previous values
 * and the line number at which those values were assigned. Note that
 * values are only cached if they have changed.
 */
public class DebugCache {
    private Map<String, VariableInfo> vars;

    public DebugCache() {
        vars = new HashMap<String, VariableInfo>();
    }

    public VariableInfo get(String var) {
        return vars.get(var);
    }

    public void put(String var, VariableInfo info) {
        vars.put(var, info);
    }

    public String toString() {
        String res = "CACHE:\n";
        for(String var : vars.keySet()) {
            res += "\n";
            res += var  + ": \n";
            res += vars.get(var).toString();
            res += "\n";
        }
        return res;
    }
}
