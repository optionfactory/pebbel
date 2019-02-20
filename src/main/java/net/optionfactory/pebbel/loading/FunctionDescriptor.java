package net.optionfactory.pebbel.loading;

import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Describes a function that can be referenced from the DSL.
 * {@code FunctionDescriptor}s are generally created by the {@code Loader}.
 */
public class FunctionDescriptor {


    public Class<?> returnType;
    public String name;
    public int arity;
    public boolean vararg;
    public ParameterDescriptor[] parameters;
    public String help;

    public static FunctionDescriptor of(String name, String help, boolean vararg, Class<?> returnType, ParameterDescriptor... parameters) {
        final FunctionDescriptor fd = new FunctionDescriptor();
        fd.name = name;
        fd.help = help;
        fd.vararg = vararg;
        fd.arity = parameters.length;
        fd.returnType = returnType;
        fd.parameters = parameters;
        return fd;
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs instanceof FunctionDescriptor == false) {
            return false;
        }
        final FunctionDescriptor other = (FunctionDescriptor) rhs;
        return this.name.equals(other.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        final String params = Stream.of(parameters).map(e -> e.toString()).collect(Collectors.joining(","));
        return String.format("%s/%s (%s) -> %s", name, arity, params, returnType.getSimpleName());
    }

    public static class ParameterDescriptor {

        public Class<?> type;
        public String name;

        public static ParameterDescriptor of(Class<?> type, String name) {
            ParameterDescriptor fp = new ParameterDescriptor();
            fp.type = type;
            fp.name = name;
            return fp;
        }

    }    

}
