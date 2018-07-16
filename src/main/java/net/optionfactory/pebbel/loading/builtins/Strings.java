package net.optionfactory.pebbel.loading.builtins;

import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.optionfactory.pebbel.loading.BindingDoc;
import net.optionfactory.pebbel.loading.BindingHandler;

/**
 * String DSL builtin functions. To be loaded via {@code Loader}.
 *
 * @author rferranti
 */
public class Strings {

    @BindingHandler("str")
    @BindingDoc("converts a value to a string")
    public static String str(Object value) {
        return value.toString();
    }

    @BindingHandler("str:empty?")
    @BindingDoc("returns true if the string is empty")
    public static Boolean empty(String value) {
        return value.isEmpty();
    }

    @BindingHandler("str:replace")
    public static String replace(String self, String what, String replacement) {
        return self.replace(what, replacement);
    }

    @BindingHandler("str:trim")
    @BindingDoc("returns the trimmed string")
    public static String trim(String self) {
        return self.trim();
    }

    @BindingHandler("str:contains?")
    @BindingDoc("returns true if needle is contained in the string")
    public static Boolean contains(String self, String needle) {
        return self.contains(needle);
    }

    @BindingHandler("str:starts?")
    @BindingDoc("returns true if the string starts with prefix")
    public static Boolean startsWith(String self, String prefix) {
        return self.startsWith(prefix);
    }

    @BindingHandler("str:ends?")
    @BindingDoc("returns true if the string ends with prefix")
    public static Boolean endsWith(String self, String suffix) {
        return self.endsWith(suffix);
    }

    @BindingHandler("str:upper")
    @BindingDoc("converts a string to uppercase")
    public static String toUpperCase(String self) {
        return self.toUpperCase();
    }

    @BindingHandler("str:lower")
    @BindingDoc("converts a string to lowercase")
    public static String toLowerCase(String self) {
        return self.toLowerCase();
    }

    @BindingHandler("str:slice")
    @BindingDoc("slices a string")
    public static String at(String source, double begin, double end) {
        return source.substring((int) begin, (int) end);
    }

    @BindingHandler("str:concat")
    @BindingDoc("concats a list of strings")
    public static String concat(String first, String... others) {
        return first + Stream.of(others).collect(Collectors.joining());
    }

    @BindingHandler("str:join")
    @BindingDoc("joins a list of strings divided by a separator")
    public static String join(String separator, String first, String... others) {
        if (others.length == 0) {
            return first;
        }
        return first + separator + Stream.of(others).collect(Collectors.joining(separator));
    }
}
