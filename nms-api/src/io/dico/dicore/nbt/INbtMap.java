package io.dico.dicore.nbt;

import io.dico.dicore.nms.NmsFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public interface INbtMap extends Map<String, Object> {
    // Present means the returned object is a member of the map
    
    /**
     * Item tags require wrapped nbt maps and lists.
     * You may not have to check, but item tag modifications might be more performant if you take good care beforehand.
     *
     * @return true if this INbtMap is implemented as a wrapper for an nms nbt tag compound.
     * @see #asWrapper()
     */
    boolean isWrapper();
    
    /**
     * @return A wrapped implementation of INbtMap with the same contents, or this object if {@link #isWrapper()} returns true.
     */
    INbtMap asWrapper();
    
    /**
     * Check if this map contains the given key, and its type is of the given NBT type.
     *
     * @param key the key to check
     * @return true if and only if there is an element of the specified type for the given key
     */
    boolean containsKeyOfType(String key, ENbtType type);
    
    /**
     * Check if this object is identical to {@link #EMPTY}
     * @return true if and only if this object == INbtMap.EMPTY
     */
    default boolean isEmptyInstance() {
        return false;
    }
    
    default INbtMap getMap(String key) {
        return getMap(key, EMPTY);
    }
    
    INbtMap getMap(String key, INbtMap absent);
    
    INbtMap getMap(String key, Supplier<INbtMap> absent);
    
    default INbtMap getPresentMap(String key) {
        return getPresentMap(key, NmsFactory.getDriver()::newWrappedNBTMap);
    }
    
    INbtMap getPresentMap(String key, INbtMap absent);
    
    INbtMap getPresentMap(String key, Supplier<INbtMap> absent);
    
    default INbtList getList(String key) {
        return getList(key, INbtList.EMPTY);
    }
    
    INbtList getList(String key, INbtList absent);
    
    INbtList getList(String key, Supplier<INbtList> absent);
    
    default INbtList getPresentList(String key) {
        return getPresentList(key, NmsFactory.getDriver()::newWrappedNBTList);
    }
    
    INbtList getPresentList(String key, INbtList absent);
    
    INbtList getPresentList(String key, Supplier<INbtList> absent);
    
    String getString(String key, String absent);
    
    String getString(String key, Supplier<String> absent);
    
    default String getString(String key) {
        return getString(key, (String) null);
    }
    
    default int[] getIntArray(String key) {
        return getIntArray(key, new int[0]);
    }
    
    int[] getIntArray(String key, int[] absent);
    
    default byte[] getByteArray(String key) {
        return getByteArray(key, new byte[0]);
    }
    
    byte[] getByteArray(String key, byte[] absent);
    
    default double getDouble(String key) {
        return getDouble(key, 0D);
    }
    
    double getDouble(String key, double absent);
    
    default float getFloat(String key) {
        return getFloat(key, 0F);
    }
    
    float getFloat(String key, float absent);
    
    default long getLong(String key) {
        return getLong(key, 0L);
    }
    
    long getLong(String key, long absent);
    
    default int getInt(String key) {
        return getInt(key, 0);
    }
    
    int getInt(String key, int absent);
    
    default short getShort(String key) {
        return getShort(key, 0);
    }
    
    short getShort(String key, int absent);
    
    default byte getByte(String key) {
        return getByte(key, 0);
    }
    
    byte getByte(String key, int absent);
    
    INbtMap EMPTY = new INbtMap() {
        @Override
        public boolean isEmptyInstance() {
            return true;
        }
    
        @Override
        public boolean isWrapper() {
            return false;
        }
        
        @Override
        public INbtMap asWrapper() {
            return NmsFactory.getDriver().newWrappedNBTMap();
        }
    
        @Override
        public boolean containsKeyOfType(String key, ENbtType type) {
            return false;
        }
    
        @Override
        public int size() {
            return 0;
        }
        
        @Override
        public boolean isEmpty() {
            return true;
        }
        
        @Override
        public boolean containsKey(Object key) {
            return false;
        }
        
        @Override
        public boolean containsValue(Object value) {
            return false;
        }
        
        @Override
        public Object get(Object key) {
            return null;
        }
        
        @Override
        public Object put(String key, Object value) {
            throw new UnsupportedOperationException();
        }
        
        @Override
        public Object remove(Object key) {
            throw new UnsupportedOperationException();
        }
        
        @Override
        public void putAll(Map<? extends String, ?> m) {
            throw new UnsupportedOperationException();
        }
        
        @Override
        public void clear() {
            throw new UnsupportedOperationException();
        }
        
        @Override
        public Set<String> keySet() {
            return Collections.emptySet();
        }
        
        @Override
        public Collection<Object> values() {
            return Collections.emptyList();
        }
        
        @Override
        public Set<Map.Entry<String, Object>> entrySet() {
            return Collections.emptySet();
        }
        
        @Override
        public INbtMap getMap(String key, INbtMap absent) {
            return absent;
        }
        
        @Override
        public INbtList getList(String key, INbtList absent) {
            return absent;
        }
        
        @Override
        public int[] getIntArray(String key, int[] absent) {
            return absent;
        }
        
        @Override
        public double getDouble(String key, double absent) {
            return absent;
        }
        
        @Override
        public float getFloat(String key, float absent) {
            return absent;
        }
        
        @Override
        public long getLong(String key, long absent) {
            return absent;
        }
        
        @Override
        public int getInt(String key, int absent) {
            return absent;
        }
        
        @Override
        public short getShort(String key, int absent) {
            return (short) absent;
        }
        
        @Override
        public byte getByte(String key, int absent) {
            return (byte) absent;
        }
        
        @Override
        public byte[] getByteArray(String key, byte[] absent) {
            return absent;
        }
        
        @Override
        public INbtMap getMap(String key, Supplier<INbtMap> absent) {
            return absent.get();
        }
        
        @Override
        public INbtMap getPresentMap(String key, INbtMap absent) {
            throw new UnsupportedOperationException();
        }
        
        @Override
        public INbtMap getPresentMap(String key, Supplier<INbtMap> absent) {
            throw new UnsupportedOperationException();
        }
        
        @Override
        public INbtList getList(String key, Supplier<INbtList> absent) {
            return absent.get();
        }
        
        @Override
        public INbtList getPresentList(String key, INbtList absent) {
            throw new UnsupportedOperationException();
        }
        
        @Override
        public INbtList getPresentList(String key, Supplier<INbtList> absent) {
            throw new UnsupportedOperationException();
        }
        
        @Override
        public String getString(String key, String absent) {
            return absent;
        }
        
        @Override
        public String getString(String key, Supplier<String> absent) {
            return absent.get();
        }
    };
}
