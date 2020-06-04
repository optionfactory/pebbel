package net.optionfactory.pebbel.compiled;

import net.optionfactory.pebbel.Parser;
import net.optionfactory.pebbel.Verifier;
import net.optionfactory.pebbel.loading.*;
import net.optionfactory.pebbel.parsing.PebbelParser;
import net.optionfactory.pebbel.parsing.ast.Expression;
import net.optionfactory.pebbel.results.Problem;
import net.optionfactory.pebbel.results.Result;
import net.optionfactory.pebbel.verification.PebbelVerifier;

import java.util.List;

public class PebbelCompiler<VERIFICATION_CONTEXT, COMPILATION_CONTEXT, EVALUATION_CONTEXT, VAR_TYPE, VAR_METADATA_TYPE> {

    private final Parser parser;
    private final Verifier<VAR_METADATA_TYPE> verifier;
    private final EarlyBindingLoader<VERIFICATION_CONTEXT, COMPILATION_CONTEXT, EVALUATION_CONTEXT, VAR_TYPE, VAR_METADATA_TYPE> loader;
    private final FunctionsLoader fl;

    public PebbelCompiler(Parser parser, Verifier<VAR_METADATA_TYPE> verifier, EarlyBindingLoader<VERIFICATION_CONTEXT, COMPILATION_CONTEXT, EVALUATION_CONTEXT, VAR_TYPE, VAR_METADATA_TYPE> loader, FunctionsLoader fl) {
        this.parser = parser;
        this.verifier = verifier;
        this.loader = loader;
        this.fl = fl;
    }

    public static <VERIFICATION_CONTEXT, COMPILATION_CONTEXT, EVALUATION_CONTEXT, VAR_TYPE, VAR_METADATA_TYPE>
    PebbelCompiler<VERIFICATION_CONTEXT, COMPILATION_CONTEXT, EVALUATION_CONTEXT, VAR_TYPE, VAR_METADATA_TYPE>
    defaults(EarlyBindingLoader<VERIFICATION_CONTEXT, COMPILATION_CONTEXT, EVALUATION_CONTEXT, VAR_TYPE, VAR_METADATA_TYPE> loader) {
        return new PebbelCompiler<>(new PebbelParser(), new PebbelVerifier<>(), loader, new PebbelFunctionsLoader(MethodHolderFunction::new));
    }

    public Descriptors<VAR_METADATA_TYPE> descriptors(VERIFICATION_CONTEXT context) {
        return loader.descriptors(context, fl);
    }

    public <R> Result<CompiledExpression<VAR_TYPE, VAR_METADATA_TYPE, R>> compile(VERIFICATION_CONTEXT context, COMPILATION_CONTEXT compilationContext, String expression, Class<R> expectedType) { // TODO pass classloader
        final Result<Expression> expressionResult = parser.parse(expression, expectedType);
        if (expressionResult.isError()) {
            return expressionResult.mapErrors();
        }
        final Descriptors<VAR_METADATA_TYPE> descriptors = loader.descriptors(context, fl);
        final List<Problem> verificationProblems = verifier.verify(descriptors, expressionResult.getValue(), expectedType);
        if (!verificationProblems.isEmpty()) {
            return Result.errors(verificationProblems);
        }
        final Bindings<String, Function, FunctionDescriptor> functionBindings = loader.functionBindings(compilationContext, fl);
        return new ExpressionCompiler<VAR_METADATA_TYPE>().compile(functionBindings, descriptors.variables, expressionResult.getValue(), expectedType);
    }

    public <T> Result<T> evaluate(EVALUATION_CONTEXT context, CompiledExpression<VAR_TYPE, VAR_METADATA_TYPE, T> expression, Class<T> expectedType) {
        final Symbols<VAR_TYPE, VAR_METADATA_TYPE> symbols = loader.symbols(context, fl);
        return Result.value(expression.evaluate(symbols.variables));
    }
}
