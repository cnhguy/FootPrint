import com.intellij.debugger.engine.DebugProcess;
import com.intellij.debugger.engine.evaluation.EvaluateException;
import com.intellij.debugger.engine.managerThread.DebuggerCommand;
import com.intellij.debugger.jdi.StackFrameProxyImpl;

import com.sun.jdi.*;
import com.sun.tools.jdi.ArrayReferenceImpl;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Extracts variable information
 */
public class DebugExtractor implements DebuggerCommand {

    private StackFrameProxyImpl frameProxy;
    private DebugProcess debugProcess;
    private static DebugCache cache;

    private DebugListener debugListener;

    /**
     * Initializes with null frame proxy, null debug process, and the cache
     */
    public DebugExtractor() {
        this(null, null, null);
    }

    /**
     * Creates a DebugExtractor
     *
     * @param frameProxy stack frame proxy
     * @param debugProcess debugger process
     */
    public DebugExtractor(StackFrameProxyImpl frameProxy, DebugProcess debugProcess, DebugListener debugListener) {
        this.frameProxy = frameProxy;
        this.debugProcess = debugProcess;
        this.cache = DebugCache.getInstance();
        this.debugListener = debugListener;
    }

    /**
     * Callback for a DebuggerCommand. Gets all visible local variables and sends them to the cache.
     */
    @Override
    public void action() {
        extractFields();
        extractLocalVariables();

        // TODO: Remove
        System.out.println(cache);
        System.out.println("---------------");
    }

    /**
     * Extract and cache local variables on the stackframe
     */
    private void extractLocalVariables() {
        try {
            StackFrame frame = frameProxy.getStackFrame();
            List<LocalVariable> localVariables = frame.visibleVariables();
            Map<LocalVariable, Value> map = frame.getValues(localVariables);
            updateCache(map);
        } catch (AbsentInformationException e) {
            e.printStackTrace();
        } catch (EvaluateException e) {
            e.printStackTrace();
        }
    }



    /**
     * Extract and cache fields visible to the debugger
     */
    private void extractFields() {
        try {
            StackFrame frame = frameProxy.getStackFrame();
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
        } catch (EvaluateException e) {
            e.printStackTrace();
        }
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
                valueAsString = getValueFromArrayReference((ArrayReferenceImpl) value);
            } else if (value instanceof ObjectReference) {
                valueAsString = getValueFromObjectReference((ObjectReference) value);
            }
        }
        return valueAsString;
    }

    /**
     * Get the object reference's value. If the object belongs to java.*, returns
     * the toString() result; otherwise represent it as its fields
     * @param object object to extract
     * @return object's string representation
     */
    private String getValueFromObjectReference(ObjectReference object) {
        ReferenceType referenceType = object.referenceType();

        if (referenceType.toString().contains("java.")) {
            // if the object is of java.*, invoke the toString() method
            return invokeToString(object);
        } else {
            // else, represent it as its fields
            return getFieldsAsString(object);
        }
    }

    /**
     * Returns the fields within an object as a string
     * @param object object whose field to extract
     * @return the fields within an object
     */
    private String getFieldsAsString(ObjectReference object) {
        String fieldsAsString = "";
        ReferenceType referenceType = object.referenceType();
        List<Field> fieldList = referenceType.visibleFields();
        Map<Field, Value> fieldMap = object.getValues(fieldList);

        for (Map.Entry<Field, Value> entry : fieldMap.entrySet()) {
            Field field = entry.getKey();
            Value val = entry.getValue();
            fieldsAsString += "\n" + field.name() + ": " + valueAsString(val) + "     ";
        }
        return fieldsAsString;
    }

    /**
     * Invoke the toString() method of an object
     * @param object Object we want to extract
     * @return object's toString() result
     */
    private String invokeToString(ObjectReference object) {
        try {
            ReferenceType referenceType = object.referenceType();
            List<Method> methods = referenceType.methodsByName("toString");

            // find the toString() method with no argument
            Method toStringMethod = null;
            for (Method m : methods) {
                if (m.argumentTypeNames().size() == 0) {
                    toStringMethod = m;
                    break;
                }
            }

            ThreadReference threadRef = frameProxy.threadProxy().getThreadReference();
            Value toString = object.invokeMethod(threadRef, toStringMethod,
                    Collections.EMPTY_LIST, ObjectReference.INVOKE_SINGLE_THREADED);
            return trimQuotes(toString.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * Returns an array as a string
     * @param value the array
     * @return array as a string
     */
    @NotNull
    private String getValueFromArrayReference(ArrayReferenceImpl value) {
        String valueAsString;
        ArrayReferenceImpl valueAsArray = value;
        valueAsString = "[";
        List<String> arrayValues =
                valueAsArray.getValues()
                        .stream()
                        .map((Value v) -> valueAsString(v))
                        .collect(Collectors.toList());
        valueAsString += String.join(", ", arrayValues);
        valueAsString += "]";
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
     * Trim quotes at the beginning and end
     * @param string string to strip
     * @return non-quoted string
     */
    private String trimQuotes(String string) {
        if (string.length() >= 2 && string.charAt(0) == '"' && string.charAt(string.length() - 1) == '"') {
            string = string.substring(1, string.length() - 1);
        }
        return string;
    }

    /**
     * Callback for DebuggerCommand
     */
    @Override
    public void commandCancelled() {

    }
}