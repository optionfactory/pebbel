package net.optionfactory.pebbel.execution;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.Arrays;
import net.optionfactory.pebbel.ast.Source;

public class MethodHandleFunction implements Function {

    final Method method;
    final MethodHandle handle;

    public MethodHandleFunction(Method method) throws IllegalAccessException {
        this.method = method;
        this.handle = MethodHandles.publicLookup().unreflect(method);
    }

    @Override
    public Object perform(Source source, Object[] t) {
        try {
            return handle.invokeWithArguments(t);
        } catch (Throwable ex) {
            throw new ExecutionException(ex.getMessage(), source, ex);
        }
    }

    @Override
    public String toString() {
        return String.format("BuiltinFunctionHandler(%s %s.%s(%s))", method.getReturnType(), method.getDeclaringClass().getSimpleName(), method.getName(), Arrays.toString(method.getParameterTypes()));
    }

}
