package net.optionfactory.pebbel;

import java.util.List;
import net.optionfactory.pebbel.ast.Expression;
import net.optionfactory.pebbel.loading.Descriptors;
import net.optionfactory.pebbel.loading.Symbols;
import net.optionfactory.pebbel.results.Problem;
import net.optionfactory.pebbel.results.Result;

public class Pebbel<C1, C2> {

    private final Parser parser;
    private final Linker linker;
    private final Loader<C1, C2> loader;
    private final Evaluator evaluator;
    private final FunctionsLoader fl;

    public Pebbel(Parser parser, Linker linker, Loader<C1, C2> loader, Evaluator evaluator, FunctionsLoader fl) {
        this.parser = parser;
        this.linker = linker;
        this.loader = loader;
        this.evaluator = evaluator;
        this.fl = fl;
    }

    public static <CTX1, CTX2> Pebbel<CTX1, CTX2> defaults(Loader<CTX1, CTX2> loader) {
        return new Pebbel<>(new PebbelParser(), new PebbelLinker(), loader, new PebbelEvaluator(), new PebbelFunctionsLoader());
    }

    public Descriptors descriptors(C1 context) {
        return loader.descriptors(context, fl);
    }

    public List<Problem> verify(C1 context, String expression, Class<?> expectedType) {
        final Result<Expression> expressionResult = parser.parse(expression, expectedType);
        if (expressionResult.isError()) {
            return expressionResult.getErrors();
        }
        return linker.link(loader.descriptors(context, fl), expressionResult.getValue(), expectedType);
    }

    public <T> Result<T> evaluate(C2 context, String source, Class<T> expectedType) {
        final Symbols symbols = loader.symbols(context, fl);
        final Result<Expression> parsed = parser.parse(source, expectedType);
        if (parsed.isError()) {
            return parsed.mapErrors();
        }
        final Expression expression = parsed.getValue();
        final List<Problem> linkingProblems = linker.link(Descriptors.from(symbols), expression, expectedType);
        if (!linkingProblems.isEmpty()) {
            return Result.errors(linkingProblems);
        }
        return evaluator.evaluate(symbols, expression, expectedType);
    }

}
