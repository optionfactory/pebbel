package net.optionfactory.pebbel.interpreted;

import java.lang.invoke.MethodHandle;
import net.optionfactory.pebbel.parsing.ast.Expression;
import net.optionfactory.pebbel.loading.Symbols;
import net.optionfactory.pebbel.results.Result;

public class PebbelEvaluator<VAR_TYPE, VAR_METADATA_TYPE> implements Evaluator<VAR_TYPE, VAR_METADATA_TYPE, MethodHandle> {

    @Override
    public <R> Result<R> evaluate(Symbols<VAR_TYPE, VAR_METADATA_TYPE, MethodHandle> symbols, Expression expression, Class<R> expected) {
        final ExpressionEvaluator<VAR_TYPE, VAR_METADATA_TYPE> evaluator = new ExpressionEvaluator<>();
        return evaluator.evaluate(symbols, expression);
    }

}
