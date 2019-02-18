import com.sun.jdi.Type;
import com.sun.jdi.Value;
import com.sun.jdi.VirtualMachine;

public class TestValue implements Value {

    private String val;

    public TestValue(String val) {
        this.val = val;
    }

    public String toString() {
        return val;
    }

    @Override
    public Type type() {
        return null;
    }

    @Override
    public VirtualMachine virtualMachine() {
        return null;
    }
}
