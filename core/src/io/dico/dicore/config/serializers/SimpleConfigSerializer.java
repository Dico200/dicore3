package io.dico.dicore.config.serializers;

public abstract class SimpleConfigSerializer<T> extends BaseConfigSerializer<T> {
    private Class<T> type;
    private T defaultValue;
    
    public SimpleConfigSerializer(Class<T> type, T defaultValue) {
        this.type = type;
        this.defaultValue = defaultValue;
    }
    
    @Override
    public T defaultValue() {
        return defaultValue;
    }
    
    @Override
    public Object serialize(T value) {
        return value;
    }
    
    @Override
    public String inputTypeName() {
        return type.getSimpleName();
    }
    
    @Override
    public Class<T> type() {
        return type;
    }
    
}
