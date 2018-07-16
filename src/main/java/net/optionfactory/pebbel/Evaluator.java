package net.optionfactory.pebbel;

import net.optionfactory.pebbel.ast.Expression;
import net.optionfactory.pebbel.loading.Symbols;
import net.optionfactory.pebbel.results.Result;

public interface Evaluator<VV, VDMD> {

    <R> Result<R> evaluate(Symbols<VV, VDMD> symbols, Expression expression, Class<R> expected);

}
