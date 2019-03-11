import com.sun.jdi.*;
import org.jetbrains.annotations.NotNull;

public class TestLocalVariable implements LocalVariable {

    private String s;

    public TestLocalVariable(String s) {
        this.s = s;
    }
    @Override
    public String name() {
        return null;
    }

    @Override
    public String typeName() {
        return null;
    }

    @Override
    public Type type() throws ClassNotLoadedException {
        return null;
    }

    @Override
    public String signature() {
        return null;
    }

    @Override
    public String genericSignature() {
        return null;
    }

    @Override
    public boolean isVisible(StackFrame stackFrame) {
        return false;
    }

    @Override
    public boolean isArgument() {
        return false;
    }

    @Override
    public VirtualMachine virtualMachine() {
        return null;
    }

    @Override
    public int compareTo(@NotNull LocalVariable o) {
        return 0;
    }
}
