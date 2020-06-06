package net.optionfactory.pebbel.compiled;

import java.lang.reflect.InvocationTargetException;

public class DynamicLoader extends ClassLoader implements CompiledExpression.Loader {

    @Override
    public <R> CompiledExpression<R> load(CompiledExpression.Unloaded<R> unloaded) {
        try {
            @SuppressWarnings("unchecked")
            final Class<CompiledExpression<R>> clazz = (Class<CompiledExpression<R>>) defineClass(unloaded.name, unloaded.bytecode, 0, unloaded.bytecode.length);
            return clazz.getConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
}
