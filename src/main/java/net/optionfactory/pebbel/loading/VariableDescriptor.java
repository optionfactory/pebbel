package net.optionfactory.pebbel.loading;

import java.util.Objects;

/**
 * Describes a variable that can be referenced from the DSL.
 * {@code FunctionDescriptor}s are generally created by the {@code Loader}.
 */
public class VariableDescriptor {

    public String name;
    public Class<?> type;
    public String help;
    public String source;

    public int externalId;

    public static VariableDescriptor of(String name, Class<?> type, String help, String source, int externalId) {
        final VariableDescriptor vd = new VariableDescriptor();
        vd.name = name;
        vd.type = type;
        vd.help = help;
        vd.source = source;
        vd.externalId = externalId;
        return vd;
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs instanceof VariableDescriptor == false) {
            return false;
        }
        final VariableDescriptor other = (VariableDescriptor) rhs;
        return Objects.equals(this.name, other.name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }

    @Override
    public String toString() {
        return name;
    }

}
