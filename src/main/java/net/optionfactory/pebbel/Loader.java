package net.optionfactory.pebbel;

import net.optionfactory.pebbel.loading.Descriptors;
import net.optionfactory.pebbel.loading.Symbols;

public interface Loader<VERIFICATION_CONTEXT, EVALUATION_CONTEXT, VAR_TYPE, VAR_METADATA_TYPE> {

    Descriptors<VAR_METADATA_TYPE> descriptors(VERIFICATION_CONTEXT context, FunctionsLoader fl);

    Symbols<VAR_TYPE, VAR_METADATA_TYPE> symbols(EVALUATION_CONTEXT context, FunctionsLoader fl);

}
