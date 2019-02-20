package net.optionfactory.pebbel.loading;

import net.optionfactory.pebbel.execution.Function;

public class Symbols {

    public Bindings<String, Function, FunctionDescriptor> functions;
    public Bindings<String, Object, VariableDescriptor> variables;

    public static Symbols of(
            Bindings<String, Function, FunctionDescriptor> functions,
            Bindings<String, Object, VariableDescriptor> variables) {
        Symbols s = new Symbols();
        s.functions = functions;
        s.variables = variables;
        return s;
    }

}
