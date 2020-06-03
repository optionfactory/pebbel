package net.optionfactory.pebbel.interpreted;

import net.optionfactory.pebbel.loading.Function;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.optionfactory.pebbel.loading.Function;
import net.optionfactory.pebbel.loading.LoadingException;
import net.optionfactory.pebbel.parsing.ast.Source;

public class MethodHandleFunction implements Function {

    private final Method method;
    private final MethodHandle handle;

    public MethodHandleFunction(Method method) {
        this.method = method;
        try {
            this.handle = MethodHandles.publicLookup().unreflect(method);
        } catch (IllegalAccessException ex) {
            throw new LoadingException(ex);
        }
    }

    @Override
    public Method method() {
        return method;
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
        return String.format("MethodHandleFunction(%s %s.%s(%s))", method.getReturnType(), method.getDeclaringClass().getSimpleName(), method.getName(), Arrays.toString(method.getParameterTypes()));
    }

}
