import com.sun.jdi.*;
import org.jetbrains.annotations.NotNull;


public class TestField implements Field {
    private String s;

    public TestField(String s) {
        this.s = s;
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
    public boolean isTransient() {
        return false;
    }

    @Override
    public boolean isVolatile() {
        return false;
    }

    @Override
    public boolean isEnumConstant() {
        return false;
    }

    @Override
    public String name() {
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
    public ReferenceType declaringType() {
        return null;
    }

    @Override
    public boolean isStatic() {
        return false;
    }

    @Override
    public boolean isFinal() {
        return false;
    }

    @Override
    public boolean isSynthetic() {
        return false;
    }

    @Override
    public int modifiers() {
        return 0;
    }

    @Override
    public boolean isPrivate() {
        return false;
    }

    @Override
    public boolean isPackagePrivate() {
        return false;
    }

    @Override
    public boolean isProtected() {
        return false;
    }

    @Override
    public boolean isPublic() {
        return false;
    }

    @Override
    public VirtualMachine virtualMachine() {
        return null;
    }

    @Override
    public int compareTo(@NotNull Field o) {
        return 0;
    }
}
