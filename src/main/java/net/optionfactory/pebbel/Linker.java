package net.optionfactory.pebbel;

import java.util.List;
import net.optionfactory.pebbel.ast.Expression;
import net.optionfactory.pebbel.loading.Descriptors;
import net.optionfactory.pebbel.results.Problem;

public interface Linker<VAR_METADATA_TYPE> {

    List<Problem> link(Descriptors<VAR_METADATA_TYPE> descriptors, Expression expression, Class<?> expected);

}
