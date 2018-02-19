package io.dico.dicore.config;

import io.dico.dicore.config.serializers.ArrayConfigSerializer;
import io.dico.dicore.config.serializers.ConfigSerializers;
import io.dico.dicore.config.serializers.DelegatedConfigSerializer;
import io.dico.dicore.config.serializers.IConfigSerializer;
import org.bukkit.configuration.ConfigurationSection;

import java.lang.reflect.Array;
import java.util.Iterator;

public class ConfigArrayReference<T> extends ConfigReference<Object> implements Iterable<T> {
    private int size;
    private boolean forceSize;
    
    public ConfigArrayReference(String location, int size, boolean forceSize, IConfigSerializer<? extends T> serializer) {
        super(location, ConfigSerializers.forArray(serializer, size, forceSize));
        this.size = size;
        this.forceSize = forceSize;
    }
    
    public ConfigArrayReference(String location, int size, IConfigSerializer<? extends T> serializer) {
        this(location, size, true, serializer);
    }
    
    public ConfigArrayReference(String location, IConfigSerializer<? extends T> serializer, Object defaultValue) {
        this(location, Array.getLength(defaultValue), false, serializer);
        // assert serializer.type() == defaultValue.getClass().getComponentType()
        this.value = defaultValue;
    }
    
    @Override
    public void load(ConfigurationSection section, ConfigLogging logger) {
        super.load(section, logger);
        if (!forceSize) {
            size = Array.getLength(value);
        }
    }
    
    @Override
    public IConfigSerializer<?> getSerializer() {
        return ((DelegatedConfigSerializer<?, ?, ?>) serializer).getDelegate();
    }
    
    @Override
    public void setSerializer(IConfigSerializer<?> serializer) {
        this.serializer = ConfigSerializers.forArray(serializer, size, forceSize);
    }
    
    public T objAt(int idx) {
        //noinspection unchecked
        return (T) ((Object[]) value)[Math.min(idx, size - 1)];
    }
    
    public int intAt(int idx) {
        return ((int[]) value)[Math.min(idx, size - 1)];
    }
    
    public long longAt(int idx) {
        return ((long[]) value)[Math.min(idx, size - 1)];
    }
    
    public double doubleAt(int idx) {
        return ((double[]) value)[Math.min(idx, size - 1)];
    }
    
    public boolean boolAt(int idx) {
        return ((boolean[]) value)[Math.min(idx, size - 1)];
    }
    
    public T objForLevel(int level) {
        return objAt(level - 1);
    }
    
    public int intForLevel(int level) {
        return intAt(level - 1);
    }
    
    public long longForLevel(int level) {
        return longAt(level - 1);
    }
    
    public double doubleForLevel(int level) {
        return doubleAt(level - 1);
    }
    
    public boolean boolForLevel(int level) {
        return boolAt(level - 1);
    }
    
    public int getSize() {
        return size;
    }
    
    public boolean isForcedSize() {
        return forceSize;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public Iterator<T> iterator() {
        return ((ArrayConfigSerializer<T, Object>) serializer).iterator(value);
    }
    
}
