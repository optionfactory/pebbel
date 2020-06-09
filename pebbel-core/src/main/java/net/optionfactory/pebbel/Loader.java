package net.optionfactory.pebbel;

import net.optionfactory.pebbel.loading.Bindings;
import net.optionfactory.pebbel.loading.FunctionDescriptor;
import net.optionfactory.pebbel.loading.FunctionsLoader;
import net.optionfactory.pebbel.loading.Descriptors;
import net.optionfactory.pebbel.loading.Symbols;
import net.optionfactory.pebbel.loading.VariableDescriptor;

import java.lang.reflect.Method;
import java.util.Map;

public interface Loader<VERIFICATION_CONTEXT, EVALUATION_CONTEXT, VAR_TYPE, VAR_METADATA_TYPE, FUN_TYPE> {

    Descriptors<VAR_METADATA_TYPE, FUN_TYPE> descriptors(VERIFICATION_CONTEXT context, FunctionsLoader fl);
    Symbols<VAR_TYPE, VAR_METADATA_TYPE, FUN_TYPE> symbols(EVALUATION_CONTEXT context, FunctionsLoader fl);

}
