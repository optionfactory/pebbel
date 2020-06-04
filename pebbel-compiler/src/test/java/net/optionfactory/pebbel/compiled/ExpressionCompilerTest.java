package net.optionfactory.pebbel.compiled;

import net.optionfactory.pebbel.loading.BindingHandler;
import net.optionfactory.pebbel.loading.Bindings;
import net.optionfactory.pebbel.loading.Function;
import net.optionfactory.pebbel.loading.FunctionDescriptor;
import net.optionfactory.pebbel.loading.PebbelFunctionsLoader;
import net.optionfactory.pebbel.loading.VariableDescriptor;
import net.optionfactory.pebbel.parsing.ast.*;
import net.optionfactory.pebbel.results.Result;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.util.ASMifier;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceClassVisitor;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.Map;


public class ExpressionCompilerTest {


    public static class Functions {
        @BindingHandler("foo")
        public static String foo() {
            return "Foo";
        }

        @BindingHandler("hello")
        public static String hello(String what) {
            return "Hello " + what;
        }

        @BindingHandler("bar")
        public static Object bar() {
            return "Bar";
        }
    }

    private static Bindings<String, Function, FunctionDescriptor> FN_BINDINGS;
    private static Map<String, VariableDescriptor<Object>> VAR_DESCRIPTORS;

    static {
        final PebbelFunctionsLoader fl = new PebbelFunctionsLoader(MethodHolderFunction::new);
        final Result<Bindings<String, Function, FunctionDescriptor>> load = fl.load(Functions.class);
        if (load.isError()) {
            throw new RuntimeException(load.getErrors().toString());
        }
        FN_BINDINGS = load.getValue();
        VAR_DESCRIPTORS = Map.of(
                "STR", VariableDescriptor.of("VAR1", String.class, null),
                "INT", VariableDescriptor.of("VAR1", Integer.class, null),
                "BOOL", VariableDescriptor.of("VAR1", Boolean.class, null)
        );
    }

    @Ignore
    @Test
    public void explore() throws IOException {
        final ClassReader cr = new ClassReader(new FileInputStream("/tmp/Asd.class"));
        TraceClassVisitor traceClassVisitor = new TraceClassVisitor(null, new ASMifier(), new PrintWriter(System.out));
        cr.accept(traceClassVisitor, 0);
    }

    @Test
    public void compileNumberLiteral() {
        final NumberLiteral expression = NumberLiteral.of(123d, null);
        final Result<CompiledExpression<Object, Object, Object>> result = new ExpressionCompiler<>().compile(FN_BINDINGS, VAR_DESCRIPTORS, expression);
        Assert.assertFalse(result.isError());
        Assert.assertEquals(Double.valueOf(123d), result.getValue().evaluate(Bindings.empty()));
        printGeneratedCode();
    }

    @Test
    public void compileStringLiteral() {
        final StringLiteral expression = StringLiteral.of("Hello", null);
        final Result<CompiledExpression<Object, Object, Object>> result = new ExpressionCompiler<>().compile(FN_BINDINGS, VAR_DESCRIPTORS, expression);
        Assert.assertFalse(result.isError());
        Assert.assertEquals("Hello", result.getValue().evaluate(Bindings.empty()));
        printGeneratedCode();
    }

    @Test
    public void compileVariableReference() {
        final Variable expression = Variable.of("INT", null);
        final Result<CompiledExpression<Object, Object, Object>> result = new ExpressionCompiler<>().compile(FN_BINDINGS, VAR_DESCRIPTORS, expression);
        Assert.assertFalse(result.isError());
        Assert.assertEquals(Integer.valueOf(123), result.getValue().evaluate(Bindings.singleton("INT", 123, VAR_DESCRIPTORS.get("INT"))));
        printGeneratedCode();
    }

