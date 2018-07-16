package net.optionfactory.pebbel.loading;

import java.util.Map;

public class Descriptors<VDMD> {

    public Map<String, FunctionDescriptor> functions;
    public Map<String, VariableDescriptor<VDMD>> variables;

    public static <VV, VDMD> Descriptors<VDMD> from(Symbols<VV, VDMD> symbols) {
        Descriptors<VDMD> self = new Descriptors<>();
        self.functions = symbols.functions.descriptors();
        self.variables = symbols.variables.descriptors();
        return self;
    }

}
