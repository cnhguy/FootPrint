import com.intellij.debugger.engine.managerThread.DebuggerCommand;
import com.intellij.debugger.jdi.DecompiledLocalVariable;
import com.intellij.debugger.jdi.StackFrameProxyImpl;
import com.sun.jdi.*;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Extracts variable information
 */
public class DebugExtractor implements DebuggerCommand {

    private static boolean ourInitializationOk;
    private static Class<?> ourSlotInfoClass;
    private static Constructor<?> slotInfoConstructor;
    private static Class<?> ourGetValuesClass;
    private static Method ourEnqueueMethod;
    private static Method ourWaitForReplyMethod;
    private StackFrameProxyImpl sfProxy;

    public DebugExtractor(StackFrameProxyImpl sfProxy) {
        this.sfProxy = sfProxy;
    }

    static {
        boolean success = false;
        try {
            ourSlotInfoClass = Class.forName("com.sun.tools.jdi.JDWP$StackFrame$GetValues$SlotInfo");
            slotInfoConstructor = ourSlotInfoClass.getDeclaredConstructor(int.class, byte.class);
            slotInfoConstructor.setAccessible(true);

            ourGetValuesClass = Class.forName("com.sun.tools.jdi.JDWP$StackFrame$GetValues");
            ourEnqueueMethod = findMethod(ourGetValuesClass, "enqueueCommand");
            ourEnqueueMethod.setAccessible(true);
            ourWaitForReplyMethod = findMethod(ourGetValuesClass, "waitForReply");
            ourWaitForReplyMethod.setAccessible(true);

            success = true;
        }
        catch (Throwable e) {
            e.printStackTrace();
        }
        ourInitializationOk = success;
    }

    /**
     * Extracts variable information when we hit a breakpoint
     */
    @Override
    public void action() {
        try {
            // get a list of local variables from the current stack frame
            StackFrame frame = sfProxy.getStackFrame();
            List<LocalVariable> visibleVariables = frame.visibleVariables();

            // Put visibleVariable's fields into DecompiledLocalVariables for formatting later on
            // We can also write our own version of the DecompiledLocalVariables class
            // to decide how the variables are displayed
            Collection<DecompiledLocalVariable> vars = new ArrayList<>();
            int slot = 0;
            for (LocalVariable visibleVariable : visibleVariables) {
                List<String>  names = new ArrayList<>();
                names.add(visibleVariable.name());
                vars.add(new DecompiledLocalVariable(slot, visibleVariable.isArgument(),
                        visibleVariable.signature(), names));
                slot++;

//                System.out.println();
//                System.out.println("Local Variable");
//                System.out.println("name: " + visibleVariable.name());
//                System.out.println("signature: " + visibleVariable.signature());
//                System.out.println("type: " + visibleVariable.type());
//                System.out.println("typeName: " + visibleVariable.typeName());
//                System.out.println("genericSignature: " + visibleVariable.genericSignature());
//                System.out.println("isArgument(): " + visibleVariable.isArgument());
            }

//                                    vars.forEach(d -> System.out.println(d.getDefaultName()));

            Field frameIdField = frame.getClass().getDeclaredField("id");
            frameIdField.setAccessible(true);
            Object frameId = frameIdField.get(frame);

            VirtualMachine vm = frame.virtualMachine();
            Method stateMethod = vm.getClass().getDeclaredMethod("state");
            stateMethod.setAccessible(true);

            Object slotInfoArray = createSlotInfoArray(vars);

            Object packetStream;
            Object vmState = stateMethod.invoke(vm);
            synchronized(vmState) {
                packetStream = ourEnqueueMethod.invoke(null, vm, frame.thread(), frameId, slotInfoArray);
            }

            Object reply = ourWaitForReplyMethod.invoke(null, vm, packetStream);
            Field valuesField = reply.getClass().getDeclaredField("values");
            valuesField.setAccessible(true);

            Value[] values = (Value[]) valuesField.get(reply);
            if (vars.size() != values.length) {
                throw new InternalException("Wrong number of values returned from target VM");
            }

            int idx = 0;
            for (DecompiledLocalVariable var : vars) {
                Value value = values[idx];
                String valueAsString = null;
                //Type valueType = (null == value) ? null : value.type();
                if (value != null) {
                    // Commented code is trying to display arrays as [v, v, v] format
                    valueAsString = value.toString();
//                                            if (value instanceof ArrayReferenceImpl & var.getSignature().equals("[Ljava/lang/String;"))
//                                            {
//                                                ArrayReferenceImpl valueAsArray = (ArrayReferenceImpl)value;
//                                                List<StringReferenceImpl> arrayValues = (List<StringReferenceImpl>) valueAsArray.getValues();
//                                                valueAsString = "[";
//                                                for (StringReferenceImpl arrayValue:arrayValues) {
//                                                    valueAsString += arrayValue + ",";
//                                                }
//                                                if (arrayValues.size() > 0) { // remove last comma
//                                                    valueAsString = valueAsString.substring(0, valueAsString.length() - 1);
//                                                }
//                                                valueAsString += "]";
//                                            }
                }
                System.out.println(var.getDisplayName() + ":" + valueAsString);
                //msg.append(var.getSlot() + ":" + var.getName() + ":" + valueType + ":" + valueAsString + ":" + var.getSignature() + "\n");

                idx++;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void commandCancelled() {

    }

    private static Method findMethod(Class aClass, String methodName) throws NoSuchMethodException {
        for (Method method : aClass.getDeclaredMethods()) {
            if (methodName.equals(method.getName())) {
                return method;
            }
        }
        throw new NoSuchMethodException(aClass.getName() + "." + methodName);
    }

    private static Object createSlotInfoArray(Collection<DecompiledLocalVariable> vars) throws Exception {
        final Object arrayInstance = Array.newInstance(ourSlotInfoClass, vars.size());

        int idx = 0;
        for (DecompiledLocalVariable var : vars) {
            final Object info = slotInfoConstructor.newInstance(var.getSlot(), (byte)var.getSignature().charAt(0));
            Array.set(arrayInstance, idx++, info);
        }

        return arrayInstance;
    }
}
