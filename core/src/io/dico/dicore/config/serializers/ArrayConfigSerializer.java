package io.dico.dicore.config.serializers;

import io.dico.dicore.config.ConfigLogging;

import java.lang.reflect.Array;
import java.util.*;

public abstract class ArrayConfigSerializer<TComp, TArray> extends DelegatedConfigSerializer<TComp, TArray, IConfigSerializer<TComp>> {
    protected final int arraySize;
    protected final boolean forceSize;
    private final Class<TArray> arrayClass;
    
    ArrayConfigSerializer(IConfigSerializer<TComp> delegate, int size, boolean forceSize) {
        super(delegate);
        this.arraySize = size;
        this.forceSize = forceSize;
        //noinspection unchecked
        this.arrayClass = (Class<TArray>) newArray(0).getClass();
    }
    
    public int getArraySize() {
        return arraySize;
    }
    
    public boolean isForceSize() {
        return forceSize;
    }
    
    public Class<TArray> getArrayClass() {
        return arrayClass;
    }
    
    @Override
    public SerializerResult<TArray> load(Object source, ConfigLogging logger) {
        if (!(source instanceof Collection)) {
            if (forceSize) {
                logger.error("should be a collection of size " + arraySize);
            } else {
                logger.error("should be a collection");
            }
            return defaultValueResult();
        }
        
        Collection<?> list = (Collection) source;
        int size, n;
        if (forceSize) {
            size = arraySize;
            n = Math.min(size, list.size());
        } else {
            size = n = list.size();
        }
        
        TArray rv = newArray(size);
        int i = 0;
        for (Object obj : list) {
            if (i >= n) break;
        
            logger.enterIndexPrefix(i);
            TComp value = delegate.load(obj, logger).value;
            logger.exitPrefix();
        
            set(rv, i, value);
            i++;
        }
        
        if (forceSize && i < (n = arraySize)) {
            TComp value = delegate.defaultValue();
            do {
                set(rv, i, value);
                i++;
            } while (i < n);
        }
        
        return new SerializerResult<>(rv);
    }
    
    @Override
    public TArray defaultValue() {
        TArray rv = newArray(arraySize);
        if (arraySize > 0) {
            TComp value = delegate.defaultValue();
            fill(rv, value);
        }
        return rv;
    }
    