    @Test
    public void compileFunctionCallNullary() {
        final FunctionCall expression = FunctionCall.of("foo", new Expression[0], null);
        final Result<CompiledExpression<Object, Object, Object>> result = new ExpressionCompiler<>().compile(FN_BINDINGS, VAR_DESCRIPTORS, expression);
        Assert.assertFalse(result.isError());
        Assert.assertEquals(Functions.foo(), result.getValue().evaluate(Bindings.empty()));
        printGeneratedCode();
    }

    @Test
    public void compileFunctionCallUnary() {
        final FunctionCall expression = FunctionCall.of("hello", new Expression[] { StringLiteral.of("world", null)}, null);
        final Result<CompiledExpression<Object, Object, Object>> result = new ExpressionCompiler<>().compile(FN_BINDINGS, VAR_DESCRIPTORS, expression);
        Assert.assertFalse(result.isError());
        Assert.assertEquals(Functions.hello("world"), result.getValue().evaluate(Bindings.empty()));
        printGeneratedCode();
    }

    @Test
    public void compileFunctionCallUnaryChain() {
        final FunctionCall expression = FunctionCall.of("hello", new Expression[] { FunctionCall.of("foo", new Expression[0], null)}, null);
        final Result<CompiledExpression<Object, Object, Object>> result = new ExpressionCompiler<>().compile(FN_BINDINGS, VAR_DESCRIPTORS, expression);
        Assert.assertFalse(result.isError());
        Assert.assertEquals(Functions.hello(Functions.foo()), result.getValue().evaluate(Bindings.empty()));
        printGeneratedCode();
    }

    @Test
    public void compileFunctionCallUnaryChainCast() {
        final FunctionCall expression = FunctionCall.of("hello", new Expression[] { FunctionCall.of("bar", new Expression[0], null)}, null);
        final Result<CompiledExpression<Object, Object, Object>> result = new ExpressionCompiler<>().compile(FN_BINDINGS, VAR_DESCRIPTORS, expression);
        Assert.assertFalse(result.isError());
        Assert.assertEquals(Functions.hello((String) Functions.bar()), result.getValue().evaluate(Bindings.empty()));
        printGeneratedCode();
    }

    @Test
    public void compileFunctionCallUnaryVariableCast() {
        final FunctionCall expression = FunctionCall.of("hello", new Expression[] { Variable.of("STR", null) }, null);
        final Result<CompiledExpression<Object, Object, Object>> result = new ExpressionCompiler<>().compile(FN_BINDINGS, VAR_DESCRIPTORS, expression);
        Assert.assertFalse(result.isError());
        Assert.assertEquals(Functions.hello("variable"), result.getValue().evaluate(Bindings.singleton("STR", "variable", VAR_DESCRIPTORS.get("STR"))));
        printGeneratedCode();
    }

    @Test
    public void compileShortCircuitExpressionOr() {
        final ShortCircuitExpression expression = ShortCircuitExpression.of(
                new BooleanOperator[]{BooleanOperator.OR},
                new BooleanExpression[]{Variable.of("STR", null), Variable.of("STR", null)},
                null);
        final Result<CompiledExpression<Object, Object, Object>> result = new ExpressionCompiler<>().compile(FN_BINDINGS, VAR_DESCRIPTORS, expression);
        Assert.assertFalse(result.isError());
        printGeneratedCode();
    }

    @Test
    public void compileShortCircuitExpressionAnd() {
        final ShortCircuitExpression expression = ShortCircuitExpression.of(
                new BooleanOperator[]{BooleanOperator.AND},
                new BooleanExpression[]{Variable.of("BOOL", null), Variable.of("BOOL", null)},
                null);
        final Result<CompiledExpression<Object, Object, Object>> result = new ExpressionCompiler<>().compile(FN_BINDINGS, VAR_DESCRIPTORS, expression);
        Assert.assertFalse(result.isError());
        printGeneratedCode();
    }

    private void printGeneratedCode() {
        final ClassReader cr = new ClassReader(ExpressionCompiler.lastGeneratedBytecode);
        TraceClassVisitor traceClassVisitor = new TraceClassVisitor(null, new Textifier(), new PrintWriter(System.out));
        cr.accept(traceClassVisitor, 0);
    }
}