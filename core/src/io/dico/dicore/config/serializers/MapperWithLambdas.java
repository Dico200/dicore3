package io.dico.dicore.config.serializers;

import java.util.function.Function;
import java.util.function.UnaryOperator;

public class MapperWithLambdas<TIn, TOut> implements IConfigSerializerMapper<TIn, TOut> {
    private static final Function<?, ?> identity = x -> x;
    private Class<TOut> type;
    private Function<SerializerResult<TIn>, SerializerResult<TOut>> load;
    private Function<TOut, TIn> save;
    
    public MapperWithLambdas(Class<TOut> type, Function<SerializerResult<TIn>, SerializerResult<TOut>> load, Function<TOut, TIn> save) {
        this.type = type;
        this.load = load;
        this.save = save == null ? identity() : save;
    }
    
    public static <T> MapperWithLambdas<T, T> loadOnlyMapper(Class<T> type, UnaryOperator<SerializerResult<T>> load) {
        return new MapperWithLambdas<>(type, load, null);
    }
    
    public static <T> MapperWithLambdas<T, T> simpleLoadOnlyMapper(Class<T> type, UnaryOperator<T> load) {
        return new MapperWithLambdas<>(type, result -> new SerializerResult<>(load.apply(result.value), result.isDefault), null);
    }
    
    @Override
    public SerializerResult<TOut> postLoad(SerializerResult<TIn> in) {
        return load.apply(in);
    }

    @Override
    public TIn preSave(TOut out) {
        return save.apply(out);
    }

    @Override
    public Class<TOut> type() {
        return type;
    }
    
    private static <TIn, TOut> Function<TIn, TOut> identity() {
        //noinspection unchecked
        return (Function<TIn, TOut>) identity;
    }
    
}
