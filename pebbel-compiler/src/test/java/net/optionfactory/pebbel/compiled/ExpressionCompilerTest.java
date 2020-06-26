package net.optionfactory.pebbel.compiled;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.optionfactory.pebbel.loading.BindingHandler;
import net.optionfactory.pebbel.loading.Bindings;
import net.optionfactory.pebbel.loading.FunctionDescriptor;
import net.optionfactory.pebbel.loading.PebbelFunctionsLoader;
import net.optionfactory.pebbel.loading.VariableDescriptor;
import net.optionfactory.pebbel.parsing.ast.BooleanExpression;
import net.optionfactory.pebbel.parsing.ast.BooleanOperator;
import net.optionfactory.pebbel.parsing.ast.Expression;
import net.optionfactory.pebbel.parsing.ast.FunctionCall;
import net.optionfactory.pebbel.parsing.ast.NumberLiteral;
import net.optionfactory.pebbel.parsing.ast.ShortCircuitExpression;
import net.optionfactory.pebbel.parsing.ast.Source;
import net.optionfactory.pebbel.parsing.ast.StringLiteral;
import net.optionfactory.pebbel.parsing.ast.Variable;
import net.optionfactory.pebbel.results.Result;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.util.ASMifier;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceClassVisitor;

@RunWith(Parameterized.class)
public class ExpressionCompilerTest {

    private final boolean includeDebugInfo;
    private final boolean remapExceptions;

    public ExpressionCompilerTest(boolean includeDebugInfo, boolean remapExceptions) {
        this.includeDebugInfo = includeDebugInfo;
        this.remapExceptions = remapExceptions;
    }

    @Parameterized.Parameters(name = "includeDebugInfo: {0} remapExceptions: {1}")
    public static Collection<Object[]> data() {
        final List<Object[]> objects = new ArrayList<>();
        objects.add(new Object[]{ true, true });
        objects.add(new Object[]{ true, false });
        objects.add(new Object[]{ false, true });
        objects.add(new Object[]{ false, false });
        return objects;
    }

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

        @BindingHandler("varargOnly")
        public static String varargOnly(String... others) {
            return Stream.of(others).collect(Collectors.toList()).toString();
        }
        @BindingHandler("vararg")
        public static String vararg(String first, String... others) {
            return Stream.concat(Stream.of(first), Stream.of(others)).collect(Collectors.toList()).toString();
        }

        @BindingHandler("varargNumber")
        public static String varargNumber(Number first, Number... others) {
            return Stream.concat(Stream.of(first), Stream.of(others)).map(e -> e.toString()).collect(Collectors.toList()).toString();
        }

        @BindingHandler("varargPrimitives")
        public static String varargPrimitives(int first, int... others) {
            return IntStream.concat(IntStream.of(first), IntStream.of(others)).mapToObj(Integer::toString).collect(Collectors.toList()).toString();
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
                "STR", VariableDescriptor.of("STR", String.class, null),
                "INT", VariableDescriptor.of("INT", Integer.class, null),
                "BOOL", VariableDescriptor.of("BOOL", Boolean.class, null),
                "LONG", VariableDescriptor.of("LONG", Long.class, null)
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
        final Result<CompiledExpression.Unloaded<Double>> result = new ExpressionCompiler(includeDebugInfo, remapExceptions).compile(FN_BINDINGS, expression, Double.class);
        Assert.assertFalse(result.isError());
        Assert.assertEquals(Double.valueOf(123d), loadAndInstantiate(result.getValue()).evaluate(Bindings.empty()));
    }

    @Test
    public void compileStringLiteral() {
        final StringLiteral expression = StringLiteral.of("Hello", DUMMY_SOURCE);
        final Result<CompiledExpression.Unloaded<String>> result = new ExpressionCompiler(includeDebugInfo, remapExceptions).compile(FN_BINDINGS, expression, String.class);
        Assert.assertFalse(result.isError());
        Assert.assertEquals("Hello", loadAndInstantiate(result.getValue()).evaluate(Bindings.empty()));
    }

    @Test
    public void compileVariableReference() {
        final Variable expression = Variable.of("INT", DUMMY_SOURCE);
        final Result<CompiledExpression.Unloaded<Integer>> result = new ExpressionCompiler(includeDebugInfo, remapExceptions).compile(FN_BINDINGS, expression, Integer.class);
        Assert.assertFalse(result.isError());
        Assert.assertEquals(Integer.valueOf(123), loadAndInstantiate(result.getValue()).evaluate(Bindings.singleton("INT", 123, VAR_DESCRIPTORS.get("INT"))));
    }

