package net.optionfactory.pebbel.compiled;

import net.optionfactory.pebbel.loading.BindingHandler;
import net.optionfactory.pebbel.loading.Bindings;
import net.optionfactory.pebbel.loading.FunctionDescriptor;
import net.optionfactory.pebbel.loading.PebbelFunctionsLoader;
import net.optionfactory.pebbel.loading.VariableDescriptor;
import net.optionfactory.pebbel.parsing.ast.*;
import net.optionfactory.pebbel.results.Result;
import org.junit.After;
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
import java.lang.reflect.Method;
import java.util.Map;
import java.util.function.Function;


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

        @BindingHandler("baz")
        public static double baz(int i) {
            return (double) i;
        }

        @BindingHandler("boom")
        public static Object boom() {
            throw new IllegalStateException();
        }
    }

    private static Bindings<String, Method, FunctionDescriptor> FN_BINDINGS;
    private static Map<String, VariableDescriptor<Object>> VAR_DESCRIPTORS;

    static {
        final PebbelFunctionsLoader<Method> fl = new PebbelFunctionsLoader<>(Function.identity());
        final Result<Bindings<String, Method, FunctionDescriptor>> load = fl.load(Functions.class);
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
        final ClassReader cr = new ClassReader(new FileInputStream("/home/fdegrassi/projects/pebbel2/pebbel-compiler/target/test-classes/net/optionfactory/pebbel/compiled/Asd.class"));
        TraceClassVisitor traceClassVisitor = new TraceClassVisitor(null, new ASMifier(), new PrintWriter(System.out));
        cr.accept(traceClassVisitor, 0);
        TraceClassVisitor traceClassVisitor2 = new TraceClassVisitor(null, new Textifier(), new PrintWriter(System.out));
        final ClassReader cr2 = new ClassReader(new FileInputStream("/home/fdegrassi/projects/pebbel2/pebbel-compiler/target/test-classes/net/optionfactory/pebbel/compiled/Asd.class"));
        cr2.accept(traceClassVisitor2, 0);
    }

    private static final Source DUMMY_SOURCE = Source.of(1,1,1,10);

    @Test
    public void compileNumberLiteral() {
        final NumberLiteral expression = NumberLiteral.of(123d, DUMMY_SOURCE); // var expr = "123"
        final Result<CompiledExpression<Object, Object, Double>> result = new ExpressionCompiler<>().compile(FN_BINDINGS, VAR_DESCRIPTORS, expression, Double.class);
        Assert.assertFalse(result.isError());
        Assert.assertEquals(Double.valueOf(123d), result.getValue().evaluate(Bindings.empty()));
        printGeneratedCode();
    }

    @Test
    public void compileStringLiteral() {
        final StringLiteral expression = StringLiteral.of("Hello", DUMMY_SOURCE);
        final Result<CompiledExpression<Object, Object, String>> result = new ExpressionCompiler<>().compile(FN_BINDINGS, VAR_DESCRIPTORS, expression, String.class);
        Assert.assertFalse(result.isError());
        Assert.assertEquals("Hello", result.getValue().evaluate(Bindings.empty()));
        printGeneratedCode();
    }

    @Test
    public void compileVariableReference() {
        final Variable expression = Variable.of("INT", DUMMY_SOURCE);
        final Result<CompiledExpression<Object, Object, Integer>> result = new ExpressionCompiler<>().compile(FN_BINDINGS, VAR_DESCRIPTORS, expression, Integer.class);
        Assert.assertFalse(result.isError());
        Assert.assertEquals(Integer.valueOf(123), result.getValue().evaluate(Bindings.singleton("INT", 123, VAR_DESCRIPTORS.get("INT"))));
        printGeneratedCode();
    }

    @Test
    public void compileFunctionCallNullary() {
        final FunctionCall expression = FunctionCall.of("foo", new Expression[0], DUMMY_SOURCE);
        final Result<CompiledExpression<Object, Object, String>> result = new ExpressionCompiler<>().compile(FN_BINDINGS, VAR_DESCRIPTORS, expression, String.class);
        Assert.assertFalse(result.isError());
        Assert.assertEquals(Functions.foo(), result.getValue().evaluate(Bindings.empty()));
        printGeneratedCode();
    }

    @Test
    public void compileFunctionCallUnary() {
        final FunctionCall expression = FunctionCall.of("hello", new Expression[] { StringLiteral.of("world", DUMMY_SOURCE)}, DUMMY_SOURCE);
        final Result<CompiledExpression<Object, Object, String>> result = new ExpressionCompiler<>().compile(FN_BINDINGS, VAR_DESCRIPTORS, expression, String.class);
        Assert.assertFalse(result.isError());
        Assert.assertEquals(Functions.hello("world"), result.getValue().evaluate(Bindings.empty()));
        printGeneratedCode();
    }

    @Test
    public void compileFunctionCallUnaryChain() {
        final FunctionCall expression = FunctionCall.of("hello", new Expression[] { FunctionCall.of("foo", new Expression[0], DUMMY_SOURCE)}, DUMMY_SOURCE);
        final Result<CompiledExpression<Object, Object, String>> result = new ExpressionCompiler<>().compile(FN_BINDINGS, VAR_DESCRIPTORS, expression, String.class);
        Assert.assertFalse(result.isError());
        Assert.assertEquals(Functions.hello(Functions.foo()), result.getValue().evaluate(Bindings.empty()));
        printGeneratedCode();
    }

    @Test
    public void compileFunctionCallUnaryChainCast() {
        final FunctionCall expression = FunctionCall.of("hello", new Expression[] { FunctionCall.of("bar", new Expression[0], DUMMY_SOURCE)}, DUMMY_SOURCE);
        final Result<CompiledExpression<Object, Object, String>> result = new ExpressionCompiler<>().compile(FN_BINDINGS, VAR_DESCRIPTORS, expression, String.class);
        Assert.assertFalse(result.isError());
        Assert.assertEquals(Functions.hello((String) Functions.bar()), result.getValue().evaluate(Bindings.empty()));
        printGeneratedCode();
    }

    @Test
    public void compileFunctionCallUnaryVariableCast() {
        final FunctionCall expression = FunctionCall.of("hello", new Expression[] { Variable.of("STR", DUMMY_SOURCE) }, DUMMY_SOURCE);
        final Result<CompiledExpression<Object, Object, String>> result = new ExpressionCompiler<>().compile(FN_BINDINGS, VAR_DESCRIPTORS, expression, String.class);
        Assert.assertFalse(result.isError());
        Assert.assertEquals(Functions.hello("variable"), result.getValue().evaluate(Bindings.singleton("STR", "variable", VAR_DESCRIPTORS.get("STR"))));
        printGeneratedCode();
    }

    @Test
    public void compileShortCircuitExpressionOr() {
        final ShortCircuitExpression expression = ShortCircuitExpression.of(
                new BooleanOperator[]{BooleanOperator.OR},
                new BooleanExpression[]{Variable.of("BOOL", DUMMY_SOURCE), Variable.of("BOOL", DUMMY_SOURCE)},
                DUMMY_SOURCE);
        final Result<CompiledExpression<Object, Object, Boolean>> result = new ExpressionCompiler<>().compile(FN_BINDINGS, VAR_DESCRIPTORS, expression, Boolean.class);
        Assert.assertFalse(result.isError());
        printGeneratedCode();
    }

    @Test
    public void compileShortCircuitExpressionAnd() {
        final ShortCircuitExpression expression = ShortCircuitExpression.of(
                new BooleanOperator[]{BooleanOperator.AND},
                new BooleanExpression[]{Variable.of("BOOL", DUMMY_SOURCE), Variable.of("BOOL", DUMMY_SOURCE)},
                DUMMY_SOURCE);
        final Result<CompiledExpression<Object, Object, Boolean>> result = new ExpressionCompiler<>().compile(FN_BINDINGS, VAR_DESCRIPTORS, expression, Boolean.class);
        Assert.assertFalse(result.isError());
        printGeneratedCode();
    }

    @Test
    public void compileFunctionCallPrimitive() {
        final FunctionCall expression = FunctionCall.of("baz", new Expression[] {
                Variable.of("INT", DUMMY_SOURCE)
        }, DUMMY_SOURCE);
        final Result<CompiledExpression<Object, Object, Double>> result = new ExpressionCompiler<>().compile(FN_BINDINGS, VAR_DESCRIPTORS, expression, Double.class);
        Assert.assertFalse(result.isError());
        Assert.assertEquals((Double) 123d, result.getValue().evaluate(Bindings.singleton("INT", 123, VAR_DESCRIPTORS.get("INT"))));
        printGeneratedCode();
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsPrimitiveReturnTypes() {
        new ExpressionCompiler<>().compile(FN_BINDINGS, VAR_DESCRIPTORS, NumberLiteral.of(123d, null), double.class);
    }

    @Test
    public void wrapsExceptions() {
        try {
            final FunctionCall expression = FunctionCall.of("boom", new Expression[0], Source.of(1, 2, 3, 4));
            final Result<CompiledExpression<Object, Object, Object>> result = new ExpressionCompiler<>().compile(FN_BINDINGS, VAR_DESCRIPTORS, expression, Object.class);
            Assert.assertFalse(result.isError());
            result.getValue().evaluate(Bindings.empty());
            Assert.fail("Should throw");
        } catch (ExecutionException ex) {
            final ExecutionException ee = (ExecutionException) ex;
            Assert.assertEquals(1, ee.source.row);
            Assert.assertEquals(2, ee.source.col);
            Assert.assertEquals(3, ee.source.endRow);
            Assert.assertEquals(4, ee.source.endCol);
        }
    }

    @After
    public void printGeneratedCode() {
        if (ExpressionCompiler.lastGeneratedBytecode == null) {
            System.out.println("No bytecode generated");
            return;
        }
        {
            final ClassReader cr = new ClassReader(ExpressionCompiler.lastGeneratedBytecode);
            TraceClassVisitor traceClassVisitor = new TraceClassVisitor(null, new Textifier(), new PrintWriter(System.out));
            cr.accept(traceClassVisitor, 0);
        }
        {
            final ClassReader cr = new ClassReader(ExpressionCompiler.lastGeneratedBytecode);
            TraceClassVisitor traceClassVisitor = new TraceClassVisitor(null, new ASMifier(), new PrintWriter(System.out));
            cr.accept(traceClassVisitor, 0);
        }
    }
}