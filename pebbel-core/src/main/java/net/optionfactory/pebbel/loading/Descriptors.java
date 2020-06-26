package net.optionfactory.pebbel.loading;

import java.util.Map;

public class Descriptors<VARMETA, FUN> {

    public Bindings<String, FUN, FunctionDescriptor> functions;
    public Map<String, VariableDescriptor<VARMETA>> variables;

    public static <VAR, VARMETA, FUN> Descriptors<VARMETA, FUN> from(Symbols<VAR, VARMETA, FUN> symbols) {
        final Descriptors<VARMETA, FUN> self = new Descriptors<>();
        self.functions = symbols.functions;
        self.variables = symbols.variables.descriptors();
        return self;
    }

}