    @Test
    public void compileFunctionCallNullary() {
        final FunctionCall expression = FunctionCall.of("foo", new Expression[0], DUMMY_SOURCE);
        final Result<CompiledExpression.Unloaded<String>> result = new ExpressionCompiler(includeDebugInfo, remapExceptions).compile(FN_BINDINGS, expression, String.class);
        Assert.assertFalse(result.isError());
        Assert.assertEquals(Functions.foo(), loadAndInstantiate(result.getValue()).evaluate(Bindings.empty()));
    }

    @Test
    public void compileFunctionCallUnary() {
        final FunctionCall expression = FunctionCall.of("hello", new Expression[] { StringLiteral.of("world", DUMMY_SOURCE)}, DUMMY_SOURCE);
        final Result<CompiledExpression.Unloaded<String>> result = new ExpressionCompiler(includeDebugInfo, remapExceptions).compile(FN_BINDINGS, expression, String.class);
        Assert.assertFalse(result.isError());
        Assert.assertEquals(Functions.hello("world"), loadAndInstantiate(result.getValue()).evaluate(Bindings.empty()));
    }


    @Test
    public void compileFunctionCallVarargWithOneValue() {
        final FunctionCall expression = FunctionCall.of("vararg", new Expression[] {
                StringLiteral.of("hello", DUMMY_SOURCE),
                StringLiteral.of("world", DUMMY_SOURCE)
        }, DUMMY_SOURCE);
        final Result<CompiledExpression.Unloaded<String>> result = new ExpressionCompiler(includeDebugInfo, remapExceptions).compile(FN_BINDINGS, expression, String.class);
        Assert.assertFalse(result.isError());
        Assert.assertEquals(Functions.vararg("hello", "world"), loadAndInstantiate(result.getValue()).evaluate(Bindings.empty()));
    }

    @Test
    public void compileFunctionCallVarargWithManyValues() {
        final FunctionCall expression = FunctionCall.of("vararg", new Expression[] {
                StringLiteral.of("hello", DUMMY_SOURCE),
                StringLiteral.of("sad", DUMMY_SOURCE),
                StringLiteral.of("world", DUMMY_SOURCE)
        }, DUMMY_SOURCE);
        final Result<CompiledExpression.Unloaded<String>> result = new ExpressionCompiler(includeDebugInfo, remapExceptions).compile(FN_BINDINGS, expression, String.class);
        Assert.assertFalse(result.isError());
        Assert.assertEquals(Functions.vararg("hello", "sad", "world"), loadAndInstantiate(result.getValue()).evaluate(Bindings.empty()));
    }

    @Test
    public void compileFunctionCallVarargWithNoValues() {
        final FunctionCall expression = FunctionCall.of("vararg", new Expression[] {
                StringLiteral.of("hello", DUMMY_SOURCE)
        }, DUMMY_SOURCE);
        final Result<CompiledExpression.Unloaded<String>> result = new ExpressionCompiler(includeDebugInfo, remapExceptions).compile(FN_BINDINGS, expression, String.class);
        Assert.assertFalse(result.isError());
        Assert.assertEquals(Functions.vararg("hello"), loadAndInstantiate(result.getValue()).evaluate(Bindings.empty()));
    }

    @Test
    public void compileFunctionCallVarargOnly() {
        final FunctionCall expression = FunctionCall.of("varargOnly", new Expression[] {
                StringLiteral.of("hello", DUMMY_SOURCE),
                StringLiteral.of("world", DUMMY_SOURCE)
        }, DUMMY_SOURCE);
        final Result<CompiledExpression.Unloaded<String>> result = new ExpressionCompiler(includeDebugInfo, remapExceptions).compile(FN_BINDINGS, expression, String.class);
        Assert.assertFalse(result.isError());
        Assert.assertEquals(Functions.varargOnly("hello", "world"), loadAndInstantiate(result.getValue()).evaluate(Bindings.empty()));
    }


    @Test
    public void compileFunctionCallVarargWithSubtypes() {
        final FunctionCall expression = FunctionCall.of("varargNumber", new Expression[] {
                Variable.of("INT", DUMMY_SOURCE),
                Variable.of("LONG", DUMMY_SOURCE),
                Variable.of("INT", DUMMY_SOURCE)
        }, DUMMY_SOURCE);
        final Result<CompiledExpression.Unloaded<String>> result = new ExpressionCompiler(includeDebugInfo, remapExceptions).compile(FN_BINDINGS, expression, String.class);
        Assert.assertFalse(result.isError());
        Assert.assertEquals(Functions.varargNumber(1,2l,1), loadAndInstantiate(result.getValue()).evaluate(Bindings.root(Map.of("INT", 1, "LONG", 2l), VAR_DESCRIPTORS)));
    }

