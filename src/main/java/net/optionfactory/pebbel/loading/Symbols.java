package net.optionfactory.pebbel.loading;

import net.optionfactory.pebbel.execution.Function;

public class Symbols<VV, VDMD> {

    public Bindings<String, Function, FunctionDescriptor> functions;
    public Bindings<String, VV, VariableDescriptor<VDMD>> variables;

    public static <VV, VDMD> Symbols<VV, VDMD> of(
            Bindings<String, Function, FunctionDescriptor> functions,
            Bindings<String, VV, VariableDescriptor<VDMD>> variables) {
        Symbols<VV, VDMD> s = new Symbols<>();
        s.functions = functions;
        s.variables = variables;
        return s;
    }

}
