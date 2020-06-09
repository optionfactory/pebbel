package net.optionfactory.pebbel.loading;

import java.util.Map;

public class Descriptors<VAR_METADATA_TYPE, FUN_TYPE> {

    public Bindings<String, FUN_TYPE, FunctionDescriptor> functions;
    public Map<String, VariableDescriptor<VAR_METADATA_TYPE>> variables;

    public static <VAR_TYPE, VAR_METADATA_TYPE, FUN_TYPE> Descriptors<VAR_METADATA_TYPE, FUN_TYPE> from(Symbols<VAR_TYPE, VAR_METADATA_TYPE, FUN_TYPE> symbols) {
        final Descriptors<VAR_METADATA_TYPE, FUN_TYPE> self = new Descriptors<>();
        self.functions = symbols.functions;
        self.variables = symbols.variables.descriptors();
        return self;
    }

}
