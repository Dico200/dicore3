package io.dico.dicore.config.serializers;

import io.dico.dicore.config.ConfigLogging;

public abstract class MappedSerializer<TIn, TOut> extends DelegatedConfigSerializer<TIn, TOut, BaseConfigSerializer<TIn>> {
    
    public MappedSerializer(BaseConfigSerializer<TIn> delegate) {
        super(delegate);
    }
    
    @Override
    public SerializerResult<TOut> load(Object source, ConfigLogging logger) {
        return postLoad(delegate.load(source, logger));
    }
    
    @Override
    public Object serialize(TOut value) {
        return delegate.serialize(preSave(value));
    }
    
    @Override
    public SerializerResult<TOut> defaultValueResult() {
        return postLoad(delegate.defaultValueResult());
    }
    
    @Override
    public TOut defaultValue() {
        return defaultValueResult().value;
    }
    
    protected abstract SerializerResult<TOut> postLoad(SerializerResult<TIn> in);
    
    protected abstract TIn preSave(TOut out);
    
    public static <TIn, TOut> MappedSerializer<TIn, TOut> map(BaseConfigSerializer<TIn> from, IConfigSerializerMapper<TIn, TOut> mapper) {
        return new MapperMappedSerializer<>(from, mapper);
    }
    
}
