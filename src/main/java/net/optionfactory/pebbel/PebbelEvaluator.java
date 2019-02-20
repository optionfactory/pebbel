package net.optionfactory.pebbel;

import net.optionfactory.pebbel.ast.Expression;
import net.optionfactory.pebbel.execution.ExpressionEvaluator;
import net.optionfactory.pebbel.loading.Symbols;
import net.optionfactory.pebbel.results.Result;

public class PebbelEvaluator implements Evaluator {

    @Override
    public <R> Result<R> evaluate(Symbols symbols, Expression expression, Class<R> expected) {
        final ExpressionEvaluator evaluator = new ExpressionEvaluator();
        return evaluator.evaluate(symbols, expression);
    }

}
