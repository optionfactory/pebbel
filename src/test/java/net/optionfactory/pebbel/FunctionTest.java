package net.optionfactory.pebbel;

import net.optionfactory.pebbel.ast.Source;
import net.optionfactory.pebbel.loading.BindingHandler;
import net.optionfactory.pebbel.loading.Bindings;
import net.optionfactory.pebbel.loading.FunctionDescriptor;
import net.optionfactory.pebbel.loading.Symbols;
import net.optionfactory.pebbel.loading.VariableDescriptor;
import net.optionfactory.pebbel.results.Problem;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.optionfactory.pebbel.ast.Expression;
import net.optionfactory.pebbel.ast.FunctionCall;
import net.optionfactory.pebbel.ast.StringLiteral;
import net.optionfactory.pebbel.execution.ExpressionEvaluator;
import net.optionfactory.pebbel.linking.ExpressionVerifier;
import net.optionfactory.pebbel.loading.Descriptors;
import org.junit.Assert;
import org.junit.Test;
import net.optionfactory.pebbel.execution.Function;

/**
 *
 * @author rferranti
 */
public class FunctionTest {

    private static final Bindings<String, Object, VariableDescriptor> NO_VARS = Bindings.empty();

    public static class UnderTest {

        @BindingHandler("vararg_obj_array")
        public static String vararg_obj_array(Object... strs) {
            return Stream.of(strs).map(e -> e.toString()).collect(Collectors.joining());
        }

        @BindingHandler("vararg_obj_array_with_other_args")
        public static String vararg_obj_array_with_other_args(String prefix, Object... strs) {
            return Stream.of(strs).map(e -> e.toString()).collect(Collectors.joining());
        }

        @BindingHandler("vararg_str_array_with_other_args")
        public static String vararg_str_array_with_other_args(String prefix, String... strs) {
            return Stream.of(strs).collect(Collectors.joining());
        }
    }

    @Test
    public void canEvaluateBuiltinWithObjectArrayVararg() {
        final PebbelFunctionsLoader loader = new PebbelFunctionsLoader();
        final Bindings<String, Function, FunctionDescriptor> loaded = loader.load(UnderTest.class).getValue();
        final Expression[] arguments = new Expression[]{
            StringLiteral.of("1", Source.of(0, 0, 0, 0)),
            StringLiteral.of("2", Source.of(0, 0, 0, 0)),
            StringLiteral.of("3", Source.of(0, 0, 0, 0))
        };
        final Symbols symbols = Symbols.of(loaded, NO_VARS);
        final FunctionCall node = FunctionCall.of("vararg_obj_array", arguments, Source.of(0, 0, 0, 0));
        final Object got = new ExpressionEvaluator().visit(node, symbols);
        Assert.assertEquals("123", got);
    }

    @Test
    public void canEvaluateBuiltinWithObjectArrayVarargAndOtherArgs() {
        final PebbelFunctionsLoader loader = new PebbelFunctionsLoader();
        final Bindings<String, Function, FunctionDescriptor> loaded = loader.load(UnderTest.class).getValue();
        final Symbols symbols = Symbols.of(loaded, NO_VARS);
        final Expression[] arguments = new Expression[]{
            StringLiteral.of("prefix", Source.of(0, 0, 0, 0)),
            StringLiteral.of("1", Source.of(0, 0, 0, 0)),
            StringLiteral.of("2", Source.of(0, 0, 0, 0)),
            StringLiteral.of("3", Source.of(0, 0, 0, 0))
        };
        final FunctionCall node = FunctionCall.of("vararg_obj_array_with_other_args", arguments, Source.of(0, 0, 0, 0));
        final Object got = new ExpressionEvaluator().visit(node, symbols);
        Assert.assertEquals("123", got);
    }

