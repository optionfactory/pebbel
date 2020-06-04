package net.optionfactory.pebbel.compiled;

import net.optionfactory.pebbel.loading.Function;
import net.optionfactory.pebbel.parsing.ast.Source;

import java.lang.reflect.Method;

public class MethodHolderFunction implements Function {
    private final Method method;

    public MethodHolderFunction(Method method) {
        this.method = method;
    }

    @Override
    public Method method() {
        return method;
    }

    @Override
    public Object perform(Source source, Object[] t) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
