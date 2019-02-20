package net.optionfactory.pebbel;

import java.util.List;
import net.optionfactory.pebbel.ast.Expression;
import net.optionfactory.pebbel.loading.Descriptors;
import net.optionfactory.pebbel.results.Problem;

public interface Linker {

    List<Problem> link(Descriptors descriptors, Expression expression, Class<?> expected);

}
