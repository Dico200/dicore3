package io.dico.dicore.config.serializers;

import io.dico.dicore.config.ConfigLogging;

public interface IConfigSerializer<T> {
    
    T load(Object source, ConfigLogging logger);
    
    T defaultValue();
    
    Object serialize(T value);
    
    Class<T> type();
    
    default String inputTypeName() {
        return type().getSimpleName();
    }
    
    default <TOut> IConfigSerializer<TOut> map(IConfigSerializerMapper<T, TOut> mapper) {
        return MappedSerializer.map(BaseConfigSerializer.wrap(this), mapper);
    }
    
}
