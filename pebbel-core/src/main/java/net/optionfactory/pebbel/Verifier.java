package net.optionfactory.pebbel;

import java.util.List;
import net.optionfactory.pebbel.loading.Descriptors;
import net.optionfactory.pebbel.parsing.ast.Expression;
import net.optionfactory.pebbel.results.Problem;

public interface Verifier {

    <VARMETA> List<Problem> verify(Descriptors<VARMETA, ?> descriptors, Expression expression, Class<?> expected);

}
