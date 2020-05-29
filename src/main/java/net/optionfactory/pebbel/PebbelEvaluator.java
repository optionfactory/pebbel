package net.optionfactory.pebbel;

import net.optionfactory.pebbel.ast.Expression;
import net.optionfactory.pebbel.execution.ExpressionEvaluator;
import net.optionfactory.pebbel.loading.Symbols;
import net.optionfactory.pebbel.results.Result;

public class PebbelEvaluator<VAR_TYPE, VAR_METADATA_TYPE> implements Evaluator<VAR_TYPE, VAR_METADATA_TYPE> {

    @Override
    public <R> Result<R> evaluate(Symbols<VAR_TYPE, VAR_METADATA_TYPE> symbols, Expression expression, Class<R> expected) {
        final ExpressionEvaluator<VAR_TYPE, VAR_METADATA_TYPE> evaluator = new ExpressionEvaluator<>();
        return evaluator.evaluate(symbols, expression);
    }

}
