package net.optionfactory.pebbel.verification;

import java.util.List;
import net.optionfactory.pebbel.parsing.ast.Expression;
import net.optionfactory.pebbel.loading.Descriptors;
import net.optionfactory.pebbel.results.Problem;
import net.optionfactory.pebbel.Verifier;

public class PebbelVerifier<VARMETA> implements Verifier<VARMETA> {

    @Override
    public List<Problem> verify(Descriptors<VARMETA, ?> descriptors, Expression expression, Class<?> expected) {
        final ExpressionVerifier<VARMETA> checker = new ExpressionVerifier<>();
        return checker.verify(descriptors, expression, expected);
    }

}
