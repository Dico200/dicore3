package io.dico.dicore.nms.v1_8_R3;

import io.dico.dicore.Reflection;
import io.dico.dicore.nbt.ENbtType;
import io.dico.dicore.nbt.INbtList;
import io.dico.dicore.nbt.INbtMap;
import net.minecraft.server.v1_8_R3.*;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Supplier;

// don't extend AbstractMap because of the field overhead of its keySet and values fields.
final class WrappedNbtMap implements INbtMap {
    private static final Field compoundMapField = Reflection.restrictedSearchField(NBTTagCompound.class, "map");
    NBTTagCompound delegate;
    Map<String, NBTBase> map;
    
    WrappedNbtMap(NBTTagCompound delegate) {
        setDelegate(delegate);
    }
    
    void setDelegate(NBTTagCompound delegate) {
        this.delegate = Objects.requireNonNull(delegate);
    
        Map<String, NBTBase> map = Reflection.getFieldValue(compoundMapField, delegate);
        if (map == null) {
            Reflection.setFieldValue(compoundMapField, delegate, map = new HashMap<>());
        }
        this.map = map;
    }
    
    //#####################
    // Map methods
    //#####################
    
    @Override
    public int size() {
        return map.size();
    }
    
    @Override
    public boolean isEmpty() {
        return size() == 0;
    }
    
    @Override
    public boolean containsKey(Object key) {
        return map.containsKey(key);
    }
    
    @Override
    public boolean containsKeyOfType(String key, ENbtType type) {
        NBTBase value = map.get(key);
        return value != null && NBT.getElementType(value.getTypeId()) == type;
    }
    
    @Override
    public boolean containsValue(Object value) {
        return ENbtType.isNbtValue(value) && map.containsValue(NBT.toNativeNbt(value));
    }
    
    @Override
    public Object get(Object key) {
        return NBT.fromNativeNbt(map.get(key));
    }
    
    @Override
    public Object put(String key, Object value) {
        return NBT.fromNativeNbt(map.put(key, NBT.toNativeNbt(value)));
    }
    
    @Override
    public Object remove(Object key) {
        return NBT.fromNativeNbt(map.remove(key));
    }
    
