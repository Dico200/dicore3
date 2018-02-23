package io.dico.dicore.config.serializers;

import io.dico.dicore.config.ConfigLogging;

import java.util.function.UnaryOperator;

public interface IConfigSerializer<T> {
    
    SerializerResult<T> load(Object source, ConfigLogging logger);
    
    T defaultValue();
    
    Object serialize(T value);
    
    Class<T> type();
    
    default String inputTypeName() {
        return type().getSimpleName();
    }
    
    default <TOut> IConfigSerializer<TOut> map(IConfigSerializerMapper<T, TOut> mapper) {
        return MappedSerializer.map(BaseConfigSerializer.wrap(this), mapper);
    }
    
    default IConfigSerializer<T> mapLoadOnly(UnaryOperator<SerializerResult<T>> load) {
        return map(MapperWithLambdas.loadOnlyMapper(type(), load));
    }
    
    default IConfigSerializer<T> mapLoadOnlySimple(UnaryOperator<T> load) {
        return map(MapperWithLambdas.simpleLoadOnlyMapper(type(), load));
    }
    
}
