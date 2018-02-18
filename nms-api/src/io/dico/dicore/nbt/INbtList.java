package io.dico.dicore.nbt;

import com.google.common.collect.Iterators;
import io.dico.dicore.nms.NmsFactory;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.function.Supplier;

public interface INbtList extends List<Object> {

    /**
     * The {@link ENbtType ENbtType} of each of the elements
     * <p>
     * <p>The ENbtType is recorded when the first element is added to the list.</p>
     *
     * @return The {@link ENbtType ENbtType} of each of the elements, or null if it has not yet been recorded.
     */
    ENbtType getElementType();
    
    /**
     * Sets the element type reported by the underlying tag list.
     * Parts of the server will check that this type has a certain value.
     *
     * @param elementType the element type to set
     */
    void setElementType(ENbtType elementType);

    /**
     * Item tags require wrapped nbt maps and lists.
     * You may not have to check, but item tag modifications might be more performant if you take good care beforehand.
     *
     * @return true if this INbtList is implemented as a wrapper for an nms nbt list.
     * @see #asWrapper()
     */
    boolean isWrapper();

    /**
     * @return A wrapped implementation of INbtList with the same contents, or this object if {@link #isWrapper()} returns true.
     */
    INbtList asWrapper();

    /**
     * <p>
     * If the element at the position is not an nbt map, {@link INbtMap#EMPTY the empty map} is returned
     * </p>
     * <p>
     * A call to this method is equivalent to calling {@code {@link #getMap(int, INbtMap)} getMap}(index, {@link INbtMap#EMPTY}}
     * unless this behavior is changed as documented by the underlying implementation
     * </p>
     *
     * @param index The index of the element
     * @return The {@link INbtMap NBTMap} at the given index
     */
    default INbtMap getMap(int index) {
        return getMap(index, INbtMap.EMPTY);
    }

    /**
     * Same as {@link #getMap(int)}, but with a custom object to return if the map is not found
     *
     * @param index  The location
     * @param absent the map to return if no map is found at the given index, may be null
     * @return The map at the given index
     */
    INbtMap getMap(int index, INbtMap absent);

    /**
     * Similar to {@link #getMap(int, INbtMap)}, but if the object is not found,
     * the result of the call {@code absent.get()} is returned.
     *
     * @param index  The location
     * @param absent The supplier to compute the result if no map is found at this index
     * @return The map at the given index
     * @throws IndexOutOfBoundsException if index is out of bounds
     * @throws NullPointerException      if absent is null
     */
    INbtMap getMap(int index, Supplier<INbtMap> absent);

    /**
     * @param index The location
     * @return A map at the index, if it is not found, a new one is put there and returned.
     * @see #getPresentMap(int, INbtMap)
     */
    default INbtMap getPresentMap(int index) {
        return getPresentMap(index, NmsFactory.getDriver()::newWrappedNBTMap);
    }

    /**
     * @param index  The location
     * @param absent The map to be added to this list at the given index if no map is found there
     * @return A map at the index, if it is not found, a new one is put there and returned.
     * @see #getMap(int, INbtMap)
     */
    INbtMap getPresentMap(int index, INbtMap absent);

    /**
     * @param index  The location
     * @param absent The supplier to compute the map to be added to this list at the given index if no map is found there
     * @return A map at the index, if it is not found, a new one is put there and returned.
     * @see #getMap(int, Supplier)
     */
    INbtMap getPresentMap(int index, Supplier<INbtMap> absent);

    default INbtList getPresentList(int index) {
        return getPresentList(index, NmsFactory.getDriver()::newWrappedNBTList);
    }

    INbtList getList(int index, INbtList absent);

    INbtList getList(int index, Supplier<INbtList> absent);

    INbtList getPresentList(int index, INbtList absent);

    INbtList getPresentList(int index, Supplier<INbtList> absent);

    default INbtList getList(int index) {
        return getList(index, EMPTY);
    }

    default int[] getIntArray(int index) {
        return getIntArray(index, new int[0]);
    }

    int[] getIntArray(int index, int[] absent);

    default byte[] getByteArray(int index) {
        return getByteArray(index, new byte[0]);
    }

