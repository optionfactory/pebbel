package net.optionfactory.pebbel.loading;

public class Symbols<VAR_TYPE, VAR_METADATA_TYPE, FUN_TYPE> {

    public Bindings<String, FUN_TYPE, FunctionDescriptor> functions;
    public Bindings<String, VAR_TYPE, VariableDescriptor<VAR_METADATA_TYPE>> variables;

    public static <VAR_TYPE, VAR_METADATA_TYPE, FUN_TYPE> Symbols<VAR_TYPE, VAR_METADATA_TYPE, FUN_TYPE> of(
            Bindings<String, FUN_TYPE, FunctionDescriptor> functions,
            Bindings<String, VAR_TYPE, VariableDescriptor<VAR_METADATA_TYPE>> variables) {
        final Symbols<VAR_TYPE, VAR_METADATA_TYPE, FUN_TYPE> s = new Symbols<>();
        s.functions = functions;
        s.variables = variables;
        return s;
    }

}
