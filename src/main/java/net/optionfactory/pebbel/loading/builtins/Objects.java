package net.optionfactory.pebbel.loading.builtins;

import java.util.Arrays;
import net.optionfactory.pebbel.loading.BindingDoc;
import net.optionfactory.pebbel.loading.BindingHandler;

/**
 * Object DSL builtin functions. To be loaded via {@code Loader}.
 *
 * @author rferranti
 */
public class Objects {

    @BindingHandler("eq?")
    @BindingDoc("returns true if lhs and rhs are equals")
    public static Boolean eq(Object lhs, Object rhs) {
        return java.util.Objects.deepEquals(lhs, rhs);
    }

    @BindingHandler("gt?")
    @BindingDoc("returns true if lhs is greater than rhs")
    public static Boolean gt(Comparable lhs, Comparable rhs) {
        return lhs.compareTo(rhs) > 0;
    }

    @BindingHandler("gte?")
    @BindingDoc("returns true if lhs is greater than or equal to rhs")
    public static Boolean gte(Comparable lhs, Comparable rhs) {
        return lhs.compareTo(rhs) >= 0;
    }

    @BindingHandler("lt?")
    @BindingDoc("returns true if lhs is lesser than rhs")
    public static Boolean lt(Comparable lhs, Comparable rhs) {
        return lhs.compareTo(rhs) < 0;
    }

    @BindingHandler("lte?")
    @BindingDoc("returns true if lhs is lesser than or equal to rhs")
    public static Boolean lte(Comparable lhs, Comparable rhs) {
        return lhs.compareTo(rhs) <= 0;
    }

    @BindingHandler("not_eq?")
    @BindingDoc("returns true if lhs and rhs are not equals")
    public static Boolean neq(Object lhs, Object rhs) {
        return !java.util.Objects.deepEquals(lhs, rhs);
    }

    @BindingHandler("in?")
    public static Boolean in(Object needle, Object... haystack) {
        return Arrays.asList(haystack).contains(needle);
    }

    @BindingHandler("not_in?")
    public static Boolean notIn(Object needle, Object... haystack) {
        return !Arrays.asList(haystack).contains(needle);
    }

    @BindingHandler("null?")
    @BindingDoc("returns true if argument is null")
    public static Boolean isNull(Object source) {
        return source == null;
    }

    @BindingHandler("not_null?")
    @BindingDoc("returns true if argument is not null")
    public static Boolean isNotNull(Object source) {
        return source != null;
    }

}
