import com.intellij.debugger.engine.DebugProcess;
import com.intellij.debugger.engine.managerThread.DebuggerCommand;
import com.intellij.debugger.jdi.StackFrameProxyImpl;

import com.sun.jdi.*;
import com.sun.tools.jdi.ArrayReferenceImpl;
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

    /**
     * Initializes with null frame proxy, null debug process, and the cache
     */
    public DebugExtractor() {
        this(null, null);
    }

    /**
     * Creates a DebugExtractor
     *
     * @param frameProxy stack frame proxy
     * @param debugProcess debugger process
     */
    public DebugExtractor(StackFrameProxyImpl frameProxy, DebugProcess debugProcess) {
        this.frameProxy = frameProxy;
        this.debugProcess = debugProcess;
        this.cache = DebugCache.getInstance();
    }

    /**
     * Callback for a DebuggerCommand. Gets all visible local variables and sends them to the cache.
     */
    @Override
    public void action() {
        try {
            StackFrame frame = frameProxy.getStackFrame();

            // Retrieve and cache fields of the current frame
            ObjectReference thisObject = frame.thisObject();
            if (thisObject != null) {
                // if the frame is in an object
                ReferenceType referenceType = thisObject.referenceType();
                List<Field> fieldList = referenceType.visibleFields();
                Map<Field, Value> fieldMap = thisObject.getValues(fieldList);

                for (Map.Entry<Field, Value> entry : fieldMap.entrySet()) {
                    Field field = entry.getKey();
                    Value val = entry.getValue();
                    cache.put(field, frameProxy.location().lineNumber(), valueAsString(val));
                }
            } else {
                // if the frame is in a native or static method
                ReferenceType referenceType = frameProxy.location().declaringType();
                List<Field> fields = referenceType.fields();
                for (Field field : fields) {

                    Value value = referenceType.getValue(field);
                    cache.put(field, frame.location().lineNumber(), valueAsString(value));
                }

            }


            // Retrieve and cache all local var on the frame
            List<LocalVariable> localVariables = frame.visibleVariables();
            Map<LocalVariable, Value> map = frame.getValues(localVariables);
            updateCache(map);

            System.out.println(cache);
            System.out.println("---------------");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Used for ModificationWatchPointEvents. Gets the field name, value, and line number and sends to the cache.
     *
     * @param e watchpoint event
     */
    public void fieldUpdate(ModificationWatchpointEvent e) {

//        System.out.println("process ModificationWatchpointEvent");
//        Field field = e.field();
//        Value value = e.valueToBe();
//        cache.put(field, e.location().lineNumber(), valueAsString(value));
//        cache.pushChangeToUI();
    }

    /**
     * Returns value as a string
     * @param value variable or field's value
     * @return value as a string
     */
    private String valueAsString(Value value) {
        String valueAsString = "null";
        if (value != null) {
            valueAsString = value.toString();
            if (value instanceof StringReference) {
                // if value is a string, just use it
                valueAsString = ((StringReference) value).value();
            } else if (value instanceof ArrayReferenceImpl) {
                // If the value is an array, print it out in array format --> [x, y, z]
                ArrayReferenceImpl valueAsArray = (ArrayReferenceImpl) value;
                int length = valueAsArray.length();
                valueAsString = "[";
                for (int i = 0; i < length - 1; i++) {
                    Value val = valueAsArray.getValue(i);
                    valueAsString += valueAsString(val) + ", ";
                }
                // append the last element without the comma
                if (length > 0) {
                    Value val = valueAsArray.getValue(length - 1);
                    valueAsString += valueAsString(val);
                }
                valueAsString += "]";
            } else if (value instanceof ObjectReference) {
                ObjectReference valueAsObject = (ObjectReference) value;
                ReferenceType referenceType = valueAsObject.referenceType();

                valueAsString = "";


                if (referenceType.toString().contains("java.")) {
                    // if the object is of java.*, invoke the toString() method
                    List<Method> methods = referenceType.methodsByName("toString");

                    // find the default toString() method
                    Method toStringMethod = null;
                    for (Method m : methods) {
                        if (m.argumentTypeNames().size() == 0) {
                            toStringMethod = m;
                            break;
                        }
                    }

                    try {
                        Value toString = valueAsObject.invokeMethod(frameProxy.threadProxy().getThreadReference(),
                                toStringMethod, Collections.EMPTY_LIST, ObjectReference.INVOKE_SINGLE_THREADED);
                        valueAsString = toString.toString();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    // else, represent it as its fields
                    List<Field> fieldList = referenceType.visibleFields();
                    Map<Field, Value> fieldMap = valueAsObject.getValues(fieldList);

                    for (Map.Entry<Field, Value> entry : fieldMap.entrySet()) {
                        Field field = entry.getKey();
                        Value val = entry.getValue();
                        valueAsString += "\n" + field.name() + ": " + val + "     ";
                    }
                }

            }
        }
        return valueAsString;
    }

    /**
     * Helper method to easily send local variables to the cache.
     *
     * @param map map of local variables and their values on the stackframe
     */
    private void updateCache(Map<LocalVariable, Value> map) {
        map.forEach(((var, val) -> {
            try {
                cache.put(var, frameProxy.location().lineNumber(), valueAsString(val));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }));
        cache.pushChangeToUI();
    }

    /**
     * Callback for DebuggerCommand
     */
    @Override
    public void commandCancelled() {

    }
}