    @Test
    public void compileFunctionCallVarargWithPrimitives() {
        final FunctionCall expression = FunctionCall.of("varargNumber", new Expression[] {
                Variable.of("INT", DUMMY_SOURCE),
                Variable.of("INT", DUMMY_SOURCE)
        }, DUMMY_SOURCE);
        final Result<CompiledExpression.Unloaded<String>> result = new ExpressionCompiler(includeDebugInfo, remapExceptions).compile(FN_BINDINGS, expression, String.class);
        Assert.assertFalse(result.isError());
        Assert.assertEquals(Functions.varargPrimitives(1,1), loadAndInstantiate(result.getValue()).evaluate(Bindings.root(Map.of("INT", 1), VAR_DESCRIPTORS)));
    }

    @Test
    public void compileFunctionCallVarargWithPrimitivesConversion() {
        final FunctionCall expression = FunctionCall.of("varargNumber", new Expression[] {
                Variable.of("LONG", DUMMY_SOURCE),
                Variable.of("LONG", DUMMY_SOURCE)
        }, DUMMY_SOURCE);
        final Result<CompiledExpression.Unloaded<String>> result = new ExpressionCompiler(includeDebugInfo, remapExceptions).compile(FN_BINDINGS, expression, String.class);
        Assert.assertFalse(result.isError());
        Assert.assertEquals(Functions.varargPrimitives(1,1), loadAndInstantiate(result.getValue()).evaluate(Bindings.root(Map.of("LONG", 1), VAR_DESCRIPTORS)));
    }

    @Test
    public void compileFunctionCallVarargConversion() {
        final FunctionCall expression = FunctionCall.of("vararg", new Expression[] {
                FunctionCall.of("foo", new Expression[0], DUMMY_SOURCE),
                FunctionCall.of("bar", new Expression[0], DUMMY_SOURCE),
                FunctionCall.of("bar", new Expression[0], DUMMY_SOURCE)
        }, DUMMY_SOURCE);
        final Result<CompiledExpression.Unloaded<String>> result = new ExpressionCompiler(includeDebugInfo, remapExceptions).compile(FN_BINDINGS, expression, String.class);
        Assert.assertFalse(result.isError());
        Assert.assertEquals("[Foo, Bar, Bar]", loadAndInstantiate(result.getValue()).evaluate(Bindings.root(Map.of("LONG", 1), VAR_DESCRIPTORS)));
    }


    @Test
    public void compileFunctionCallUnaryChain() {
        final FunctionCall expression = FunctionCall.of("hello", new Expression[] { FunctionCall.of("foo", new Expression[0], DUMMY_SOURCE)}, DUMMY_SOURCE);
        final Result<CompiledExpression.Unloaded<String>> result = new ExpressionCompiler(includeDebugInfo, remapExceptions).compile(FN_BINDINGS, expression, String.class);
        Assert.assertFalse(result.isError());
        Assert.assertEquals(Functions.hello(Functions.foo()), loadAndInstantiate(result.getValue()).evaluate(Bindings.empty()));
    }

    @Test
    public void compileFunctionCallUnaryChainCast() {
        final FunctionCall expression = FunctionCall.of("hello", new Expression[] { FunctionCall.of("bar", new Expression[0], DUMMY_SOURCE)}, DUMMY_SOURCE);
        final Result<CompiledExpression.Unloaded<String>> result = new ExpressionCompiler(includeDebugInfo, remapExceptions).compile(FN_BINDINGS, expression, String.class);
        Assert.assertFalse(result.isError());
        Assert.assertEquals(Functions.hello((String) Functions.bar()), loadAndInstantiate(result.getValue()).evaluate(Bindings.empty()));
    }

    @Test
    public void compileFunctionCallUnaryVariableCast() {
        final FunctionCall expression = FunctionCall.of("hello", new Expression[] { Variable.of("STR", DUMMY_SOURCE) }, DUMMY_SOURCE);
        final Result<CompiledExpression.Unloaded<String>> result = new ExpressionCompiler(includeDebugInfo, remapExceptions).compile(FN_BINDINGS, expression, String.class);
        Assert.assertFalse(result.isError());
        Assert.assertEquals(Functions.hello("variable"), loadAndInstantiate(result.getValue()).evaluate(Bindings.singleton("STR", "variable", VAR_DESCRIPTORS.get("STR"))));
    }

    @Test
    public void compileShortCircuitExpressionOr() {
        final ShortCircuitExpression expression = ShortCircuitExpression.of(
                new BooleanOperator[]{BooleanOperator.OR},
                new BooleanExpression[]{Variable.of("BOOL", DUMMY_SOURCE), Variable.of("BOOL", DUMMY_SOURCE)},
                DUMMY_SOURCE);
        final Result<CompiledExpression.Unloaded<Boolean>> result = new ExpressionCompiler(includeDebugInfo, remapExceptions).compile(FN_BINDINGS, expression, Boolean.class);
        Assert.assertFalse(result.isError());
    }

