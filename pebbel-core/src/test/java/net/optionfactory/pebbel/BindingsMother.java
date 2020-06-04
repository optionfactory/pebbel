package net.optionfactory.pebbel;

import java.util.HashMap;
import java.util.Map;
import net.optionfactory.pebbel.loading.Descriptors;
import net.optionfactory.pebbel.loading.FunctionDescriptor;
import net.optionfactory.pebbel.loading.FunctionDescriptor.ParameterDescriptor;
import net.optionfactory.pebbel.loading.VariableDescriptor;

public class BindingsMother {


    private static final Map<String, FunctionDescriptor> FUNCTION_DESCRIPTORS = new HashMap<>();
    private static final Map<String, VariableDescriptor<Object>> VARIABLE_DESCRIPTORS = new HashMap<>();

    public static final Descriptors<Object> DESCRIPTORS = new Descriptors<>();
    
    static {
        FUNCTION_DESCRIPTORS.put("true", FunctionDescriptor.of("true", "no help", false, Boolean.class));
        FUNCTION_DESCRIPTORS.put("false", FunctionDescriptor.of("false", "no help", false, Boolean.class));
        FUNCTION_DESCRIPTORS.put("bool", FunctionDescriptor.of("bool", "no help", false, Boolean.class, ParameterDescriptor.of(Object.class, "value")));
        FUNCTION_DESCRIPTORS.put("not", FunctionDescriptor.of("bool", "no help", false, Boolean.class, ParameterDescriptor.of(Boolean.class, "value")));

        VARIABLE_DESCRIPTORS.put("VAR1", VariableDescriptor.of("VAR1", Object.class, 0));

        
        DESCRIPTORS.functions = FUNCTION_DESCRIPTORS;
        DESCRIPTORS.variables = VARIABLE_DESCRIPTORS;
    }

}
