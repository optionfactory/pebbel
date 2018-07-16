package net.optionfactory.pebbel.loading.builtins;

import net.optionfactory.pebbel.loading.BindingDoc;
import net.optionfactory.pebbel.loading.BindingHandler;

/**
 * Boolean DSL builtin functions. To be loaded via {@code Loader}.
 *
 * @author rferranti
 */
public class Booleans {

    @BindingHandler("bool")
    @BindingDoc("transforms the input into a Boolean")
    public static Boolean bool(Object value) {
        if (value == null) {
            throw new IllegalArgumentException("cannot transform a null value to bool");
        }
        return Boolean.parseBoolean(value.toString().toLowerCase());
    }

    @BindingHandler("true")
    @BindingDoc("returns the true value")
    public static Boolean makeTrue() {
        return true;
    }

    @BindingHandler("false")
    @BindingDoc("returns the false value")
    public static Boolean makeFalse() {
        return false;
    }

    @BindingHandler("not")
    @BindingDoc("negates a Boolean")
    public static Boolean not(Boolean value) {
        return !value;
    }
}
