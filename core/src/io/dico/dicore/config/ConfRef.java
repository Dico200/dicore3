package io.dico.dicore.config;

import io.dico.dicore.config.serializers.IConfigSerializer;
import io.dico.dicore.config.serializers.SerializerResult;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Objects;

public class ConfRef<T> implements ConfigEntry {
    protected final String location;
    protected IConfigSerializer<T> serializer;
    protected boolean hasDefaultValue;
    protected T defaultValue;
    protected T value;
    
    protected ConfRef(String location) {
        Objects.requireNonNull(location);
        this.location = location;
    }
    
    public ConfRef(String location, IConfigSerializer<T> serializer) {
        this(location);
        this.serializer = serializer;
        this.value = serializer.defaultValue();
    }
    
    public ConfRef(String location, IConfigSerializer<T> serializer, T defaultValue) {
        this(location, serializer);
        setDefaultValue(defaultValue);
    }
    
    public final T getValue() {
        return value;
    }
    
    public void setValue(T value) {
        this.value = value;
    }
    
    @Override
    public String getLocation() {
        return location;
    }
    
    public void setDefaultValue(T defaultValue) {
        value = defaultValue;
        hasDefaultValue = true;
    }
    
    @Override
    public void load(ConfigurationSection section, ConfigLogging logger) {
        logger.enterPrefix("(" + serializer.type().getSimpleName() + ") ");
        logger.enterPrefix(location);
        try {
            Object source = section.get(location);
            SerializerResult<T> result = serializer.load(source, logger);
            setValue(result.isDefault && hasDefaultValue ? defaultValue : result.value);
        } finally {
            logger.exitPrefix();
            logger.exitPrefix();
        }
    }
    
    @Override
    public final void save(ConfigurationSection section, boolean firstCreated) {
        section.set(location, serializer.serialize(value));
    }
    
    public IConfigSerializer<T> getSerializer() {
        return serializer;
    }
    
    public void setSerializer(IConfigSerializer<T> serializer) {
        //noinspection unchecked
        this.serializer = Objects.requireNonNull(serializer);
    }
    
}
