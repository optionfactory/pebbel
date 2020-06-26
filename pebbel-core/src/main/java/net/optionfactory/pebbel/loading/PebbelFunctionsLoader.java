package net.optionfactory.pebbel.loading;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.optionfactory.pebbel.loading.FunctionDescriptor.ParameterDescriptor;
import net.optionfactory.pebbel.results.Problem;
import net.optionfactory.pebbel.results.Result;

/**
 * Load
 */
public class PebbelFunctionsLoader<FUN> implements FunctionsLoader<FUN> {

    private final java.util.function.Function<Method, FUN> factory;

    public PebbelFunctionsLoader(java.util.function.Function<Method, FUN> factory) {
        this.factory = factory;
    }
    
    @Override
    public Result<Bindings<String, FUN, FunctionDescriptor>> load(Class<?>... classes) {
        final List<Problem> problems = new ArrayList<>();
        final Map<String, FUN> functions = new HashMap<>();
        final Map<String, FunctionDescriptor> schema = new HashMap<>();
        for (Class<?> c : classes) {
            for (final Method method : c.getDeclaredMethods()) {
                if (!method.isAnnotationPresent(BindingHandler.class)) {
                    continue;
                }
                final int modifiers = method.getModifiers();
                if (!Modifier.isStatic(modifiers)) {
                    problems.add(Problem.of("CANNOT_LOAD", String.format("@Handler '%s' is not static", method.getName()), null));
                    continue;
                }

                if (!Modifier.isPublic(modifiers)) {
                    problems.add(Problem.of("CANNOT_LOAD", String.format("@Handler '%s' is not public", method.getName()), null));
                    continue;
                }
                final BindingHandler handler = method.getAnnotation(BindingHandler.class);

                final ParameterDescriptor[] parameters = Arrays.stream(method.getParameters()).map(p -> ParameterDescriptor.of(p.getType(), p.getName())).toArray(l -> new ParameterDescriptor[l]);
                final Class<?> returnType = method.getReturnType();
                final String functionName = handler.value();
                final String help = method.isAnnotationPresent(BindingDoc.class) ? method.getAnnotation(BindingDoc.class).value() : "no help";
                final FunctionDescriptor fd = FunctionDescriptor.of(functionName, help, method.isVarArgs(), returnType, parameters);
                if (schema.containsKey(functionName)) {
                    problems.add(Problem.of("CANNOT_LOAD", String.format("binding for %s already present", handler.value()), null));
                    continue;
                }
                schema.put(functionName, fd);
                try {
                    functions.put(functionName, factory.apply(method));
                } catch (RuntimeException ex) {
                    problems.add(Problem.of("CANNOT_LOAD", String.format("illegal access on unreflect for %s", handler.value()), null));
                }
            }
        }
        return problems.isEmpty() ? Result.value(Bindings.root(functions, schema)) : Result.errors(problems);
    }

}
