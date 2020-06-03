package net.optionfactory.pebbel.interpreted;

import java.util.List;
import net.optionfactory.pebbel.loading.FunctionsLoader;
import net.optionfactory.pebbel.Loader;
import net.optionfactory.pebbel.Parser;
import net.optionfactory.pebbel.loading.PebbelFunctionsLoader;
import net.optionfactory.pebbel.verification.PebbelVerifier;
import net.optionfactory.pebbel.parsing.PebbelParser;
import net.optionfactory.pebbel.parsing.ast.Expression;
import net.optionfactory.pebbel.loading.Descriptors;
import net.optionfactory.pebbel.loading.Symbols;
import net.optionfactory.pebbel.results.Problem;
import net.optionfactory.pebbel.results.Result;
import net.optionfactory.pebbel.Verifier;

public class PebbelInterpreter<VERIFICATION_CONTEXT, EVALUATION_CONTEXT, VAR_TYPE, VAR_METADATA_TYPE> {

    private final Parser parser;
    private final Verifier<VAR_METADATA_TYPE> verifier;
    private final Loader<VERIFICATION_CONTEXT, EVALUATION_CONTEXT, VAR_TYPE, VAR_METADATA_TYPE> loader;
    private final Evaluator<VAR_TYPE, VAR_METADATA_TYPE> evaluator;
    private final FunctionsLoader fl;

    public PebbelInterpreter(Parser parser, Verifier<VAR_METADATA_TYPE> verifier, Loader<VERIFICATION_CONTEXT, EVALUATION_CONTEXT, VAR_TYPE, VAR_METADATA_TYPE> loader, Evaluator<VAR_TYPE, VAR_METADATA_TYPE> evaluator, FunctionsLoader fl) {
        this.parser = parser;
        this.verifier = verifier;
        this.loader = loader;
        this.evaluator = evaluator;
        this.fl = fl;
    }

    public static <VERIFICATION_CONTEXT, EVALUATION_CONTEXT, VAR_TYPE, VAR_METADATA_TYPE> PebbelInterpreter<VERIFICATION_CONTEXT, EVALUATION_CONTEXT, VAR_TYPE, VAR_METADATA_TYPE> defaults(Loader<VERIFICATION_CONTEXT, EVALUATION_CONTEXT, VAR_TYPE, VAR_METADATA_TYPE> loader) {
        return new PebbelInterpreter<>(new PebbelParser(), new PebbelVerifier<>(), loader, new PebbelEvaluator<>(), new PebbelFunctionsLoader(MethodHandleFunction::new));
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
        final List<Problem> problems = verifier.verify(loader.descriptors(context, fl), expressionResult.getValue(), expectedType);
        return problems.isEmpty() ? expressionResult : Result.errors(problems);
    }

    public <T> Result<T> evaluate(EVALUATION_CONTEXT context, Expression expression, Class<T> expectedType) {
        final Symbols<VAR_TYPE, VAR_METADATA_TYPE> symbols = loader.symbols(context, fl);
        return evaluator.evaluate(symbols, expression, expectedType);
    }

}
