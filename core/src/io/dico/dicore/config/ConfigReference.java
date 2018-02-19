package io.dico.dicore.config;

import io.dico.dicore.config.serializers.IConfigSerializer;
import io.dico.dicore.config.serializers.IConfigSerializerMapper;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Objects;

public class ConfigReference<T> implements ConfigEntry {
    protected final String location;
    protected IConfigSerializer<T> serializer;
    protected T value;
    
    public ConfigReference(String location, IConfigSerializer<? extends T> serializer) {
        this.location = location;
        //noinspection unchecked
        this.serializer = (IConfigSerializer<T>) Objects.requireNonNull(serializer);
        this.value = serializer.defaultValue();
    }
    
    public final T getValue() {
        return value;
    }
    
    @Override
    public String getLocation() {
        return location;
    }
    
    @Override
    public void load(ConfigurationSection section, ConfigLogging logger) {
        logger.enterPrefix("(" + serializer.type().getSimpleName() + ") ");
        logger.enterPrefix(location);
        try {
            Object source = section.get(location);
            value = serializer.load(source, logger);
        } finally {
            logger.exitPrefix();
            logger.exitPrefix();
        }
    }
    
    @Override
    public final void save(ConfigurationSection section, boolean firstCreated) {
        section.set(location, serializer.serialize(value));
    }
    
    public IConfigSerializer<?> getSerializer() {
        return serializer;
    }
    
    public void setSerializer(IConfigSerializer<?> serializer) {
        //noinspection unchecked
        this.serializer = (IConfigSerializer<T>) Objects.requireNonNull(serializer);
    }
    
    public void mapSerializer(IConfigSerializerMapper<?, ?> mapper) {
        //noinspection unchecked
        setSerializer(((IConfigSerializer) getSerializer()).map(mapper));
    }
    
}
