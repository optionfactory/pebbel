package net.optionfactory.pebbel.loading;

import net.optionfactory.pebbel.results.Result;

public interface FunctionsLoader<FUN> {

    Result<Bindings<String, FUN, FunctionDescriptor>> load(Class<?>... classes);

}
