package net.optionfactory.pebbel.interpreted;

import java.lang.invoke.MethodHandle;
import net.optionfactory.pebbel.parsing.ast.Expression;
import net.optionfactory.pebbel.loading.Symbols;
import net.optionfactory.pebbel.results.Result;

public class PebbelEvaluator<VAR, VARMETA> implements Evaluator<VAR, VARMETA, MethodHandle> {

    @Override
    public <R> Result<R> evaluate(Symbols<VAR, VARMETA, MethodHandle> symbols, Expression expression, Class<R> expected) {
        final ExpressionEvaluator<VAR, VARMETA> evaluator = new ExpressionEvaluator<>();
        return evaluator.evaluate(symbols, expression);
    }

}
