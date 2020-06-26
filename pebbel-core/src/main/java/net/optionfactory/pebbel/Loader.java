package net.optionfactory.pebbel;

import net.optionfactory.pebbel.loading.FunctionsLoader;
import net.optionfactory.pebbel.loading.Descriptors;
import net.optionfactory.pebbel.loading.Symbols;

public interface Loader<VER_CTX, EVAL_CTX, VAR, VARMETA, FUN> {

    Descriptors<VARMETA, FUN> descriptors(VER_CTX context, FunctionsLoader fl);

    Symbols<VAR, VARMETA, FUN> symbols(EVAL_CTX context, FunctionsLoader fl);

}
