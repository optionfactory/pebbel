package net.optionfactory.pebbel;

import net.optionfactory.pebbel.loading.Descriptors;
import net.optionfactory.pebbel.loading.FunctionsLoader;
import net.optionfactory.pebbel.loading.Symbols;

public interface Loader<VAR, VARMETA, FUN, DCTX, SCTX> {

    Descriptors<VARMETA, FUN> descriptors(DCTX context, FunctionsLoader<FUN> fl);

    Symbols<VAR, VARMETA, FUN> symbols(SCTX context, FunctionsLoader<FUN> fl);

}
