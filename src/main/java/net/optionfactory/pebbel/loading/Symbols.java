package net.optionfactory.pebbel.loading;

import net.optionfactory.pebbel.execution.Function;

public class Symbols<VAR_TYPE, VAR_METADATA_TYPE> {

    public Bindings<String, Function, FunctionDescriptor> functions;
    public Bindings<String, VAR_TYPE, VariableDescriptor<VAR_METADATA_TYPE>> variables;

    public static <VAR_TYPE, VAR_METADATA_TYPE> Symbols<VAR_TYPE, VAR_METADATA_TYPE> of(
            Bindings<String, Function, FunctionDescriptor> functions,
            Bindings<String, VAR_TYPE, VariableDescriptor<VAR_METADATA_TYPE>> variables) {
        final Symbols<VAR_TYPE, VAR_METADATA_TYPE> s = new Symbols<>();
        s.functions = functions;
        s.variables = variables;
        return s;
    }

}