    @Test
    public void compileShortCircuitExpressionAnd() {
        final ShortCircuitExpression expression = ShortCircuitExpression.of(
                new BooleanOperator[]{BooleanOperator.AND},
                new BooleanExpression[]{Variable.of("BOOL", DUMMY_SOURCE), Variable.of("BOOL", DUMMY_SOURCE)},
                DUMMY_SOURCE);
        final Result<CompiledExpression.Unloaded<Boolean>> result = new ExpressionCompiler(includeDebugInfo, remapExceptions).compile(FN_BINDINGS, expression, Boolean.class);
        Assert.assertFalse(result.isError());
    }

    @Test
    public void compileFunctionCallPrimitive() {
        final FunctionCall expression = FunctionCall.of("baz", new Expression[] {
                Variable.of("INT", DUMMY_SOURCE)
        }, DUMMY_SOURCE);
        final Result<CompiledExpression.Unloaded<Double>> result = new ExpressionCompiler(includeDebugInfo, remapExceptions).compile(FN_BINDINGS, expression, Double.class);
        Assert.assertFalse(result.isError());
        Assert.assertEquals((Double) 123d, loadAndInstantiate(result.getValue()).evaluate(Bindings.singleton("INT", 123, VAR_DESCRIPTORS.get("INT"))));
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsPrimitiveReturnTypes() {
        new ExpressionCompiler(includeDebugInfo, remapExceptions).compile(FN_BINDINGS, NumberLiteral.of(123d, null), double.class);
    }

    @Test
    public void wrapsExceptions() {
        try {
            final FunctionCall expression = FunctionCall.of("boom", new Expression[0], Source.of(1, 2, 3, 4));
            final Result<CompiledExpression.Unloaded<Object>> result = new ExpressionCompiler(includeDebugInfo, remapExceptions).compile(FN_BINDINGS, expression, Object.class);
            Assert.assertFalse(result.isError());
            loadAndInstantiate(result.getValue()).evaluate(Bindings.empty());
            Assert.fail("Should throw");
        } catch (ExecutionException ex) {
            if (!remapExceptions) {
                Assert.fail("Unexpected ExecutionException with remapExceptions disabled");
            }
            final ExecutionException ee = (ExecutionException) ex;
            Assert.assertEquals(1, ee.source.row);
            Assert.assertEquals(2, ee.source.col);
            Assert.assertEquals(3, ee.source.endRow);
            Assert.assertEquals(4, ee.source.endCol);
        } catch (Exception ex) {
            if (remapExceptions) {
                Assert.fail("Should have thrown an ExecutionException with remapExceptions enabled");
            }
        }
    }

    @Test
    public void wrapsExceptionsOnVarFetch() {
        try {
            final Variable expression = Variable.of("STR", Source.of(1,2,3,4));
            final Result<CompiledExpression.Unloaded<String>> result = new ExpressionCompiler(includeDebugInfo, remapExceptions).compile(FN_BINDINGS, expression, String.class);
            Assert.assertFalse(result.isError());
            loadAndInstantiate(result.getValue()).evaluate(Bindings.singleton("STR", 1234, VAR_DESCRIPTORS.get("STR")));
            Assert.fail("Should throw");
        } catch (ExecutionException ex) {
            if (!remapExceptions) {
                Assert.fail("Unexpected ExecutionException with remapExceptions disabled");
            }
            final ExecutionException ee = (ExecutionException) ex;
            Assert.assertEquals(1, ee.source.row);
            Assert.assertEquals(2, ee.source.col);
            Assert.assertEquals(3, ee.source.endRow);
            Assert.assertEquals(4, ee.source.endCol);
        } catch (Exception ex) {
            if (remapExceptions) {
                Assert.fail("Should have thrown an ExecutionException with remapExceptions enabled");
            }
        }
    }

    private static <R> CompiledExpression<R> loadAndInstantiate(CompiledExpression.Unloaded<R> unloaded) {
        printGeneratedCode(unloaded.bytecode);
        return new DynamicLoader().load(unloaded);
    }
    
    private static void printGeneratedCode(byte[] bytecode) {
        if (true) {
            return;
        }
        {
            final ClassReader cr = new ClassReader(bytecode);
            TraceClassVisitor traceClassVisitor = new TraceClassVisitor(null, new Textifier(), new PrintWriter(System.out));
            cr.accept(traceClassVisitor, 0);
        }
        {
            final ClassReader cr = new ClassReader(bytecode);
            TraceClassVisitor traceClassVisitor = new TraceClassVisitor(null, new ASMifier(), new PrintWriter(System.out));
            cr.accept(traceClassVisitor, 0);
        }
    }
}