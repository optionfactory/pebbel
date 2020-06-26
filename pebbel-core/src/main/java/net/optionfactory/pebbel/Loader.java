package net.optionfactory.pebbel;

import net.optionfactory.pebbel.loading.Descriptors;
import net.optionfactory.pebbel.loading.FunctionsLoader;
import net.optionfactory.pebbel.loading.Symbols;

public interface Loader<VAR, VARMETA, FUN> {

    <CTX> Descriptors<VARMETA, FUN> descriptors(CTX context, FunctionsLoader<FUN> fl);

    <CTX> Symbols<VAR, VARMETA, FUN> symbols(CTX context, FunctionsLoader<FUN> fl);

}
