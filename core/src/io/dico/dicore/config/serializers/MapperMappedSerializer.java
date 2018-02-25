package io.dico.dicore.config.serializers;

public final class MapperMappedSerializer<TIn, TOut> extends MappedSerializer<TIn, TOut> {
    private final IConfigSerializerMapper<TIn, TOut> mapper;
    
    public MapperMappedSerializer(BaseConfigSerializer<TIn> delegate, IConfigSerializerMapper<TIn, TOut> mapper) {
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
    
    public IConfigSerializerMapper<TIn, TOut> getMapper() {
        return mapper;
    }
    
}
