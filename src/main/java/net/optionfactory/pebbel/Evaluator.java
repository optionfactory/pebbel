package net.optionfactory.pebbel;

import net.optionfactory.pebbel.ast.Expression;
import net.optionfactory.pebbel.loading.Symbols;
import net.optionfactory.pebbel.results.Result;

public interface Evaluator {

    <R> Result<R> evaluate(Symbols symbols, Expression expression, Class<R> expected);

}
