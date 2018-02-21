package io.dico.dicore.config;

import io.dico.dicore.config.serializers.ArrayConfigSerializer;
import io.dico.dicore.config.serializers.ConfigSerializers;
import io.dico.dicore.config.serializers.IConfigSerializer;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Iterator;

public class ConfArrayRef<T> extends ConfRef<Object> implements Iterable<T> {
    private int size;
    
    public ConfArrayRef(String location, ArrayConfigSerializer<T, ?> serializer) {
        //noinspection unchecked
        super(location, (IConfigSerializer<Object>) serializer);
        size = serializer.getArraySize();
    }
    
    public ConfArrayRef(String location, ArrayConfigSerializer<T, ?> serializer, Object defaultValue) {
        this(location, serializer);
        setDefaultValue(defaultValue);
    }
    
    public ConfArrayRef(String location, IConfigSerializer<T> serializer, int size, boolean forceSize) {
        this(location, ConfigSerializers.forArray(serializer, size, forceSize));
    }
    
    public ConfArrayRef(String location, IConfigSerializer<T> serializer, int size, boolean forceSize, Object defaultValue) {
        this(location, ConfigSerializers.forArray(serializer, size, forceSize), defaultValue);
    }
    
    @Override
    public void setDefaultValue(Object defaultValue) {
        if (defaultValue.getClass() != getArraySerializer().getArrayClass()) {
            throw new IllegalArgumentException();
        }
        super.setDefaultValue(defaultValue);
    }
    
    @Override
    public void load(ConfigurationSection section, ConfigLogging logger) {
        super.load(section, logger);
        if (!isForceSize()) {
            size = getArraySerializer().sizeOf(value);
        }
    }
    
    @Override
    public ArrayConfigSerializer<T, Object> getSerializer() {
        return getArraySerializer();
    }
    
    @Override
    public void setSerializer(IConfigSerializer<Object> serializer) {
        if (!(serializer instanceof ArrayConfigSerializer)) {
            throw new IllegalArgumentException();
        }
        super.setSerializer(serializer);
    }
    
    public ArrayConfigSerializer<T, Object> getArraySerializer() {
        return ((ArrayConfigSerializer<T, Object>) serializer);
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
    
    public boolean isForceSize() {
        return getArraySerializer().isForceSize();
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public Iterator<T> iterator() {
        return getArraySerializer().iterator(value);
    }
    
}
