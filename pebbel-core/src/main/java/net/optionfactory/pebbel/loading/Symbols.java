package net.optionfactory.pebbel.loading;

public class Symbols<VAR, VARMETA, FUN> {

    public Bindings<String, FUN, FunctionDescriptor> functions;
    public Bindings<String, VAR, VariableDescriptor<VARMETA>> variables;

    public static <VAR, VARMETA, FUN> Symbols<VAR, VARMETA, FUN> of(
            Bindings<String, FUN, FunctionDescriptor> functions,
            Bindings<String, VAR, VariableDescriptor<VARMETA>> variables) {
        final Symbols<VAR, VARMETA, FUN> s = new Symbols<>();
        s.functions = functions;
        s.variables = variables;
        return s;
    }

}
