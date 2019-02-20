package net.optionfactory.pebbel.linking;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TypeChecks {

    public static boolean isAssignable(Class<?> lhs, Class<?> rhs) {
        if (rhs == lhs) {
            return true;
        }
        if (!lhs.isPrimitive() && rhs.isPrimitive()) {
            if (!PRIMITIVE_TO_BOXED.containsKey(rhs)) {
                return false;
            }
            return isAssignable(lhs, PRIMITIVE_TO_BOXED.get(rhs));
        }
        if (lhs.isPrimitive() && !rhs.isPrimitive()) {
            if (!BOXED_TO_PRIMITIVE.containsKey(rhs)) {
                return false;
            }
            return isAssignable(lhs, BOXED_TO_PRIMITIVE.get(rhs));
        }
        if (rhs.isPrimitive()) {
            if (!lhs.isPrimitive()) {
                return false;
            }
            if (rhs == boolean.class || rhs == double.class) {
                return false;
            }
            if (rhs == int.class) {
                return lhs == long.class || lhs == float.class || lhs == double.class;
            }
            if (rhs == long.class) {
                return lhs == float.class || lhs == double.class;
            }
            if (rhs == float.class) {
                return lhs == double.class;
            }
            if (rhs == char.class) {
                return lhs == int.class || lhs == long.class || lhs == float.class || lhs == double.class;
            }
            if (rhs == short.class) {
                return lhs == int.class || lhs == long.class || lhs == float.class || lhs == double.class;
            }
            //byte.class
            return lhs == short.class || lhs == int.class || lhs == long.class || lhs == float.class || lhs == double.class;
        }
        return lhs.isAssignableFrom(rhs);
    }

    private static final Map<Class, Class> PRIMITIVE_TO_BOXED = new ConcurrentHashMap<>();
    private static final Map<Class, Class> BOXED_TO_PRIMITIVE = new ConcurrentHashMap<>();

    static {
        PRIMITIVE_TO_BOXED.put(boolean.class, Boolean.class);
        PRIMITIVE_TO_BOXED.put(byte.class, Byte.class);
        PRIMITIVE_TO_BOXED.put(char.class, Character.class);
        PRIMITIVE_TO_BOXED.put(short.class, Short.class);
        PRIMITIVE_TO_BOXED.put(int.class, Integer.class);
        PRIMITIVE_TO_BOXED.put(long.class, Long.class);
        PRIMITIVE_TO_BOXED.put(double.class, Double.class);
        PRIMITIVE_TO_BOXED.put(float.class, Float.class);
        PRIMITIVE_TO_BOXED.put(void.class, Void.class);

        for (Map.Entry<Class, Class> entry : PRIMITIVE_TO_BOXED.entrySet()) {
            BOXED_TO_PRIMITIVE.put(entry.getValue(), entry.getKey());
        }
    }
}
