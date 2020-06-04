package net.optionfactory.pebbel.interpreted;

import net.optionfactory.pebbel.parsing.ast.Expression;
import net.optionfactory.pebbel.loading.Symbols;
import net.optionfactory.pebbel.results.Result;

public interface Evaluator<VAR_TYPE, VAR_METADATA_TYPE, FUN_TYPE> {

    <R> Result<R> evaluate(Symbols<VAR_TYPE, VAR_METADATA_TYPE, FUN_TYPE> symbols, Expression expression, Class<R> expected);

}