    byte[] getByteArray(int index, byte[] absent);

    default double getDouble(int index) {
        return getDouble(index, 0D);
    }

    double getDouble(int index, double absent);

    default float getFloat(int index) {
        return getFloat(index, 0F);
    }

    float getFloat(int index, float absent);

    default String getString(int index) {
        return getString(index, "");
    }

    String getString(int index, String absent);

    default long getLong(int index) {
        return getLong(index, 0L);
    }

    long getLong(int index, long absent);

    default int getInt(int index) {
        return getInt(index, 0);
    }

    int getInt(int index, int absent);

    default short getShort(int index) {
        return getShort(index, 0);
    }

    short getShort(int index, int absent);

    default byte getByte(int index) {
        return getByte(index, 0);
    }

    byte getByte(int index, int absent);

    INbtList EMPTY = new INbtList() {
        @Override
        public boolean isWrapper() {
            return false;
        }

        @Override
        public INbtList asWrapper() {
            return NmsFactory.getDriver().newWrappedNBTList();
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
        public boolean contains(Object o) {
            return false;
        }

        @Override
        public Iterator<Object> iterator() {
            return Iterators.emptyIterator();
        }

        @Override
        public Object[] toArray() {
            return new Object[0];
        }

        @Override
        public <T> T[] toArray(T[] a) {
            if (a.length > 0) {
                a[0] = null;
            }
            return a;
        }

        @Override
        public boolean add(Object o) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean remove(Object o) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            return false;
        }

        @Override
        public boolean addAll(Collection<?> c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean addAll(int index, Collection<?> c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void clear() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Object get(int index) {
            throw new IndexOutOfBoundsException("" + index);
        }

        @Override
        public Object set(int index, Object element) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void add(int index, Object element) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Object remove(int index) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int indexOf(Object o) {
            return -1;
        }

        @Override
        public int lastIndexOf(Object o) {
            return -1;
        }

        @Override
        public ListIterator<Object> listIterator() {
            return (ListIterator<Object>) iterator();
        }

        @Override
        public ListIterator<Object> listIterator(int index) {
            return listIterator();
        }

        @Override
        public List<Object> subList(int fromIndex, int toIndex) {
            throw new IndexOutOfBoundsException("" + fromIndex);
        }

        @Override
        public ENbtType getElementType() {
            return null;
        }
    
        @Override
        public void setElementType(ENbtType elementType) {
            throw new UnsupportedOperationException();
        }
    
        @Override
        public INbtMap getMap(int index, INbtMap absent) {
            return absent;
        }

        @Override
        public int[] getIntArray(int index, int[] absent) {
            return absent;
        }

        @Override
        public byte[] getByteArray(int index, byte[] absent) {
            return absent;
        }

        @Override
        public double getDouble(int index, double absent) {
            return absent;
        }

        @Override
        public float getFloat(int index, float absent) {
            return absent;
        }

        @Override
        public String getString(int index, String absent) {
            return absent;
        }

        @Override
        public INbtList getList(int index, INbtList absent) {
            return absent;
        }

        @Override
        public long getLong(int index, long absent) {
            return absent;
        }

        @Override
        public int getInt(int index, int absent) {
            return absent;
        }

        @Override
        public short getShort(int index, int absent) {
            return (short) absent;
        }

        @Override
        public byte getByte(int index, int absent) {
            return (byte) absent;
        }

        @Override
        public INbtMap getPresentMap(int index, INbtMap absent) {
            throw new UnsupportedOperationException();
        }

        @Override
        public INbtMap getPresentMap(int index, Supplier<INbtMap> absent) {
            throw new UnsupportedOperationException();
        }

        @Override
        public INbtList getPresentList(int index, INbtList absent) {
            throw new UnsupportedOperationException();
        }

        @Override
        public INbtList getPresentList(int index, Supplier<INbtList> absent) {
            throw new UnsupportedOperationException();
        }

        @Override
        public INbtMap getMap(int index, Supplier<INbtMap> absent) {
            return absent.get();
        }

        @Override
        public INbtList getList(int index, Supplier<INbtList> absent) {
            return absent.get();
        }
    };
}
