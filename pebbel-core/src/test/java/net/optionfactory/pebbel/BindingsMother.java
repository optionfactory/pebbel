package net.optionfactory.pebbel;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import net.optionfactory.pebbel.loading.Bindings;
import net.optionfactory.pebbel.loading.Descriptors;
import net.optionfactory.pebbel.loading.Function;
import net.optionfactory.pebbel.loading.FunctionDescriptor;
import net.optionfactory.pebbel.loading.FunctionDescriptor.ParameterDescriptor;
import net.optionfactory.pebbel.loading.Symbols;
import net.optionfactory.pebbel.loading.VariableDescriptor;
import net.optionfactory.pebbel.parsing.ast.Source;

public class BindingsMother {

    private static final Map<String, Function> FUNCTION_HANDLERS = new HashMap<>();
    private static final Map<String, FunctionDescriptor> FUNCTION_DESCRIPTORS = new HashMap<>();

    public static class TestFunction<T> implements Function {

        private final BiFunction<Source, Object[], T> inner;
        private final Method method;

        public TestFunction(BiFunction<Source, Object[], T> inner) {
            this.inner = inner;
            try {
                this.method = BiFunction.class.getMethod("apply", Object.class, Object.class);
            } catch (NoSuchMethodException | SecurityException ex) {
                throw new RuntimeException(ex);
            }
        }

        @Override
        public Method method() {
            return method;
        }

        @Override
        public T perform(Source source, Object[] t) {
            return inner.apply(source, t);
        }

    }

    static {
        FUNCTION_HANDLERS.put("true", new TestFunction<>((s, vs) -> Boolean.TRUE));
        FUNCTION_DESCRIPTORS.put("true", FunctionDescriptor.of("true", "no help", false, Boolean.class));
        FUNCTION_HANDLERS.put("false", new TestFunction<>((s, vs) -> Boolean.FALSE));
        FUNCTION_DESCRIPTORS.put("false", FunctionDescriptor.of("false", "no help", false, Boolean.class));
        FUNCTION_HANDLERS.put("bool", new TestFunction<>((s, vs) -> Boolean.parseBoolean(vs[0].toString())));
        FUNCTION_DESCRIPTORS.put("bool", FunctionDescriptor.of("bool", "no help", false, Boolean.class, ParameterDescriptor.of(Object.class, "value")));
        FUNCTION_HANDLERS.put("not", new TestFunction<>((s, vs) -> !(Boolean) vs[0]));
        FUNCTION_DESCRIPTORS.put("not", FunctionDescriptor.of("bool", "no help", false, Boolean.class, ParameterDescriptor.of(Boolean.class, "value")));
    }

    private static final Map<String, Object> VARIABLE_VALUES = new HashMap<>();
    private static final Map<String, VariableDescriptor<Object>> VARIABLE_DESCRIPTORS = new HashMap<>();

    static {
        VARIABLE_VALUES.put("VAR1", new Object());
        VARIABLE_DESCRIPTORS.put("VAR1", VariableDescriptor.of("VAR1", Object.class, 0));
    }

    private static final Bindings<String, Function, FunctionDescriptor> FUNCTIONS = Bindings.root(FUNCTION_HANDLERS, FUNCTION_DESCRIPTORS);
    private static final Bindings<String, Object, VariableDescriptor<Object>> VARS = Bindings.root(VARIABLE_VALUES, VARIABLE_DESCRIPTORS);

    public static final Symbols<Object, Object> SYMBOLS = Symbols.of(FUNCTIONS, VARS);
    public static final Descriptors<Object> DESCRIPTORS = Descriptors.from(SYMBOLS);

}
