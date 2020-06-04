package net.optionfactory.pebbel.compiled;

import net.optionfactory.pebbel.loading.Bindings;
import net.optionfactory.pebbel.loading.Function;
import net.optionfactory.pebbel.loading.FunctionDescriptor;
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

    @Ignore
    @Test
    public void explore() throws IOException {
        final ClassReader cr = new ClassReader(new FileInputStream("/tmp/Asd.class"));
        TraceClassVisitor traceClassVisitor = new TraceClassVisitor(null, new ASMifier(), new PrintWriter(System.out));
        cr.accept(traceClassVisitor, 0);
    }

    @Test
    public void compileNumberLiteral() {
        final NumberLiteral expression = new NumberLiteral();
        expression.value = 123d;
        final Result<CompiledExpression<Object, Object, Object>> result = new ExpressionCompiler<>().compile(Bindings.empty(), Collections.emptyMap(), expression);
        Assert.assertFalse(result.isError());
        Assert.assertEquals(Double.valueOf(123d), result.getValue().evaluate(Bindings.empty()));
        printGeneratedCode();
    }

    @Test
    public void compileStringLiteral() {
        final StringLiteral expression = new StringLiteral();
        expression.literal = "Hello";
        final Result<CompiledExpression<Object, Object, Object>> result = new ExpressionCompiler<>().compile(Bindings.empty(), Collections.emptyMap(), expression);
        Assert.assertFalse(result.isError());
        Assert.assertEquals("Hello", result.getValue().evaluate(Bindings.empty()));
        printGeneratedCode();
    }

    @Test
    public void compileVariableReference() {
        final Variable expression = Variable.of("VAR1", null);
        final VariableDescriptor<Object> var1 = VariableDescriptor.of("VAR1", Integer.class, null);
        final Result<CompiledExpression<Object, Object, Object>> result = new ExpressionCompiler<>().compile(Bindings.empty(), Map.of("VAR1", var1), expression);
        Assert.assertFalse(result.isError());
        Assert.assertEquals(Integer.valueOf(123), result.getValue().evaluate(Bindings.singleton("VAR1", 123, var1)));
        printGeneratedCode();
    }

    public static String foo() {
        return "Foo";
    }

    public static String hello(String what) {
        return "Hello " + what;
    }

    public static Object bar() {
        return "Bar";
    }

    @Test
    public void compileFunctionCallNullary() throws NoSuchMethodException {
        final FunctionCall expression = FunctionCall.of("foo", new Expression[0], null);
        final MethodHolderFunction fun = new MethodHolderFunction(this.getClass().getMethod("foo"));
        final FunctionDescriptor descriptor = FunctionDescriptor.of("foo", "", false, String.class);
        final Result<CompiledExpression<Object, Object, Object>> result = new ExpressionCompiler<>().compile(Bindings.singleton("foo", fun, descriptor), Collections.emptyMap(), expression);
        Assert.assertFalse(result.isError());
        Assert.assertEquals("Foo", result.getValue().evaluate(Bindings.empty()));
        printGeneratedCode();
    }

    @Test
    public void compileFunctionCallUnary() throws NoSuchMethodException {
        final FunctionCall expression = FunctionCall.of("hello", new Expression[] { StringLiteral.of("world", null)}, null);
        final MethodHolderFunction fun = new MethodHolderFunction(this.getClass().getMethod("hello", String.class));
        final FunctionDescriptor descriptor = FunctionDescriptor.of("hello", "", false, String.class, FunctionDescriptor.ParameterDescriptor.of(String.class, "what"));
        final Result<CompiledExpression<Object, Object, Object>> result = new ExpressionCompiler<>().compile(Bindings.singleton("hello", fun, descriptor), Collections.emptyMap(), expression);
        Assert.assertFalse(result.isError());
        Assert.assertEquals("Hello world", result.getValue().evaluate(Bindings.empty()));
        printGeneratedCode();
    }

    @Test
    public void compileFunctionCallUnaryChain() throws NoSuchMethodException {
        final FunctionCall expression = FunctionCall.of("hello", new Expression[] { FunctionCall.of("foo", new Expression[0], null)}, null);
        final MethodHolderFunction foo = new MethodHolderFunction(this.getClass().getMethod("foo"));
        final MethodHolderFunction hello = new MethodHolderFunction(this.getClass().getMethod("hello", String.class));
        final FunctionDescriptor fooDescriptor = FunctionDescriptor.of("foo", "", false, String.class);
        final FunctionDescriptor helloDescriptor = FunctionDescriptor.of("hello", "", false, String.class, FunctionDescriptor.ParameterDescriptor.of(String.class, "what"));
        final Bindings<String, Function, FunctionDescriptor> functionBindings = Bindings.<String, Function, FunctionDescriptor>singleton("hello", hello, helloDescriptor).overlaying(Bindings.singleton("foo", foo, fooDescriptor));
        final Result<CompiledExpression<Object, Object, Object>> result = new ExpressionCompiler<>().compile(functionBindings, Collections.emptyMap(), expression);
        Assert.assertFalse(result.isError());
        Assert.assertEquals("Hello Foo", result.getValue().evaluate(Bindings.empty()));
        printGeneratedCode();
    }

    @Test
    public void compileFunctionCallUnaryChainCast() throws NoSuchMethodException {
        final FunctionCall expression = FunctionCall.of("hello", new Expression[] { FunctionCall.of("bar", new Expression[0], null)}, null);
        final MethodHolderFunction bar = new MethodHolderFunction(this.getClass().getMethod("bar"));
        final MethodHolderFunction hello = new MethodHolderFunction(this.getClass().getMethod("hello", String.class));
        final FunctionDescriptor barDescriptor = FunctionDescriptor.of("bar", "", false, String.class);
        final FunctionDescriptor helloDescriptor = FunctionDescriptor.of("hello", "", false, String.class, FunctionDescriptor.ParameterDescriptor.of(String.class, "what"));
        final Bindings<String, Function, FunctionDescriptor> functionBindings = Bindings.<String, Function, FunctionDescriptor>singleton("hello", hello, helloDescriptor).overlaying(Bindings.singleton("bar", bar, barDescriptor));
        final Result<CompiledExpression<Object, Object, Object>> result = new ExpressionCompiler<>().compile(functionBindings, Collections.emptyMap(), expression);
        Assert.assertFalse(result.isError());
        Assert.assertEquals("Hello Bar", result.getValue().evaluate(Bindings.empty()));
        printGeneratedCode();
    }

    @Test
    public void compileFunctionCallUnaryVariableCast() throws NoSuchMethodException {
        final VariableDescriptor<Object> var1 = VariableDescriptor.of("VAR1", String.class, null);
        final FunctionCall expression = FunctionCall.of("hello", new Expression[] { Variable.of("VAR1", null) }, null);
        final MethodHolderFunction fun = new MethodHolderFunction(this.getClass().getMethod("hello", String.class));
        final FunctionDescriptor descriptor = FunctionDescriptor.of("hello", "", false, String.class, FunctionDescriptor.ParameterDescriptor.of(String.class, "what"));

        final Result<CompiledExpression<Object, Object, Object>> result = new ExpressionCompiler<>().compile(Bindings.singleton("hello", fun, descriptor), Map.of("VAR1", var1), expression);
        Assert.assertFalse(result.isError());
        Assert.assertEquals("Hello variable", result.getValue().evaluate(Bindings.singleton("VAR1", "variable", var1)));
        printGeneratedCode();
    }

    @Test
    public void compileShortCircuitExpressionOr() {
        final VariableDescriptor<Object> var1 = VariableDescriptor.of("VAR1", Boolean.class, null);
        final ShortCircuitExpression expression = ShortCircuitExpression.of(
                new BooleanOperator[]{BooleanOperator.OR},
                new BooleanExpression[]{Variable.of("VAR1", null), Variable.of("VAR1", null)},
                null);
        final Result<CompiledExpression<Object, Object, Object>> result = new ExpressionCompiler<>().compile(Bindings.empty(), Map.of("VAR1", var1), expression);
        printGeneratedCode();
    }

    @Test
    public void compileShortCircuitExpressionAnd() {
        final VariableDescriptor<Object> var1 = VariableDescriptor.of("VAR1", Boolean.class, null);
        final ShortCircuitExpression expression = ShortCircuitExpression.of(
                new BooleanOperator[]{BooleanOperator.AND},
                new BooleanExpression[]{Variable.of("VAR1", null), Variable.of("VAR1", null)},
                null);
        final Result<CompiledExpression<Object, Object, Object>> result = new ExpressionCompiler<>().compile(Bindings.empty(), Map.of("VAR1", var1), expression);
        printGeneratedCode();
    }

    private void printGeneratedCode() {
        final ClassReader cr = new ClassReader(ExpressionCompiler.lastGeneratedBytecode);
        TraceClassVisitor traceClassVisitor = new TraceClassVisitor(null, new Textifier(), new PrintWriter(System.out));
        cr.accept(traceClassVisitor, 0);
    }
}