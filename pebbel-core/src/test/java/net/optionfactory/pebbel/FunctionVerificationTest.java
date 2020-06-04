package net.optionfactory.pebbel;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import net.optionfactory.pebbel.loading.PebbelFunctionsLoader;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.optionfactory.pebbel.parsing.ast.Expression;
import net.optionfactory.pebbel.parsing.ast.FunctionCall;
import net.optionfactory.pebbel.parsing.ast.Source;
import net.optionfactory.pebbel.parsing.ast.StringLiteral;
import net.optionfactory.pebbel.verification.ExpressionVerifier;
import net.optionfactory.pebbel.loading.BindingHandler;
import net.optionfactory.pebbel.loading.Bindings;
import net.optionfactory.pebbel.loading.Descriptors;
import net.optionfactory.pebbel.loading.FunctionDescriptor;
import net.optionfactory.pebbel.loading.LoadingException;
import net.optionfactory.pebbel.loading.Symbols;
import net.optionfactory.pebbel.loading.VariableDescriptor;
import net.optionfactory.pebbel.results.Problem;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author rferranti
 */
public class FunctionVerificationTest {

    private static final Bindings<String, Object, VariableDescriptor<Object>> NO_VARS = Bindings.empty();

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
    public void canVerifyBuiltinWithObjectArrayVararg() {
        final PebbelFunctionsLoader<MethodHandle> loader = new PebbelFunctionsLoader<>(this::unreflect);
        final Bindings<String, MethodHandle, FunctionDescriptor> loaded = loader.load(UnderTest.class).getValue();
        final Symbols<Object, Object, MethodHandle> symbols = Symbols.of(loaded, NO_VARS);
        final Expression[] arguments = new Expression[]{
            StringLiteral.of("1", Source.of(0, 0, 0, 0)),
            StringLiteral.of("2", Source.of(0, 0, 0, 0)),
            StringLiteral.of("3", Source.of(0, 0, 0, 0))
        };
        final FunctionCall node = FunctionCall.of("vararg_obj_array", arguments, Source.of(0, 0, 0, 0));
        final List<Problem> problems = new ExpressionVerifier<>().verify(Descriptors.from(symbols), node, String.class);
        Assert.assertEquals(Arrays.<Problem>asList(), problems);
    }

    @Test
    public void canVerifyBuiltinWithObjectArrayVarargAndOtherArgs() {
        final PebbelFunctionsLoader<MethodHandle> loader = new PebbelFunctionsLoader<>(this::unreflect);
        final Bindings<String, MethodHandle, FunctionDescriptor> loaded = loader.load(UnderTest.class).getValue();
        final Symbols<Object, Object, MethodHandle> symbols = Symbols.of(loaded, NO_VARS);
        final Expression[] arguments = new Expression[]{
            StringLiteral.of("prefix", Source.of(0, 0, 0, 0)),
            StringLiteral.of("1", Source.of(0, 0, 0, 0)),
            StringLiteral.of("2", Source.of(0, 0, 0, 0)),
            StringLiteral.of("3", Source.of(0, 0, 0, 0))
        };
        final FunctionCall node = FunctionCall.of("vararg_obj_array_with_other_args", arguments, Source.of(0, 0, 0, 0));
        final List<Problem> problems = new ExpressionVerifier<>().verify(Descriptors.from(symbols), node, String.class);
        Assert.assertEquals(Arrays.<Problem>asList(), problems);
    }

    @Test
    public void canVerifyBuiltinWithStringArrayVarargAndOtherArgs() {
        final PebbelFunctionsLoader<MethodHandle> loader = new PebbelFunctionsLoader<>(this::unreflect);
        final Bindings<String, MethodHandle, FunctionDescriptor> loaded = loader.load(UnderTest.class).getValue();
        final Symbols<Object, Object, MethodHandle> symbols = Symbols.of(loaded, NO_VARS);
        final Expression[] arguments = new Expression[]{
            StringLiteral.of("prefix", Source.of(0, 0, 0, 0)),
            StringLiteral.of("1", Source.of(0, 0, 0, 0)),
            StringLiteral.of("2", Source.of(0, 0, 0, 0)),
            StringLiteral.of("3", Source.of(0, 0, 0, 0))
        };
        final FunctionCall node = FunctionCall.of("vararg_str_array_with_other_args", arguments, Source.of(0, 0, 0, 0));
        final List<Problem> problems = new ExpressionVerifier<>().verify(Descriptors.from(symbols), node, String.class);
        Assert.assertEquals(Arrays.<Problem>asList(), problems);
    }

    private MethodHandle unreflect(Method m) {
        try {
            return MethodHandles.publicLookup().unreflect(m);
        } catch (IllegalAccessException ex) {
            throw new LoadingException(ex);
        }

    }
}
