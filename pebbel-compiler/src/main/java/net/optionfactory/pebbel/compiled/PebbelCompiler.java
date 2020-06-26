package net.optionfactory.pebbel.compiled;

import java.lang.reflect.Method;
import java.util.List;
import net.optionfactory.pebbel.Loader;
import net.optionfactory.pebbel.Parser;
import net.optionfactory.pebbel.Verifier;
import net.optionfactory.pebbel.loading.Descriptors;
import net.optionfactory.pebbel.loading.FunctionsLoader;
import net.optionfactory.pebbel.loading.PebbelFunctionsLoader;
import net.optionfactory.pebbel.loading.Symbols;
import net.optionfactory.pebbel.parsing.PebbelParser;
import net.optionfactory.pebbel.parsing.ast.Expression;
import net.optionfactory.pebbel.results.Problem;
import net.optionfactory.pebbel.results.Result;
import net.optionfactory.pebbel.verification.PebbelVerifier;

public class PebbelCompiler<VER_CTX, EVAL_CTX, VAR, VARMETA> {

    private final Parser parser;
    private final Verifier<VARMETA> verifier;
    private final Loader<VER_CTX, EVAL_CTX, VAR, VARMETA, Method> loader;
    private final FunctionsLoader<Method> fl;
    private final CompiledExpression.Loader expressionLoader;

    public PebbelCompiler(Parser parser, Verifier<VARMETA> verifier, Loader<VER_CTX, EVAL_CTX, VAR, VARMETA, Method> loader, FunctionsLoader<Method> fl, CompiledExpression.Loader expressionLoader) {
        this.parser = parser;
        this.verifier = verifier;
        this.loader = loader;
        this.fl = fl;
        this.expressionLoader = expressionLoader;
    }

    public static <VER_CTX, EVAL_CTX, VAR, VARMETA> PebbelCompiler<VER_CTX, EVAL_CTX, VAR, VARMETA> defaults(Loader<VER_CTX, EVAL_CTX, VAR, VARMETA, Method> loader, CompiledExpression.Loader expressionLoader) {
        return new PebbelCompiler<>(new PebbelParser(), new PebbelVerifier<>(), loader, new PebbelFunctionsLoader<>(m -> m), expressionLoader);
    }

    public Descriptors<VARMETA, Method> descriptors(VER_CTX context) {
        return loader.descriptors(context, fl);
    }

    public Symbols<VAR, VARMETA, Method> symbols(EVAL_CTX context) {
        return loader.symbols(context, fl);
    }

    public <R> Result<Expression> parse(String expression, Class<R> expectedType) {
        return parser.parse(expression, expectedType);
    }

    public <R> Result<Expression> verify(Descriptors<VARMETA, Method> descriptors, Expression expression, Class<R> expectedType) {
        final List<Problem> problems = verifier.verify(descriptors, expression, expectedType);
        return problems.isEmpty() ? Result.value(expression) : Result.errors(problems);
    }

    public <R> Result<CompiledExpression.Unloaded<R>> compile(Descriptors<VARMETA, Method> descriptors, Expression expression, Class<R> expectedType) {
        return new ExpressionCompiler().compile(descriptors.functions, expression, expectedType);
    }

    public <R> CompiledExpression<R> load(CompiledExpression.Unloaded<R> unloaded) {
        return expressionLoader.load(unloaded);
    }

    public <T> Result<T> evaluate(Symbols<VAR, VARMETA, Method> symbols, CompiledExpression<T> expression, Class<T> expectedType) {
        return Result.value(expression.evaluate(symbols.variables));
    }
}
