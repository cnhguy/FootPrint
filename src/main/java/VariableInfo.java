import com.sun.jdi.Value;
import com.sun.tools.jdi.ArrayReferenceImpl;

/**
 * Variable info holds a value and the line number at which it was assigned.
 */
public class VariableInfo {
    private int line;
    private String value;

    public VariableInfo(int line, Value value) {
        this.line = line;
        this.value = valueAsString(value);
    }

    @Override
    public String toString() {
        return "line: " + line + ", value: " + value;
    }


    private String valueAsString(Value value) {
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

    /**
     * Returns the value in string format
     * @return the value
     */
    public String getValue() {
        return value;
    }
    /**
     * Returns the line number this change was made on
     * @return the line number
     */
    public int getLineNumber() {
        return line;
    }

    public boolean equals(Object o) {

        // If the object is compared with itself then return true
        if (o == this) {
            return true;
        }

        /* Check if o is an instance of Complex or not
          "null instanceof [type]" also returns false */
        if (!(o instanceof VariableInfo)) {
            return false;
        }

        // typecast o to Complex so that we can compare data members
        VariableInfo c = (VariableInfo) o;

        // Compare the data members and return accordingly
        return this.value.equals(c.value);
    }
}
