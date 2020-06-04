package net.optionfactory.pebbel.loading;

import net.optionfactory.pebbel.results.Result;

public interface FunctionsLoader<FUN_TYPE> {

    Result<Bindings<String, FUN_TYPE, FunctionDescriptor>> load(Class<?>... classes);

}
