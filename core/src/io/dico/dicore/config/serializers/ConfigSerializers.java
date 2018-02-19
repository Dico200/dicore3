package io.dico.dicore.config.serializers;

import io.dico.dicore.config.ConfigLogging;

public class ConfigSerializers {
    
    public static IConfigSerializer<Boolean> forBoolean() {
        return BooleanSerializer.defaultToFalse;
    }
    
    public static IConfigSerializer<Boolean> forBoolean(boolean defaultValue) {
        return defaultValue ? BooleanSerializer.defaultToTrue : forBoolean();
    }
    
    public static IConfigSerializer<Integer> forInt() {
        return IntSerializer.defaultToZero;
    }
    
    public static IConfigSerializer<Integer> forInt(int defaultValue) {
        return defaultValue == 0 ? forInt() : new IntSerializer(defaultValue);
    }
    
    public static IConfigSerializer<Long> forLong() {
        return LongSerializer.defaultToZero;
    }
    
    public static IConfigSerializer<Long> forLong(long defaultValue) {
        return defaultValue == 0 ? forLong() : new LongSerializer(defaultValue);
    }
    
    public static IConfigSerializer<Float> forFloat() {
        return FloatSerializer.defaultToZero;
    }
    
    public static IConfigSerializer<Float> forFloat(float defaultValue) {
        return defaultValue == 0 ? forFloat() : new FloatSerializer(defaultValue);
    }
    
    public static IConfigSerializer<Double> forDouble() {
        return DoubleSerializer.defaultToZero;
    }
    
    public static IConfigSerializer<Double> forDouble(double defaultValue) {
        return defaultValue == 0 ? forDouble() : new DoubleSerializer(defaultValue);
    }
    
    public static IConfigSerializer<String> forString() {
        return StringSerializer.defaultToNull;
    }
    
    public static IConfigSerializer<String> forString(String defaultValue) {
        return defaultValue == null ? forString() : new StringSerializer(defaultValue);
    }
    
    public static IConfigSerializer<Double> forChance() {
        return forChance(0);
    }
    
    public static IConfigSerializer<Double> forChance(double defaultChance) {
        return forDouble(defaultChance).map(DoubleAsChanceMapper.instance);
    }
    
    @SuppressWarnings("unchecked")
    public static <TComp, TArray> ArrayConfigSerializer<TComp, TArray> forArray(IConfigSerializer<TComp> delegate, int size, boolean forceSize) {
        Class<TComp> type = delegate.type();
        ArrayConfigSerializer<?, ?> rv;
        if (type.isPrimitive()) {
            if (type == Integer.TYPE) {
                rv = new ArrayConfigSerializer.OfInt((IConfigSerializer<Integer>) delegate, size, forceSize);
            } else if (type == Long.TYPE) {
                rv = new ArrayConfigSerializer.OfLong((IConfigSerializer<Long>) delegate, size, forceSize);
            } else if (type == Float.TYPE) {
                rv = new ArrayConfigSerializer.OfFloat((IConfigSerializer<Float>) delegate, size, forceSize);
            } else if (type == Double.TYPE) {
                rv = new ArrayConfigSerializer.OfDouble((IConfigSerializer<Double>) delegate, size, forceSize);
            } else if (type == Boolean.TYPE) {
                rv = new ArrayConfigSerializer.OfBoolean((IConfigSerializer<Boolean>) delegate, size, forceSize);
            }
            throw new IllegalArgumentException();
        } else {
            rv = new ArrayConfigSerializer.OfReference<>(delegate, size, forceSize);
        }
        return (ArrayConfigSerializer<TComp, TArray>) rv;
    }
    
    ////////////////////////////////////////////////////////////////////////////////
    
    private static final class BooleanSerializer extends SimpleConfigSerializer<Boolean> {
        private static final BooleanSerializer defaultToFalse = new BooleanSerializer(false);
        private static final BooleanSerializer defaultToTrue = new BooleanSerializer(true);
        
        public BooleanSerializer(Boolean defaultValue) {
            super(Boolean.TYPE, defaultValue);
        }
    
        @Override
        public SerializerResult<Boolean> load(Object source, ConfigLogging logger) {
            Boolean rv = null;
            if (source instanceof Boolean) {
                rv = (Boolean) source;
            } else if (source instanceof String) {
                if ("true".equalsIgnoreCase((String) source)) {
                    rv = true;
                } else if ("false".equalsIgnoreCase((String) source)) {
                    rv = false;
                }
            }
            if (rv == null) {
                logger.error("Expected boolean");
                return defaultValueResult();
            }
            return new SerializerResult<>(rv);
        }
    }
    
    private static final class IntSerializer extends NumberSerializer<Integer> {
        private static final IntSerializer defaultToZero = new IntSerializer(0);
        
        public IntSerializer(Integer defaultValue) {
            super(Integer.TYPE, defaultValue);
        }
    
        @Override
        protected Integer parse(String string) {
            return Integer.parseInt(string);
        }
    
        @Override
        protected Integer select(Number number) {
            return number instanceof Integer ? (Integer) number : number.intValue();
        }
        
    }
    
    private static final class LongSerializer extends NumberSerializer<Long> {
        private static final LongSerializer defaultToZero = new LongSerializer(0L);
        
        public LongSerializer(Long defaultValue) {
            super(Long.TYPE, defaultValue);
        }
        
        @Override
        protected Long parse(String string) {
            return Long.parseLong(string);
        }
        
        @Override
        protected Long select(Number number) {
            return number instanceof Long ? (Long) number : number.longValue();
        }
        
    }
    
    private static final class FloatSerializer extends NumberSerializer<Float> {
        private static final FloatSerializer defaultToZero = new FloatSerializer(0F);
        
        public FloatSerializer(Float defaultValue) {
            super(Float.TYPE, defaultValue);
        }
        
        @Override
        protected Float parse(String string) {
            return Float.parseFloat(string);
        }
        
        @Override
        protected Float select(Number number) {
            return number instanceof Float ? (Float) number : number.floatValue();
        }
        
    }
    
    private static final class DoubleSerializer extends NumberSerializer<Double> {
        private static final DoubleSerializer defaultToZero = new DoubleSerializer(0D);
        
        public DoubleSerializer(Double defaultValue) {
            super(Double.TYPE, defaultValue);
        }
        
        @Override
        protected Double parse(String string) {
            return Double.parseDouble(string);
        }
        
        @Override
        protected Double select(Number number) {
            return number instanceof Double ? (Double) number : number.doubleValue();
        }
        
    }
    
    private static final class StringSerializer extends SimpleConfigSerializer<String> {
        private static final StringSerializer defaultToNull = new StringSerializer(null);
    
        public StringSerializer(String defaultValue) {
            super(String.class, defaultValue);
        }
    
        @Override
        public SerializerResult<String> load(Object source, ConfigLogging logger) {
            if (source == null) {
                logger.error("Expected string");
                return defaultValueResult();
            }
            return new SerializerResult<>(source.toString());
        }
        
    }
    
    private static final class DoubleAsChanceMapper implements IConfigSerializerMapper<Double, Double> {
        private static final DoubleAsChanceMapper instance = new DoubleAsChanceMapper();
        
        @Override
        public SerializerResult<Double> postLoad(SerializerResult<Double> in) {
            return new SerializerResult<>(in.value / 100, in.isDefault);
        }
        
        @Override
        public Double preSave(Double value) {
            return value * 100;
        }
        
        @Override
        public Class<Double> type() {
            return Double.TYPE;
        }
    }

}
