package net.optionfactory.pebbel.interpreted;

import net.optionfactory.pebbel.parsing.ast.Expression;
import net.optionfactory.pebbel.loading.Symbols;
import net.optionfactory.pebbel.results.Result;

public interface Evaluator<VAR, VARMETA, FUN> {

    <R> Result<R> evaluate(Symbols<VAR, VARMETA, FUN> symbols, Expression expression, Class<R> expected);

}
