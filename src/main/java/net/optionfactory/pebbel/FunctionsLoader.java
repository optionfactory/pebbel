package net.optionfactory.pebbel;

import net.optionfactory.pebbel.loading.Bindings;
import net.optionfactory.pebbel.loading.FunctionDescriptor;
import net.optionfactory.pebbel.results.Result;
import net.optionfactory.pebbel.execution.Function;

public interface FunctionsLoader {

    Result<Bindings<String, Function, FunctionDescriptor>> load(Class<?>... classes);

}
