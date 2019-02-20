package net.optionfactory.pebbel;

import net.optionfactory.pebbel.loading.Descriptors;
import net.optionfactory.pebbel.loading.Symbols;

public interface Loader<C1, C2> {

    Descriptors descriptors(C1 context, FunctionsLoader fl);

    Symbols symbols(C2 context, FunctionsLoader fl);

}
