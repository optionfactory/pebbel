package net.optionfactory.pebbel.loading;

import net.optionfactory.pebbel.results.Result;

public interface FunctionsLoader {

    Result<Bindings<String, Function, FunctionDescriptor>> load(Class<?>... classes);

}
