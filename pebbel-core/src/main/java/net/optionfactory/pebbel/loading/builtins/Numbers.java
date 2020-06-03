package net.optionfactory.pebbel.loading.builtins;

import net.optionfactory.pebbel.loading.BindingDoc;
import net.optionfactory.pebbel.loading.BindingHandler;

/**
 * Number DSL builtin functions. To be loaded via {@code Loader}.
 *
 * @author rferranti
 */
public class Numbers {

    @BindingHandler("num")
    @BindingDoc("converts an object to a numeric value")
    public static Double number(Object value) {
        if (value instanceof String) {
            return Double.parseDouble(value.toString());
        }
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        throw new UnsupportedOperationException("unsupported type");
    }

    @BindingHandler("num:add")
    @BindingDoc("sums two numeric values")
    public static Double plus(Double lhs, Double rhs) {
        return lhs + rhs;
    }

    @BindingHandler("num:sub")
    @BindingDoc("subtracts two numeric values")
    public static Double sub(Double lhs, Double rhs) {
        return lhs - rhs;
    }

    @BindingHandler("num:mul")
    @BindingDoc("multiplies two numeric values")
    public static Double mul(Double lhs, Double rhs) {
        return lhs * rhs;
    }

    @BindingHandler("num:div")
    public static Double div(Double lhs, Double rhs) {
        return lhs / rhs;
    }

    @BindingHandler("num:mod")
    public static Double mod(Double lhs, Double rhs) {
        return lhs % rhs;
    }

    @BindingHandler("num:abs")
    @BindingDoc("Returns the absolute value of a numeric")
    public static Double abs(Double value) {
        return Math.abs(value);
    }

}
