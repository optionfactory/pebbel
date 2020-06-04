package net.optionfactory.pebbel.compiled;

import net.optionfactory.pebbel.loading.Bindings;
import net.optionfactory.pebbel.loading.VariableDescriptor;

public interface CompiledExpression<VAR_TYPE, VAR_METADATA_TYPE, R> {
    <R> R evaluate(Bindings<String, VAR_TYPE, VariableDescriptor<VAR_METADATA_TYPE>> varBindings);
}
