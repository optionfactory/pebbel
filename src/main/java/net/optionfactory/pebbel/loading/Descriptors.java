package net.optionfactory.pebbel.loading;

import java.util.Map;

public class Descriptors {

    public Map<String, FunctionDescriptor> functions;
    public Map<String, VariableDescriptor> variables;

    public static Descriptors from(Symbols symbols) {
        Descriptors self = new Descriptors();
        self.functions = symbols.functions.descriptors();
        self.variables = symbols.variables.descriptors();
        return self;
    }

}
