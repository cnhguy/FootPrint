import com.intellij.debugger.engine.DebugProcess;
import com.intellij.debugger.engine.managerThread.DebuggerCommand;
import com.intellij.debugger.jdi.DecompiledLocalVariable;
import com.intellij.debugger.jdi.LocalVariablesUtil;
import com.intellij.debugger.jdi.StackFrameProxyImpl;
import com.intellij.openapi.util.text.StringUtil;
import com.sun.jdi.*;

import com.sun.tools.jdi.ArrayReferenceImpl;
import com.sun.tools.jdi.StringReferenceImpl;
import com.sun.jdi.event.ModificationWatchpointEvent;


import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Extracts variable information
 */
public class DebugExtractor implements DebuggerCommand {

    private StackFrameProxyImpl frameProxy;
    private DebugProcess debugProcess;
    private DebugCache cache;

    public DebugExtractor() {
        this(null, null);
    }

    public DebugExtractor(StackFrameProxyImpl frameProxy, DebugProcess debugProcess) {
        this.frameProxy = frameProxy;
        this.debugProcess = debugProcess;
        this.cache = DebugCache.getInstance();
    }

    @Override
    public void action() {
        try {
            //System.out.println("current line number: " + frameProxy.location());
            // used intelliJ's LocalVariablesUtil to fetch variables in the current stackframe
            Map<DecompiledLocalVariable, Value> map = LocalVariablesUtil.fetchValues(frameProxy, debugProcess, true);
            updateCache(map);
            System.out.println(cache);
            // for each local variable that we fetched, print out its value
            // map.forEach(((decompiledLocalVariable, value) -> printValue(decompiledLocalVariable, value)));

            System.out.println("---------------");
        } catch (Exception e) {
            e.printStackTrace();
        }
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
                    Value val = valueAsArray.getValue(i);
                    valueAsString += valueAsString(val) + ", ";
                }
                // append the last element without the comma
                if (length > 0) {
                    valueAsString += valueAsArray.getValue(length - 1);
                }
                valueAsString += "]";
            } else if (value instanceof ObjectReference && !(value instanceof StringReferenceImpl)) {
                // if value is an object that is not a string, call the objects toString()
                ObjectReference or = (ObjectReference) value;
                ReferenceType ref = or.referenceType();
                List<Method> methods = ref.methodsByName("toString");
                try {
                    Value toString = or.invokeMethod(frameProxy.threadProxy().getThreadReference(),
                            methods.get(0), Collections.EMPTY_LIST, ObjectReference.INVOKE_SINGLE_THREADED);
                    valueAsString = toString.toString();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return valueAsString;

    public void fieldUpdate(ModificationWatchpointEvent e) {
        System.out.println("process ModificationWatchpointEvent");
        Field field = e.field();
        Value value = e.valueToBe();
        cache.put(field.name(), e.location().lineNumber(), value);
        cache.pushChangeToUI();

    }

    private void updateCache(Map<DecompiledLocalVariable, Value> map) {
        map.forEach(((var, val) -> {
            try {
                String varName = StringUtil.join(var.getMatchedNames(), " | ");
                cache.put(varName, frameProxy.location().lineNumber(), valueAsString(val));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }));
        cache.pushChangeToUI();
    }

    @Override
    public void commandCancelled() {

    }

/*    private void printValue(DecompiledLocalVariable var, Value value) {
        System.out.println("-----");
        System.out.println("display name: " + var.getDisplayName());
        System.out.println("default name: " + var.getDefaultName());
        System.out.println("signature: " + var.getSignature());
        System.out.println("matched names: " + var.getMatchedNames());
        System.out.println("slot: " + var.getSlot());
        System.out.println("decompiled local variable toString: " + var.toString());




        String valueAsString = null;
        if (value != null) {
            valueAsString = value.toString();

            // If the value is an array, print it out in array format --> [x, y, z]
            if (value instanceof ArrayReferenceImpl)
            {
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

        System.out.println();
        String varName = StringUtil.join(var.getMatchedNames(), " | ");
       // String varName = var.getDisplayName().split(" ")[0];
       // String varName = var.getDisplayName();
        System.out.println(varName + ": " + valueAsString);
    }

//    static {
//        boolean success = false;
//        try {
//            ourSlotInfoClass = Class.forName("com.sun.tools.jdi.JDWP$StackFrame$GetValues$SlotInfo");
//            slotInfoConstructor = ourSlotInfoClass.getDeclaredConstructor(int.class, byte.class);
//            slotInfoConstructor.setAccessible(true);
//
//            ourGetValuesClass = Class.forName("com.sun.tools.jdi.JDWP$StackFrame$GetValues");
//            ourEnqueueMethod = findMethod(ourGetValuesClass, "enqueueCommand");
//            ourEnqueueMethod.setAccessible(true);
//            ourWaitForReplyMethod = findMethod(ourGetValuesClass, "waitForReply");
//            ourWaitForReplyMethod.setAccessible(true);
//
//            success = true;
//        }
//        catch (Throwable e) {
//            e.printStackTrace();
//        }
//        ourInitializationOk = success;
//    }
//
//    /**
//     * Extracts variable information when we hit a breakpoint
//     */
//    @Override
//    public void action() {
//        try {
//
//            // get a list of local variables from the current stack frame
//            StackFrame frame = sfProxy.getStackFrame(); // the stackframe of the line we are about to execute
//            List<LocalVariable> visibleVariables = frame.visibleVariables();
//
//            // Put visibleVariable's fields into DecompiledLocalVariables for formatting later on
//            // We can also write our own version of the DecompiledLocalVariables class
//            // to decide how the variables are displayed (?)
//            Collection<DecompiledLocalVariable> vars = new ArrayList<>();
//            int slot = 0;
//            for (LocalVariable visibleVariable : visibleVariables) {
//                List<String>  names = new ArrayList<>();
//                names.add(visibleVariable.name());
//                vars.add(new DecompiledLocalVariable(slot, visibleVariable.isArgument(),
//                        visibleVariable.signature(), names));
//                slot++;
//
//                System.out.println();
//                System.out.println("Local Variable");
//                System.out.println("name: " + visibleVariable.name()); // gives the name
//                System.out.println("signature: " + visibleVariable.signature()); // type initial
//                System.out.println("type: " + visibleVariable.type()); // gives detailed type. Ex: interface java.util.List (no class loader)
//                System.out.println("typeName: " + visibleVariable.typeName()); // the complete type name. Ex: java.util.List, java.lang.String, int[]
//                System.out.println("genericSignature: " + visibleVariable.genericSignature()); // returns null for primitive. Contains name, return type, parameters. Prob not needed
//                System.out.println("isArgument(): " + visibleVariable.isArgument()); // is it a parameter
//            }
//
//            Field frameIdField = frame.getClass().getDeclaredField("id");
//            frameIdField.setAccessible(true);
//            Object frameId = frameIdField.get(frame);
//
//            VirtualMachine vm = frame.virtualMachine();
//            Method stateMethod = vm.getClass().getDeclaredMethod("state");
//            stateMethod.setAccessible(true);
//
//            Object slotInfoArray = createSlotInfoArray(vars);
//
//            Object packetStream;
//            Object vmState = stateMethod.invoke(vm);
//            synchronized(vmState) {
//                packetStream = ourEnqueueMethod.invoke(null, vm, frame.thread(), frameId, slotInfoArray);
//            }
//
//            Object reply = ourWaitForReplyMethod.invoke(null, vm, packetStream);
//            Field valuesField = reply.getClass().getDeclaredField("values");
//            valuesField.setAccessible(true);
//
//            Value[] values = (Value[]) valuesField.get(reply);
//            if (vars.size() != values.length) {
//                throw new InternalException("Wrong number of values returned from target VM");
//            }
//
//            int idx = 0;
//            for (DecompiledLocalVariable var : vars) {
//                Value value = values[idx];
//                String valueAsString = null;
//                if (value != null) {
//                    valueAsString = value.toString();
//
//                    // If the value is an array, print it out in array format --> [x, y, z]
//                    if (value instanceof ArrayReferenceImpl)
//                    {
//                        ArrayReferenceImpl valueAsArray = (ArrayReferenceImpl) value;
//                        int length = valueAsArray.length();
//                        valueAsString = "[";
//                        for (int i = 0; i < length - 1; i++) {
//                            valueAsString += valueAsArray.getValue(i) + ", ";
//                        }
//
//                        // append the last element without the comma
//                        if (length > 0) {
//                            valueAsString += valueAsArray.getValue(length - 1);
//                        }
//                        valueAsString += "]";
//                    }
//
////                            else if (value instanceof ObjectReferenceImpl && var.getSignature().equals("Ljava/util/List;")) {
////                                ObjectReferenceImpl valueAsObject = (ObjectReferenceImpl) value;
////                                Type type = valueAsObject.type();
////                                ReferenceType referenceType = valueAsObject.referenceType();
////                                System.out.println("referencType to string: " + referenceType.classObject().toString());
////                                List<com.sun.jdi.Method> methods = referenceType.methodsByName("toString");
////                                System.out.println("methods: " + methods.toString());
////                                // get the fields. Might be useful for displaying complex objects later on
////                                List<com.sun.jdi.Field> fields = referenceType.allFields();
////                                System.out.println("fields: " + fields.toString());
////                                // List<Object> list = (List<Object>) value;
////
////                            }
//                }
//                System.out.println();
//                System.out.println(var.getDisplayName() + ":" + valueAsString);
//                //msg.append(var.getSlot() + ":" + var.getName() + ":" + valueType + ":" + valueAsString + ":" + var.getSignature() + "\n");
//
//                idx++;
//            }
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    @Override
//    public void commandCancelled() {
//
//    }
//
//    private static Method findMethod(Class aClass, String methodName) throws NoSuchMethodException {
//        for (Method method : aClass.getDeclaredMethods()) {
//            if (methodName.equals(method.getName())) {
//                return method;
//            }
//        }
//        throw new NoSuchMethodException(aClass.getName() + "." + methodName);
//    }
//
//    private static Object createSlotInfoArray(Collection<DecompiledLocalVariable> vars) throws Exception {
//        final Object arrayInstance = Array.newInstance(ourSlotInfoClass, vars.size());
//
//        int idx = 0;
//        for (DecompiledLocalVariable var : vars) {
//            final Object info = slotInfoConstructor.newInstance(var.getSlot(), (byte)var.getSignature().charAt(0));
//            Array.set(arrayInstance, idx++, info);
//        }
//
//        return arrayInstance;
//    }
}
