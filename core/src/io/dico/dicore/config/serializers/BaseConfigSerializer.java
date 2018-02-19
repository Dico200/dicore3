package io.dico.dicore.config.serializers;

import io.dico.dicore.config.ConfigLogging;

abstract class BaseConfigSerializer<T> implements IConfigSerializer<T> {
    
    public abstract SerializerResult<T> loadResult(Object source, ConfigLogging logger);
    
    @Override
    public T load(Object source, ConfigLogging logger) {
        return loadResult(source, logger).value;
    }
    
    public SerializerResult<T> defaultValueResult() {
        return new SerializerResult<>(defaultValue(), true);
    }
    
    static <T> BaseConfigSerializer<T> wrap(IConfigSerializer<T> serializer) {
        if (serializer instanceof BaseConfigSerializer) {
            return (BaseConfigSerializer<T>) serializer;
        }
        return new DelegatedConfigSerializer<T, T, IConfigSerializer<T>>(serializer) {
            @Override
            public SerializerResult<T> loadResult(Object source, ConfigLogging logger) {
                return new SerializerResult<>(load(source, logger));
            }
    
            @Override
            public T load(Object source, ConfigLogging logger) {
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
