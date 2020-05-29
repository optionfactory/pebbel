package net.optionfactory.pebbel;

import net.optionfactory.pebbel.ast.Expression;
import net.optionfactory.pebbel.loading.Symbols;
import net.optionfactory.pebbel.results.Result;

public interface Evaluator<VAR_TYPE, VAR_METADATA_TYPE> {

    <R> Result<R> evaluate(Symbols<VAR_TYPE, VAR_METADATA_TYPE> symbols, Expression expression, Class<R> expected);

}
