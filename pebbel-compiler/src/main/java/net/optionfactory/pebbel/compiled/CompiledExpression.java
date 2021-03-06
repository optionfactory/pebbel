package net.optionfactory.pebbel.compiled;

import net.optionfactory.pebbel.loading.Bindings;
import net.optionfactory.pebbel.loading.VariableDescriptor;

public interface CompiledExpression<R> {

    <VAR, VARMETA> R evaluate(Bindings<String, VAR, VariableDescriptor<VARMETA>> varBindings);

    public static class Unloaded<R> {

        public final String name;
        public final byte[] bytecode;

        public Unloaded(String name, byte[] bytecode) {
            this.name = name;
            this.bytecode = bytecode;

        }
    }

    public interface Loader {

        <R> CompiledExpression<R> load(Unloaded<R> unloaded);
    }

}
