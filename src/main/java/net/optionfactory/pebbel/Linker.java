package net.optionfactory.pebbel;

import java.util.List;
import net.optionfactory.pebbel.ast.Expression;
import net.optionfactory.pebbel.loading.Descriptors;
import net.optionfactory.pebbel.results.Problem;

public interface Linker<VDMD> {

    List<Problem> link(Descriptors<VDMD> descriptors, Expression expression, Class<?> expected);

}