    @Test
    public void canEvaluateBuiltinWithStringArrayVarargAndOtherArgs() {
        final PebbelFunctionsLoader loader = new PebbelFunctionsLoader();
        final Bindings<String, Function, FunctionDescriptor> loaded = loader.load(UnderTest.class).getValue();
        final Expression[] arguments = new Expression[]{
            StringLiteral.of("prefix", Source.of(0, 0, 0, 0)),
            StringLiteral.of("1", Source.of(0, 0, 0, 0)),
            StringLiteral.of("2", Source.of(0, 0, 0, 0)),
            StringLiteral.of("3", Source.of(0, 0, 0, 0))
        };
        final Symbols symbols = Symbols.of(loaded, NO_VARS);
        final FunctionCall node = FunctionCall.of("vararg_str_array_with_other_args", arguments, Source.of(0, 0, 0, 0));
        final Object got = new ExpressionEvaluator().visit(node, symbols);
        Assert.assertEquals("123", got);
    }

    @Test
    public void canVerifyBuiltinWithObjectArrayVararg() {
        final PebbelFunctionsLoader loader = new PebbelFunctionsLoader();
        final Bindings<String, Function, FunctionDescriptor> loaded = loader.load(UnderTest.class).getValue();
        final Symbols symbols = Symbols.of(loaded, NO_VARS);
        final Expression[] arguments = new Expression[]{
            StringLiteral.of("1", Source.of(0, 0, 0, 0)),
            StringLiteral.of("2", Source.of(0, 0, 0, 0)),
            StringLiteral.of("3", Source.of(0, 0, 0, 0))
        };
        final FunctionCall node = FunctionCall.of("vararg_obj_array", arguments, Source.of(0, 0, 0, 0));
        final List<Problem> problems = new ExpressionVerifier().verify(Descriptors.from(symbols), node, String.class);
        Assert.assertEquals(Arrays.<Problem>asList(), problems);
    }

    @Test
    public void canVerifyBuiltinWithObjectArrayVarargAndOtherArgs() {
        final PebbelFunctionsLoader loader = new PebbelFunctionsLoader();
        final Bindings<String, Function, FunctionDescriptor> loaded = loader.load(UnderTest.class).getValue();
        final Symbols symbols = Symbols.of(loaded, NO_VARS);
        final Expression[] arguments = new Expression[]{
            StringLiteral.of("prefix", Source.of(0, 0, 0, 0)),
            StringLiteral.of("1", Source.of(0, 0, 0, 0)),
            StringLiteral.of("2", Source.of(0, 0, 0, 0)),
            StringLiteral.of("3", Source.of(0, 0, 0, 0))
        };
        final FunctionCall node = FunctionCall.of("vararg_obj_array_with_other_args", arguments, Source.of(0, 0, 0, 0));
        final List<Problem> problems = new ExpressionVerifier().verify(Descriptors.from(symbols), node, String.class);
        Assert.assertEquals(Arrays.<Problem>asList(), problems);
    }

    @Test
    public void canVerifyBuiltinWithStringArrayVarargAndOtherArgs() {
        final PebbelFunctionsLoader loader = new PebbelFunctionsLoader();
        final Bindings<String, Function, FunctionDescriptor> loaded = loader.load(UnderTest.class).getValue();
        final Symbols symbols = Symbols.of(loaded, NO_VARS);
        final Expression[] arguments = new Expression[]{
            StringLiteral.of("prefix", Source.of(0, 0, 0, 0)),
            StringLiteral.of("1", Source.of(0, 0, 0, 0)),
            StringLiteral.of("2", Source.of(0, 0, 0, 0)),
            StringLiteral.of("3", Source.of(0, 0, 0, 0))
        };
        final FunctionCall node = FunctionCall.of("vararg_str_array_with_other_args", arguments, Source.of(0, 0, 0, 0));
        final List<Problem> problems = new ExpressionVerifier().verify(Descriptors.from(symbols), node, String.class);
        Assert.assertEquals(Arrays.<Problem>asList(), problems);
    }
}
