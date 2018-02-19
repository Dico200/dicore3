package io.dico.dicore.config.serializers;

import io.dico.dicore.config.ConfigLogging;

abstract class NumberSerializer<T extends Number> extends SimpleConfigSerializer<T> {
    
    public NumberSerializer(Class<T> type, T defaultValue) {
        super(type, defaultValue);
    }
    
    @Override
    public SerializerResult<T> load(Object source, ConfigLogging logger) {
        T rv;
        if (source instanceof Number) {
            rv = select((Number) source);
        } else {
            try {
                rv = parse(String.valueOf(source));
            } catch (Exception ignored) {
                return defaultValueResult();
            }
        }
        return new SerializerResult<>(rv);
    }
    
    protected abstract T parse(String string);
    
    protected abstract T select(Number number);
    
}


