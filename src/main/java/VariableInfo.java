import com.sun.jdi.Value;
import com.sun.tools.jdi.ArrayReferenceImpl;
import java.util.LinkedList;

/**
 * Variable info holds the values and line numbers at which the value was changed.
 */
public class VariableInfo {
    private LinkedList<Integer> lines;
    private LinkedList<Value> values;

    public VariableInfo() {
        this.lines = new LinkedList<Integer>();
        this.values = new LinkedList<Value>();
    }

    public void update(Integer line, Value value) {
        // only update if the value has changed
        if(values.size() == 0 || !values.getLast().equals(value)) {
            lines.add(line);
            values.add(value);
        }
    }

    public String toString() {
        String res = "VALUES: ";
        for(Value value : values) {
            res += valueAsString(value) + ", ";
        }
        res += "\nLINES: " + lines;
        return res;
    }

    public String valueAsString(Value value) {
        String valueAsString = null;
        if (value != null) {
            valueAsString = value.toString();
            // If the value is an array, print it out in array format --> [x, y, z]
            if (value instanceof ArrayReferenceImpl) {
                ArrayReferenceImpl valueAsArray = (ArrayReferenceImpl) value;
                int length = valueAsArray.length();
                valueAsString = "[";
                for (int i = 0; i < length - 1; i++) {
                    valueAsString += valueAsArray.getValue(i) + ", ";
                }
                // append the last element without the comma
                if (length > 0) {
                    valueAsString += valueAsArray.getValue(length - 1);
                }
                valueAsString += "]";
            }
        }
        return valueAsString;
    }
}
