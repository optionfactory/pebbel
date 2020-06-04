package net.optionfactory.pebbel;

import java.lang.invoke.MethodHandle;
import net.optionfactory.pebbel.loading.PebbelFunctionsLoader;
import net.optionfactory.pebbel.loading.BindingHandler;
import net.optionfactory.pebbel.loading.Bindings;
import net.optionfactory.pebbel.loading.FunctionDescriptor;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author rferranti
 */
public class FunctionsLoaderTest {

    public static class SimpleHandler {

        @BindingHandler("null?")
        public static boolean isNull(Object obj) {
            return obj == null;
        }
    }
    private final PebbelFunctionsLoader<MethodHandle> FL = new PebbelFunctionsLoader<>((method) -> null);

    @Test
    public void canLoadSimpleHandler() {
        final Bindings<String, MethodHandle, FunctionDescriptor> loaded = FL.load(SimpleHandler.class).getValue();
        Assert.assertTrue(loaded.descriptor("null?").isPresent());
    }

    @Test
    public void arityIsDeduced() {
        final Bindings<String, MethodHandle, FunctionDescriptor> loaded = FL.load(SimpleHandler.class).getValue();
        Assert.assertEquals(1, loaded.descriptor("null?").get().arity);
    }

    @Test
    public void nameIsTakenFromAnnotation() {
        final Bindings<String, MethodHandle, FunctionDescriptor> loaded = FL.load(SimpleHandler.class).getValue();

        Assert.assertEquals("null?", loaded.descriptor("null?").get().name);
    }

    @Test
    public void parametersIsFilled() {
        final Bindings<String, MethodHandle, FunctionDescriptor> loaded = FL.load(SimpleHandler.class).getValue();

        Assert.assertEquals(1, loaded.descriptor("null?").get().parameters.length);
    }

}
