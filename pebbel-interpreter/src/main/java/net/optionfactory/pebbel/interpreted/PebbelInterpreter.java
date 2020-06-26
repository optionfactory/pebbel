package net.optionfactory.pebbel.interpreted;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.List;
import net.optionfactory.pebbel.Loader;
import net.optionfactory.pebbel.Parser;
import net.optionfactory.pebbel.Verifier;
import net.optionfactory.pebbel.loading.Descriptors;
import net.optionfactory.pebbel.loading.FunctionsLoader;
import net.optionfactory.pebbel.loading.LoadingException;
import net.optionfactory.pebbel.loading.PebbelFunctionsLoader;
import net.optionfactory.pebbel.loading.Symbols;
import net.optionfactory.pebbel.parsing.PebbelParser;
import net.optionfactory.pebbel.parsing.ast.Expression;
import net.optionfactory.pebbel.results.Problem;
import net.optionfactory.pebbel.results.Result;
import net.optionfactory.pebbel.verification.PebbelVerifier;

public class PebbelInterpreter<VAR, VARMETA> {

    private final Parser parser;
    private final Verifier verifier;
    private final Loader<VAR, VARMETA, MethodHandle> loader;
    private final Evaluator<VAR, VARMETA, MethodHandle> evaluator;
    private final FunctionsLoader<MethodHandle> fl;

    public PebbelInterpreter(Parser parser, Verifier verifier, Loader<VAR, VARMETA, MethodHandle> loader, Evaluator<VAR, VARMETA, MethodHandle> evaluator, FunctionsLoader<MethodHandle> fl) {
        this.parser = parser;
        this.verifier = verifier;
        this.loader = loader;
        this.evaluator = evaluator;
        this.fl = fl;
    }

    public static <VAR, VARMETA> PebbelInterpreter<VAR, VARMETA> defaults(Loader<VAR, VARMETA, MethodHandle> loader) {
        return new PebbelInterpreter<>(new PebbelParser(), new PebbelVerifier(), loader, new PebbelEvaluator<>(), new PebbelFunctionsLoader<>(PebbelInterpreter::unreflect));
    }

    private static MethodHandle unreflect(Method m) {
        try {
            return MethodHandles.publicLookup().unreflect(m);
        } catch (IllegalAccessException ex) {
            throw new LoadingException(ex);
        }

    }

    public <CTX> Descriptors<VARMETA, MethodHandle> descriptors(CTX context) {
        return loader.descriptors(context, fl);
    }

    public <CTX> Symbols<VAR, VARMETA, MethodHandle> symbols(CTX context) {
        return loader.symbols(context, fl);
    }

    public Result<Expression> parse(String expression, Class<?> expectedType) {
        return parser.parse(expression, expectedType);
    }

    public Result<Expression> verify(Descriptors<VARMETA, MethodHandle> descriptors, Expression expression, Class<?> expectedType) {
        final List<Problem> problems = verifier.verify(descriptors, expression, expectedType);
        return problems.isEmpty() ? Result.value(expression) : Result.errors(problems);
    }

    public <T> Result<T> evaluate(Symbols<VAR, VARMETA, MethodHandle> symbols, Expression expression, Class<T> expectedType) {
        return evaluator.evaluate(symbols, expression, expectedType);
    }

}