    @Override
    public Object serialize(TArray value) {
        if (value == null) {
            return null;
        }
        
        int size = sizeOf(value);
        Collection<Object> rv = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            rv.add(delegate.serialize(get(value, i)));
        }
        return rv;
    }
    
    public abstract TArray newArray(int size);
    
    public abstract int sizeOf(TArray array);
    
    public abstract TComp get(TArray array, int index);
    
    public abstract void set(TArray array, int index, TComp value);
    
    public abstract void fill(TArray array, TComp value);
    
    public Iterator<TComp> iterator(TArray array) {
        return new ArrayIterator<>(this, array);
    }
    
    @Override
    public Class<TArray> type() {
        return arrayClass;
    }
    
    static final class OfReference<T> extends ArrayConfigSerializer<T, T[]> {
        
        OfReference(IConfigSerializer<T> delegate, int size, boolean forceSize) {
            super(delegate, size, forceSize);
        }
    
        @Override
        public T[] newArray(int size) {
            //noinspection unchecked
            return (T[]) Array.newInstance(delegate.type(), size);
        }
    
        @Override
        public int sizeOf(T[] array) {
            return array.length;
        }
    
        @Override
        public T get(T[] array, int index) {
            return array[index];
        }
    
        @Override
        public void set(T[] array, int index, T value) {
            array[index] = value;
        }
    
        @Override
        public void fill(T[] array, T value) {
            Arrays.fill(array, value);
        }
    }
    
    static final class OfInt extends ArrayConfigSerializer<Integer, int[]> {
    
        OfInt(IConfigSerializer<Integer> delegate, int size, boolean forceSize) {
            super(delegate, size, forceSize);
        }
    
        @Override
        public int[] newArray(int size) {
            return new int[size];
        }
    
        @Override
        public int sizeOf(int[] array) {
            return array.length;
        }
    
        @Override
        public Integer get(int[] array, int index) {
            return array[index];
        }
    
        @Override
        public void set(int[] array, int index, Integer value) {
            array[index] = value;
        }
    
        @Override
        public void fill(int[] array, Integer value) {
            Arrays.fill(array, value);
        }
    }
    
    static final class OfBoolean extends ArrayConfigSerializer<Boolean, boolean[]> {
    
        public OfBoolean(IConfigSerializer<Boolean> delegate, int size, boolean forceSize) {
            super(delegate, size, forceSize);
        }
    
        @Override
        public boolean[] newArray(int size) {
            return new boolean[size];
        }
    
        @Override
        public int sizeOf(boolean[] array) {
            return array.length;
        }
    
        @Override
        public Boolean get(boolean[] array, int index) {
            return array[index];
        }
    
        @Override
        public void set(boolean[] array, int index, Boolean value) {
            array[index] = value;
        }
    
        @Override
        public void fill(boolean[] array, Boolean value) {
            Arrays.fill(array, value);
        }
        
    }
    
    static final class OfLong extends ArrayConfigSerializer<Long, long[]> {
        
        public OfLong(IConfigSerializer<Long> delegate, int size, boolean forceSize) {
            super(delegate, size, forceSize);
        }
    
        @Override
        public long[] newArray(int size) {
            return new long[size];
        }
    
        @Override
        public int sizeOf(long[] array) {
            return array.length;
        }
        
        @Override
        public Long get(long[] array, int index) {
            return array[index];
        }
        
        @Override
        public void set(long[] array, int index, Long value) {
            array[index] = value;
        }
    
        @Override
        public void fill(long[] array, Long value) {
            Arrays.fill(array, value);
        }
        
    }
    
    static final class OfFloat extends ArrayConfigSerializer<Float, float[]> {
        
        public OfFloat(IConfigSerializer<Float> delegate, int size, boolean forceSize) {
            super(delegate, size, forceSize);
        }
    
        @Override
        public float[] newArray(int size) {
            return new float[size];
        }
    
        @Override
        public int sizeOf(float[] array) {
            return array.length;
        }
        
        @Override
        public Float get(float[] array, int index) {
            return array[index];
        }
        
        @Override
        public void set(float[] array, int index, Float value) {
            array[index] = value;
        }
    
        @Override
        public void fill(float[] array, Float value) {
            Arrays.fill(array, value);
        }
        
    }
    
    static final class OfDouble extends ArrayConfigSerializer<Double, double[]> {
        
        public OfDouble(IConfigSerializer<Double> delegate, int size, boolean forceSize) {
            super(delegate, size, forceSize);
        }
    
        @Override
        public double[] newArray(int size) {
            return new double[size];
        }
    
        @Override
        public int sizeOf(double[] array) {
            return array.length;
        }
        
        @Override
        public Double get(double[] array, int index) {
            return array[index];
        }
        
        @Override
        public void set(double[] array, int index, Double value) {
            array[index] = value;
        }
    
        @Override
        public void fill(double[] array, Double value) {
            Arrays.fill(array, value);
        }
        
    }
    
    @SuppressWarnings("unchecked")
    private static class ArrayIterator<TComp, TArray> implements Iterator<TComp> {
        private ArrayConfigSerializer<TComp, TArray> ser;
        private TArray array;
        private int index;
        private int size;
        
        public ArrayIterator(ArrayConfigSerializer<TComp, TArray> ser, TArray array) {
            this.ser = ser;
            this.array = array;
            this.size = ser.sizeOf(array);
        }
        
        @Override
        public boolean hasNext() {
            return index < size;
        }
        
        @Override
        public TComp next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            return ser.get(array, index++);
        }
    }
    
}
