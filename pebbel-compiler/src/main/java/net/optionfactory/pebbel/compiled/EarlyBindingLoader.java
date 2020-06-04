package net.optionfactory.pebbel.compiled;

import net.optionfactory.pebbel.Loader;
import net.optionfactory.pebbel.loading.*;

// TODO: get rid of redundant VAR_METADATA_TYPE?
public interface EarlyBindingLoader<VERIFICATION_CONTEXT, COMPILATION_CONTEXT, EVALUATION_CONTEXT, VAR_TYPE, VAR_METADATA_TYPE> extends Loader<VERIFICATION_CONTEXT, EVALUATION_CONTEXT, VAR_TYPE, VAR_METADATA_TYPE> {
    Bindings<String, Function, FunctionDescriptor> functionBindings(COMPILATION_CONTEXT context, FunctionsLoader fl);
}