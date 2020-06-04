package net.optionfactory.pebbel.compiled;

import java.lang.reflect.Method;
import net.optionfactory.pebbel.Loader;
import net.optionfactory.pebbel.loading.*;

// TODO: get rid of redundant VAR_METADATA_TYPE?
public interface EarlyBindingLoader<VERIFICATION_CONTEXT, COMPILATION_CONTEXT, EVALUATION_CONTEXT, VAR_TYPE, VAR_METADATA_TYPE> extends Loader<VERIFICATION_CONTEXT, EVALUATION_CONTEXT, VAR_TYPE, VAR_METADATA_TYPE, Method> {
    Bindings<String, Method, FunctionDescriptor> functionBindings(COMPILATION_CONTEXT context, FunctionsLoader<Method> fl);
}