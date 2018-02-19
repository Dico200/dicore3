package io.dico.dicore.config.serializers;

public final class SerializerResult<T> {
    private static final SerializerResult<?> defaultNull = new SerializerResult<>(null, true);
    public final T value;
    public final boolean isDefault;
    
    public SerializerResult(T value) {
        this.value = value;
        this.isDefault = false;
    }
    
    public SerializerResult(T value, boolean isDefault) {
        this.value = value;
        this.isDefault = isDefault;
    }
    
    public static <T> SerializerResult<T> defaultNull() {
        //noinspection unchecked
        return (SerializerResult<T>) defaultNull;
    }
    
}
