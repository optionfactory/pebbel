package net.optionfactory.pebbel;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import net.optionfactory.pebbel.loading.Bindings;
import net.optionfactory.pebbel.loading.Descriptors;
import net.optionfactory.pebbel.loading.FunctionDescriptor;
import net.optionfactory.pebbel.loading.FunctionDescriptor.ParameterDescriptor;
import net.optionfactory.pebbel.loading.LoadingException;
import net.optionfactory.pebbel.loading.VariableDescriptor;

public class BindingsMother {


    private static final Map<String, MethodHandle> FUNCTION_HANDLERS = new HashMap<>();
    private static final Map<String, FunctionDescriptor> FUNCTION_DESCRIPTORS = new HashMap<>();
    private static final Map<String, VariableDescriptor<Object>> VARIABLE_DESCRIPTORS = new HashMap<>();

    public static final Descriptors<Object,MethodHandle> DESCRIPTORS = new Descriptors<>();
    
    static {
        FUNCTION_HANDLERS.put("true", unreflect("makeTrue"));
        FUNCTION_DESCRIPTORS.put("true", FunctionDescriptor.of("true", "no help", false, Boolean.class));
        FUNCTION_HANDLERS.put("false", unreflect("makeFalse"));
        FUNCTION_DESCRIPTORS.put("false", FunctionDescriptor.of("false", "no help", false, Boolean.class));
        FUNCTION_HANDLERS.put("bool", unreflect("makeBool"));
        FUNCTION_DESCRIPTORS.put("bool", FunctionDescriptor.of("bool", "no help", false, Boolean.class, ParameterDescriptor.of(Object.class, "value")));
        FUNCTION_HANDLERS.put("not", unreflect("makeNot"));
        FUNCTION_DESCRIPTORS.put("not", FunctionDescriptor.of("bool", "no help", false, Boolean.class, ParameterDescriptor.of(Boolean.class, "value")));

        VARIABLE_DESCRIPTORS.put("VAR1", VariableDescriptor.of("VAR1", Object.class, 0));
        DESCRIPTORS.functions = Bindings.root(FUNCTION_HANDLERS, FUNCTION_DESCRIPTORS);
        DESCRIPTORS.variables = VARIABLE_DESCRIPTORS;

    }

    public static Boolean makeTrue() {
        return true;
    }

    public static Boolean makeFalse() {
        return false;
    }

    public static boolean makeBool(Object value) {
        return Boolean.parseBoolean(value.toString());
    }

    public static boolean makeNot(Boolean value) {
        return !value;
    }

    private static MethodHandle unreflect(String name) {
        try {
            final Method m = Stream.of(BindingsMother.class.getMethods()).filter(method -> method.getName().equals(name)).findFirst().get();
            return MethodHandles.publicLookup().unreflect(m);
        } catch (SecurityException | IllegalAccessException ex) {
            throw new LoadingException(ex);
        }
    }
}
