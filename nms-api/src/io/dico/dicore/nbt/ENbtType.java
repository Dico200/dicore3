package io.dico.dicore.nbt;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;

public enum ENbtType {
    BYTE(Byte.class),
    SHORT(Short.class),
    INT(Integer.class),
    LONG(Long.class),
    FLOAT(Float.class),
    DOUBLE(Double.class),
    STRING(String.class),
    BYTE_ARRAY(byte[].class),
    INT_ARRAY(int[].class),
    LIST(INbtList.class),
    MAP(INbtMap.class);
    
    private static Map<Class<?>, ENbtType> classMap;
    
    /**
     * @param o The object
     * @return The {@link ENbtType} of o, or null if o is null or it's not recognized as an NBT object
     * @throws NullPointerException if o is null
     * @implNote identical to {@code valueOf(o.getClass())}
     */
    public static ENbtType of(Object o) {
        return o == null ? null : valueOf(o.getClass());
    }
    
    /**
     * @param clazz The class
     * @return The {@link ENbtType} of clazz, or null if clazz is not recognized as an NBT type
     * @throws NullPointerException if clazz is null
     */
    public static ENbtType valueOf(Class<?> clazz) {
        Objects.requireNonNull(clazz);
        ENbtType result = classMap.get(clazz);
        if (result == null) {
            if (INbtList.class.isAssignableFrom(clazz)) {
                classMap.put(clazz, LIST);
                result = LIST;
            } else if (INbtMap.class.isAssignableFrom(clazz)) {
                classMap.put(clazz, MAP);
                result = MAP;
            }
        }
        return result;
    }
    
    /**
     * Check if the given value is (can be) an nbt value // has an nbt type.
     *
     * @param o the value
     * @return true if and only if o can be an nbt value
     */
    public static boolean isNbtValue(Object o) {
        return of(o) != null;
    }
    
    ENbtType(Class<?> clazz) {
        registerClasses(this, clazz);
    }
    
    private static void registerClasses(ENbtType type, Class<?> clazz) {
        // You can't access static fields from the constructor.
        // Everything is initialized top-down, so the static field must be initialized in this method.
        if (classMap == null) {
            classMap = new IdentityHashMap<>();
        }
        classMap.put(clazz, type);
    }
    
    /**
     * A representation of this NBT type, in the form of NBT_ with the enum constant's name
     *
     * @return A representation of this NBT type
     */
    @Override
    public String toString() {
        return "NBT_" + name();
    }
    
}