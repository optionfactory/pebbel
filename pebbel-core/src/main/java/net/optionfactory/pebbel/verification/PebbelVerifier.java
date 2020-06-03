package net.optionfactory.pebbel.verification;

import java.util.List;
import net.optionfactory.pebbel.parsing.ast.Expression;
import net.optionfactory.pebbel.loading.Descriptors;
import net.optionfactory.pebbel.results.Problem;
import net.optionfactory.pebbel.Verifier;

public class PebbelVerifier<VAR_METADATA_TYPE> implements Verifier<VAR_METADATA_TYPE> {

    @Override
    public List<Problem> verify(Descriptors<VAR_METADATA_TYPE> descriptors, Expression expression, Class<?> expected) {
        final ExpressionVerifier<VAR_METADATA_TYPE> checker = new ExpressionVerifier<>();
        return checker.verify(descriptors, expression, expected);
    }

}
