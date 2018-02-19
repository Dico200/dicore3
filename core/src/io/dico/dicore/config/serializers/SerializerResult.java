package io.dico.dicore.config.serializers;

final class SerializerResult<T> {
    public T value;
    public boolean isDefault;
    
    public SerializerResult(T value) {
        this.value = value;
    }
    
    public SerializerResult(T value, boolean isDefault) {
        this.value = value;
        this.isDefault = isDefault;
    }
    
}
