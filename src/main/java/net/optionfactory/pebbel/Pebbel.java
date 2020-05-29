package net.optionfactory.pebbel;

import java.util.List;
import net.optionfactory.pebbel.ast.Expression;
import net.optionfactory.pebbel.loading.Descriptors;
import net.optionfactory.pebbel.loading.Symbols;
import net.optionfactory.pebbel.results.Problem;
import net.optionfactory.pebbel.results.Result;

public class Pebbel<VERIFICATION_CONTEXT, EVALUATION_CONTEXT, VAR_TYPE, VAR_METADATA_TYPE> {

    private final Parser parser;
    private final Linker<VAR_METADATA_TYPE> linker;
    private final Loader<VERIFICATION_CONTEXT, EVALUATION_CONTEXT, VAR_TYPE, VAR_METADATA_TYPE> loader;
    private final Evaluator<VAR_TYPE, VAR_METADATA_TYPE> evaluator;
    private final FunctionsLoader fl;

    public Pebbel(Parser parser, Linker<VAR_METADATA_TYPE> linker, Loader<VERIFICATION_CONTEXT, EVALUATION_CONTEXT, VAR_TYPE, VAR_METADATA_TYPE> loader, Evaluator<VAR_TYPE, VAR_METADATA_TYPE> evaluator, FunctionsLoader fl) {
        this.parser = parser;
        this.linker = linker;
        this.loader = loader;
        this.evaluator = evaluator;
        this.fl = fl;
    }

    public static <VERIFICATION_CONTEXT, EVALUATION_CONTEXT, VAR_TYPE, VAR_METADATA_TYPE> Pebbel<VERIFICATION_CONTEXT, EVALUATION_CONTEXT, VAR_TYPE, VAR_METADATA_TYPE> defaults(Loader<VERIFICATION_CONTEXT, EVALUATION_CONTEXT, VAR_TYPE, VAR_METADATA_TYPE> loader) {
        return new Pebbel<>(new PebbelParser(), new PebbelLinker<>(), loader, new PebbelEvaluator<>(), new PebbelFunctionsLoader());
    }

    public Descriptors<VAR_METADATA_TYPE> descriptors(VERIFICATION_CONTEXT context) {
        return loader.descriptors(context, fl);
    }

    public enum VerificationMode {
        VERIFY, SKIP;
    }

    public Result<Expression> parse(VERIFICATION_CONTEXT context, String expression, VerificationMode mode, Class<?> expectedType) {
        final Result<Expression> expressionResult = parser.parse(expression, expectedType);
        if (expressionResult.isError()) {
            return expressionResult;
        }
        if (mode == VerificationMode.SKIP) {
            return expressionResult;
        }
        final List<Problem> linkerProblems = linker.link(loader.descriptors(context, fl), expressionResult.getValue(), expectedType);
        return linkerProblems.isEmpty() ? expressionResult : Result.errors(linkerProblems);
    }

    public <T> Result<T> evaluate(EVALUATION_CONTEXT context, Expression expression, Class<T> expectedType) {
        final Symbols<VAR_TYPE, VAR_METADATA_TYPE> symbols = loader.symbols(context, fl);
        return evaluator.evaluate(symbols, expression, expectedType);
    }

}
