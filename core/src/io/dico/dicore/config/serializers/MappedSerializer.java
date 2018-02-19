package io.dico.dicore.config.serializers;

import io.dico.dicore.config.ConfigLogging;

abstract class MappedSerializer<TIn, TOut> extends DelegatedConfigSerializer<TIn, TOut, BaseConfigSerializer<TIn>> {
    
    MappedSerializer(BaseConfigSerializer<TIn> delegate) {
        super(delegate);
    }
    
    @Override
    public SerializerResult<TOut> loadResult(Object source, ConfigLogging logger) {
        return postLoad(delegate.loadResult(source, logger));
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
    
    static <TIn, TOut> MappedSerializer<TIn, TOut> map(BaseConfigSerializer<TIn> from, IConfigSerializerMapper<TIn, TOut> mapper) {
        return new WithLambda<>(from, mapper);
    }
    
    private static final class WithLambda<TIn, TOut> extends MappedSerializer<TIn, TOut> {
        private final IConfigSerializerMapper<TIn, TOut> mapper;
        
        public WithLambda(BaseConfigSerializer<TIn> delegate, IConfigSerializerMapper<TIn, TOut> mapper) {
            super(delegate);
            this.mapper = mapper;
        }
    
        @Override
        protected SerializerResult<TOut> postLoad(SerializerResult<TIn> in) {
            return mapper.postLoad(in);
        }
    
        @Override
        protected TIn preSave(TOut out) {
            return mapper.preSave(out);
        }
    
        @Override
        public Class<TOut> type() {
            return mapper.type();
        }
    }
    
}
