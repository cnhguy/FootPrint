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
        System.out.println("process ModificationWatchpointEvent");
        Field field = e.field();
        Value value = e.valueToBe();
        cache.put(field, e.location().lineNumber(), valueAsString(value));
        cache.pushChangeToUI();
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
                    valueAsString += valueAsArray.getValue(length - 1);
                }
                valueAsString += "]";
            } else if (value instanceof ObjectReference) {
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
    }

    /**
     * Helper method to easily send local varaibles to the cache.
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