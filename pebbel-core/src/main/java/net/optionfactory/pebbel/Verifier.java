package net.optionfactory.pebbel;

import java.util.List;
import net.optionfactory.pebbel.parsing.ast.Expression;
import net.optionfactory.pebbel.loading.Descriptors;
import net.optionfactory.pebbel.results.Problem;

public interface Verifier<VAR_METADATA_TYPE> {

    List<Problem> verify(Descriptors<VAR_METADATA_TYPE> descriptors, Expression expression, Class<?> expected);

}
