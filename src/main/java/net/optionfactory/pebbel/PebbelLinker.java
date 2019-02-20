package net.optionfactory.pebbel;

import java.util.List;
import net.optionfactory.pebbel.ast.Expression;
import net.optionfactory.pebbel.linking.ExpressionVerifier;
import net.optionfactory.pebbel.loading.Descriptors;
import net.optionfactory.pebbel.results.Problem;

public class PebbelLinker implements Linker {

    
    @Override
    public List<Problem> link(Descriptors descriptors, Expression expression, Class<?> expected) {
        final ExpressionVerifier checker = new ExpressionVerifier();
        return checker.verify(descriptors, expression, expected);
    }
    

}