    @Override
    public void putAll(Map<? extends String, ?> map) {
        for (Entry<? extends String, ?> entry : map.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }
    
    @Override
    public void clear() {
        map.clear();
    }
    
    @Override
    public Set<String> keySet() {
        return map.keySet();
    }
    
    @Override
    public Collection<Object> values() {
        return new AbstractCollection<Object>() {
            
            @Override
            public Iterator<Object> iterator() {
                return new Iterator<Object>() {
                    Iterator<NBTBase> iterator = WrappedNbtMap.this.map.values().iterator();
                    
                    @Override
                    public boolean hasNext() {
                        return iterator.hasNext();
                    }
                    
                    @Override
                    public Object next() {
                        return NBT.fromNativeNbt(iterator.next());
                    }
                    
                    @Override
                    public void remove() {
                        iterator.remove();
                    }
                };
            }
            
            @Override
            public int size() {
                return WrappedNbtMap.this.size();
            }
            
            @Override
            public boolean contains(Object o) {
                return WrappedNbtMap.this.containsValue(o);
            }
            
            @Override
            public void clear() {
                WrappedNbtMap.this.clear();
            }
        };
    }
    
    @Override
    public Set<Entry<String, Object>> entrySet() {
        return new AbstractSet<Entry<String, Object>>() {
            @Override
            public int size() {
                return WrappedNbtMap.this.size();
            }
            
            @Override
            public boolean contains(Object o) {
                if (o instanceof Map.Entry) {
                    Entry<String, Object> entry = (Entry<String, Object>) o;
                    return Objects.equals(WrappedNbtMap.this.get(entry.getKey()), entry.getValue());
                }
                return false;
            }
            
            @Override
            public Iterator<Entry<String, Object>> iterator() {
                return new Iterator<Entry<String, Object>>() {
                    Iterator<Entry<String, NBTBase>> iterator = WrappedNbtMap.this.map.entrySet().iterator();
                    
                    @Override
                    public boolean hasNext() {
                        return iterator.hasNext();
                    }
                    
                    @Override
                    public Entry<String, Object> next() {
                        Entry<String, NBTBase> next = iterator.next();
                        return new AbstractMap.SimpleEntry<>(next.getKey(), NBT.fromNativeNbt(next.getValue()));
                    }
                    
                    @Override
                    public void remove() {
                        iterator.remove();
                    }
                };
            }
            
            @Override
            public void clear() {
                WrappedNbtMap.this.clear();
            }
            
        };
    }
    
    @Override
    public boolean isWrapper() {
        return true;
    }
    
    @Override
    public INbtMap asWrapper() {
        return this;
    }
    
    @Override
    public INbtMap getMap(String key, INbtMap absent) {
        NBTBase base = map.get(key);
        if (base instanceof NBTTagCompound) {
            return new WrappedNbtMap((NBTTagCompound) base);
        }
        return absent;
    }
    
    @Override
    public INbtMap getMap(String key, Supplier<INbtMap> absent) {
        INbtMap result = getMap(key, (INbtMap) null);
        if (result == null) {
            result = absent.get();
        }
        return result;
    }
    
    @Override
    public INbtMap getPresentMap(String key, INbtMap absent) {
        INbtMap result = getMap(key, absent);
        if (result == absent) {
            put(key, result = absent);
        }
        return result;
    }
    
    @Override
    public INbtMap getPresentMap(String key, Supplier<INbtMap> absent) {
        INbtMap result = getMap(key, (INbtMap) null);
        if (result == null) {
            put(key, result = absent.get());
        }
        return result;
    }
    
    @Override
    public INbtList getList(String key, INbtList absent) {
        NBTBase result = map.get(key);
        if (result instanceof NBTTagList) {
            return new WrappedNbtList((NBTTagList) result);
        }
        return absent;
    }
    
    @Override
    public INbtList getList(String key, Supplier<INbtList> absent) {
        INbtList result = getList(key, (INbtList) null);
        if (result == null) {
            result = absent.get();
        }
        return result;
    }
    
    @Override
    public INbtList getPresentList(String key, INbtList absent) {
        INbtList result = getList(key, absent);
        if (result == absent) {
            put(key, result = absent);
        }
        return result;
    }
    
    @Override
    public INbtList getPresentList(String key, Supplier<INbtList> absent) {
        INbtList result = getList(key, (INbtList) null);
        if (result == null) {
            put(key, result = absent.get());
        }
        return result;
    }
    
    @Override
    public String getString(String key, String absent) {
        NBTBase result = map.get(key);
        if (result instanceof NBTTagString) {
            return ((NBTTagString) result).a_();
        }
        return absent;
    }
    
    @Override
    public String getString(String key, Supplier<String> absent) {
        String result = getString(key, (String) null);
        if (result == null) {
            result = absent.get();
        }
        return result;
    }
    
    @Override
    public int[] getIntArray(String key, int[] absent) {
        NBTBase result = map.get(key);
        if (result instanceof NBTTagIntArray) {
            return ((NBTTagIntArray) result).c();
        }
        return absent;
    }
    
    @Override
    public byte[] getByteArray(String key, byte[] absent) {
        NBTBase result = map.get(key);
        if (result instanceof NBTTagByteArray) {
            return ((NBTTagByteArray) result).c();
        }
        return absent;
    }
    
    @Override
    public double getDouble(String key, double absent) {
        NBTBase result = map.get(key);
        if (result instanceof NBTTagDouble) {
            return ((NBTTagDouble) result).g();
        }
        return absent;
    }
    
    @Override
    public float getFloat(String key, float absent) {
        NBTBase result = map.get(key);
        if (result instanceof NBTTagFloat) {
            return ((NBTTagFloat) result).h();
        }
        return absent;
    }
    
    @Override
    public long getLong(String key, long absent) {
        NBTBase result = map.get(key);
        if (result instanceof NBTTagLong) {
            return ((NBTTagLong) result).c();
        }
        return absent;
    }
    
    @Override
    public int getInt(String key, int absent) {
        NBTBase result = map.get(key);
        if (result instanceof NBTTagInt) {
            return ((NBTTagInt) result).d();
        }
        return absent;
    }
    
    @Override
    public short getShort(String key, int absent) {
        NBTBase result = map.get(key);
        if (result instanceof NBTTagShort) {
            return ((NBTTagShort) result).e();
        }
        return (short) absent;
    }
    
    @Override
    public byte getByte(String key, int absent) {
        NBTBase result = map.get(key);
        if (result instanceof NBTTagByte) {
            return ((NBTTagByte) result).f();
        }
        return (byte) absent;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WrappedNbtMap)) return false;
        
        WrappedNbtMap that = (WrappedNbtMap) o;
        
        return map.equals(that.map);
    }
    
    @Override
    public int hashCode() {
        return map.hashCode();
    }
    
    @Override
    public String toString() {
        return "INbtMap[" + delegate.toString() + "]";
    }
    
}
