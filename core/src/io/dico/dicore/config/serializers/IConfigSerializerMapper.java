package io.dico.dicore.config.serializers;

public interface IConfigSerializerMapper<TIn, TOut> {
    
    SerializerResult<TOut> postLoad(SerializerResult<TIn> in);
    
    TIn preSave(TOut out);
    
    Class<TOut> type();
    
    default <TTo> IConfigSerializerMapper<TIn, TTo> andThen(IConfigSerializerMapper<TOut, TTo> mapper) {
        return new IConfigSerializerMapper<TIn, TTo>() {
            @Override
            public SerializerResult<TTo> postLoad(SerializerResult<TIn> in) {
                return mapper.postLoad(IConfigSerializerMapper.this.postLoad(in));
            }
    
            @Override
            public TIn preSave(TTo out) {
                return IConfigSerializerMapper.this.preSave(mapper.preSave(out));
            }
    
            @Override
            public Class<TTo> type() {
                return mapper.type();
            }
        };
    }
    
}
