package io.dico.dicore.config.serializers;

import io.dico.dicore.config.ConfigLogging;

public abstract class BaseConfigSerializer<T> implements IConfigSerializer<T> {
    private SerializerResult<T> defaultValueResult;
    
    public SerializerResult<T> defaultValueResult() {
        if (defaultValueResult == null) {
            T defaultValue = defaultValue();
            if (defaultValue == null) {
                defaultValueResult = SerializerResult.defaultNull();
            } else {
                defaultValueResult = new SerializerResult<>(defaultValue, true);
            }
        }
        return defaultValueResult;
    }
    
    public static <T> BaseConfigSerializer<T> wrap(IConfigSerializer<T> serializer) {
        if (serializer instanceof BaseConfigSerializer) {
            return (BaseConfigSerializer<T>) serializer;
        }
        return new DelegatedConfigSerializer<T, T, IConfigSerializer<T>>(serializer) {
            @Override
            public SerializerResult<T> load(Object source, ConfigLogging logger) {
                return delegate.load(source, logger);
            }
    
            @Override
            public SerializerResult<T> defaultValueResult() {
                return new SerializerResult<>(defaultValue(), true);
            }
    
            @Override
            public T defaultValue() {
                return serializer.defaultValue();
            }
    
            @Override
            public Object serialize(T value) {
                return serializer.serialize(value);
            }
    
            @Override
            public Class<T> type() {
                return serializer.type();
            }
        };
    }
    
}
