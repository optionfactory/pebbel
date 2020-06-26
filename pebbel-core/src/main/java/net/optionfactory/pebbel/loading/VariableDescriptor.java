package net.optionfactory.pebbel.loading;

import java.util.Objects;

/**
 * Describes a variable that can be referenced from the DSL.
 * {@code FunctionDescriptor}s are generally created by the {@code Loader}.
 */
public class VariableDescriptor<VARMETA> {

    public String name;
    public Class<?> type;
    public VARMETA metadata;

    public static <VARMETA> VariableDescriptor<VARMETA> of(String name, Class<?> type, VARMETA metadata) {
        final VariableDescriptor<VARMETA> vd = new VariableDescriptor<>();
        vd.name = name;
        vd.type = type;
        vd.metadata = metadata;
        return vd;
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs instanceof VariableDescriptor == false) {
            return false;
        }
        final VariableDescriptor<?> other = (VariableDescriptor<?>) rhs;
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
