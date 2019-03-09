import com.sun.jdi.*;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class TestMethod implements Method {
    private String s;

    public TestMethod(String s) {
        this.s = s;
    }
    @Override
    public String returnTypeName() {
        return null;
    }

    @Override
    public Type returnType() throws ClassNotLoadedException {
        return null;
    }

    @Override
    public List<String> argumentTypeNames() {
        return null;
    }

    @Override
    public List<Type> argumentTypes() throws ClassNotLoadedException {
        return null;
    }

    @Override
    public boolean isAbstract() {
        return false;
    }

    @Override
    public boolean isSynchronized() {
        return false;
    }

    @Override
    public boolean isNative() {
        return false;
    }

    @Override
    public boolean isVarArgs() {
        return false;
    }

    @Override
    public boolean isBridge() {
        return false;
    }

    @Override
    public boolean isConstructor() {
        return false;
    }

    @Override
    public boolean isStaticInitializer() {
        return false;
    }

    @Override
    public boolean isObsolete() {
        return false;
    }

    @Override
    public List<Location> allLineLocations() throws AbsentInformationException {
        return null;
    }

    @Override
    public List<Location> allLineLocations(String s, String s1) throws AbsentInformationException {
        return null;
    }

    @Override
    public List<Location> locationsOfLine(int i) throws AbsentInformationException {
        return null;
    }

    @Override
    public List<Location> locationsOfLine(String s, String s1, int i) throws AbsentInformationException {
        return null;
    }

    @Override
    public Location locationOfCodeIndex(long l) {
        return null;
    }

    @Override
    public List<LocalVariable> variables() throws AbsentInformationException {
        return null;
    }

    @Override
    public List<LocalVariable> variablesByName(String s) throws AbsentInformationException {
        return null;
    }

    @Override
    public List<LocalVariable> arguments() throws AbsentInformationException {
        return null;
    }

    @Override
    public byte[] bytecodes() {
        return new byte[0];
    }

    @Override
    public Location location() {
        return null;
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
    public int compareTo(@NotNull Method o) {
        return 0;
    }
}
