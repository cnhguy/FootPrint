import com.intellij.debugger.engine.DebugProcessImpl;
import com.intellij.debugger.engine.SuspendContextImpl;
import com.intellij.debugger.engine.SuspendManager;
import com.intellij.debugger.engine.evaluation.EvaluateException;
import com.intellij.debugger.engine.managerThread.DebuggerCommand;
import com.intellij.debugger.jdi.StackFrameProxyImpl;
import com.intellij.ide.ui.EditorOptionsTopHitProvider;
import com.intellij.openapi.util.Key;
import com.sun.jdi.*;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.EventIterator;
import com.sun.jdi.event.EventSet;
import com.sun.jdi.request.BreakpointRequest;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.EventRequestManager;
import com.sun.jdi.request.StepRequest;
import com.sun.tools.jdi.ArrayReferenceImpl;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Extracts variable information
 */
public class DebugExtractor implements DebuggerCommand {

    private StackFrameProxyImpl frameProxy;
    private DebugProcessImpl debugProcess;
    private SuspendContextImpl suspendContext;
    private List<DebugListener.StepInfo> steps;

    private static MasterCache cache;

    /**
     * Creates a DebugExtractor
     *
     * @param frameProxy stack frame proxy
     * @param suspendContext suspend context
     * @param steps list of step requests and their original locations
     */
    public DebugExtractor(StackFrameProxyImpl frameProxy,
                          SuspendContextImpl suspendContext,
                          List<DebugListener.StepInfo> steps) {
        this.frameProxy = frameProxy;
        this.debugProcess = suspendContext.getDebugProcess();
        this.suspendContext = suspendContext;
        this.steps = steps;

        this.cache = MasterCache.getInstance();
    }

    /**
     * Callback for a DebuggerCommand. Gets all visible local variables and sends them to the cache.
     */
    @Override
    public void action() {
        extractFields();
        extractLocalVariables();
        resumeIfOnlyRequestor();
    }

    /**
     * Check if we are the only ones requesting to stop
     * If yes, then resume the execution. If not then wait
     */
    private void resumeIfOnlyRequestor() {
        SuspendManager suspendManager = debugProcess.getSuspendManager();
        boolean isOnlyEventRequest = true;
        // this loop detects:
        // -breakpoint requests
        // -step requests without method calls
        // -step requests into method calls
        outer:
        for (SuspendContextImpl context : suspendManager.getEventContexts()) {
            System.out.println(context);
            EventSet events = context.getEventSet();
            EventIterator eventIterator = events.eventIterator();
            while (eventIterator.hasNext()) {
                Event e = eventIterator.nextEvent();
                EventRequest request = e.request();
                if (request instanceof BreakpointRequest) {
                    Object o = request.getProperty(Key.findKeyByName("Requestor"));
                    if (o != null && o instanceof DebugListener) {
                        continue;
                    }
                }
                isOnlyEventRequest = false;
                break outer;
            }
        }
        // check if a step request is met
        Iterator<DebugListener.StepInfo> iterator = steps.iterator();
        while (iterator.hasNext()) {
            DebugListener.StepInfo stepInfo = iterator.next();
            try {
                if (stepRequestMet(stepInfo, suspendContext.getFrameProxy().location())) {
                    isOnlyEventRequest = false;
                    iterator.remove();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (isOnlyEventRequest)
            suspendManager.resume(suspendContext);
    }

    private boolean stepRequestMet(DebugListener.StepInfo stepInfo, Location curLocation) {
        StepRequest stepRequest = stepInfo.stepRequest;
        switch (stepRequest.depth()) {
            case StepRequest.STEP_INTO:
                return true;
            case StepRequest.STEP_OUT:
                if (!stepInfo.originalLocation.method().equals(curLocation.method()))
                    return true;
                return false;
            case StepRequest.STEP_OVER:
                if (stepInfo.originalLocation.method().equals(curLocation.method()))
                    return true;
                return false;
            default:
                return false;
        }
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
            String objectId = getObjectId();

            if (thisObject != null) {
                // if the frame is in an object
                ReferenceType referenceType = thisObject.referenceType();
                List<Field> fieldList = referenceType.visibleFields();
                Map<Field, Value> fieldMap = thisObject.getValues(fieldList);


                for (Map.Entry<Field, Value> entry : fieldMap.entrySet()) {
                    Field field = entry.getKey();
                    Value val = entry.getValue();
                    VariableInfo info = new VariableInfo(frameProxy.location().lineNumber(), valueAsString(val));
                    cache.put(objectId, field, info);
                }
            } else {
                // if the frame is in a native or static method
                ReferenceType referenceType = frameProxy.location().declaringType();
                List<Field> fields = referenceType.fields();
                for (Field field : fields) {

                    Value value = referenceType.getValue(field);
                    VariableInfo info = new VariableInfo(frameProxy.location().lineNumber(), valueAsString(value));

                    cache.put(objectId, field, info);
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
                valueAsString = invokeToString((ObjectReference) value);
            }
        }
        return valueAsString;
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

            List<BreakpointRequest> toStringBreakpoints = getBreakPoints(threadRef.virtualMachine());
            // disable all breakpoints within toString method(s) so the thread can execute
            disableBreakPoints(toStringBreakpoints);

            Value toString = object.invokeMethod(threadRef, toStringMethod,
                    Collections.EMPTY_LIST, ObjectReference.INVOKE_SINGLE_THREADED);

            // restore breakpoints
            enableBreakPoints(toStringBreakpoints);

            return trimQuotes(toString.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * Get all breakpoints within toString() method(s)
     * @param vm virtual machine
     * @return all breakpoints within toString method(s)
     */
    private List<BreakpointRequest> getBreakPoints(VirtualMachine vm) {
        List<BreakpointRequest> requests = new ArrayList<>();
        EventRequestManager manager = vm.eventRequestManager();
        List<BreakpointRequest> breakpointRequests = manager.breakpointRequests();
        for (BreakpointRequest breakPoint : breakpointRequests) {
            if (breakPoint.location().method().name().contains("toString")) {
                requests.add(breakPoint);
            }
        }
        return requests;
    }

    /**
     * Enable breakpoint requests
     * @param breakpointRequests breakpoint requests
     */
    private void enableBreakPoints(List<BreakpointRequest> breakpointRequests) {
        for (BreakpointRequest bp : breakpointRequests) {
            bp.enable();
        }
    }

    /**
     * Disable breakpoint requests
     * @param breakpointRequests breakpoint requests
     */
    private void disableBreakPoints(List<BreakpointRequest> breakpointRequests) {
        for (BreakpointRequest bp : breakpointRequests) {
            bp.disable();
        }
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
                VariableInfo info = new VariableInfo(frameProxy.location().lineNumber(), valueAsString(val));
                String objectId = getObjectId();
                Method method = frameProxy.location().method();
                cache.put(objectId, method, var, info);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }));
    }

    private String getObjectId() {
        try {
            StackFrame frame = frameProxy.getStackFrame();
            ObjectReference thisObject = frame.thisObject();
            if (thisObject != null) {
                return thisObject.referenceType().name() + "(id=" + thisObject.uniqueID() + ")";
            } else {
                // if the frame is in a native or static method
                ReferenceType referenceType = frameProxy.location().declaringType();
                return referenceType.name();
            }
        } catch (EvaluateException e) {
            e.printStackTrace();
        }
        return null;
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