package net.optionfactory.pebbel;

import java.util.List;
import net.optionfactory.pebbel.ast.Expression;
import net.optionfactory.pebbel.linking.ExpressionVerifier;
import net.optionfactory.pebbel.loading.Descriptors;
import net.optionfactory.pebbel.results.Problem;

public class PebbelLinker<VAR_METADATA_TYPE> implements Linker<VAR_METADATA_TYPE> {

    @Override
    public List<Problem> link(Descriptors<VAR_METADATA_TYPE> descriptors, Expression expression, Class<?> expected) {
        final ExpressionVerifier<VAR_METADATA_TYPE> checker = new ExpressionVerifier<>();
        return checker.verify(descriptors, expression, expected);
    }

}
