package io.dico.dicore.command;

import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public abstract class ParameterType<TType> {
    protected final Type returnType;
    private transient ParameterType<List<TType>> repeatingType;
    
    protected ParameterType(Type returnType) {
        this.returnType = returnType;
    }
    
    public Type getReturnType() {
        return returnType;
    }
    
    public Class<?> getPrimitiveType() {
        return null;
    }
    
    public boolean isRepeating() {
        return false;
    }
    
    public abstract TType parse(ParameterParseContext context, ArgumentBuffer buffer) throws ParameterParseException;
    
    public abstract TType getDefaultValue(ParameterParseContext context) throws ParameterParseException;
    
    /*
    Repeating type stuff below
     */
    
    public final ParameterType<List<TType>> repeat() {
        if (this instanceof RepeatingParameterType) {
            throw new IllegalStateException();
        }
        if (repeatingType == null) {
            repeatingType = RepeatingParameterType.repeat(this);
        }
        return repeatingType;
    }
    
    private static class RepeatingParameterType<TType> extends ParameterType<List<TType>> {
        protected ParameterType<TType> delegate;
    
        private RepeatingParameterType(ParameterType<TType> delegate) {
            super(listOf(delegate.returnType));
            this.delegate = delegate;
        }
        
        static <TType> RepeatingParameterType<TType> repeat(ParameterType<TType> delegate) {
            return delegate instanceof Configurable ? new ConfigurableRepeatingParameterType<>(delegate) : new RepeatingParameterType<>(delegate);
        }
    
        private static Type listOf(Type type) {
            return ParameterizedTypeImpl.make(List.class, new Type[]{type}, null);
        }
    
        @Override
        public List<TType> parse(ParameterParseContext context, ArgumentBuffer buffer) throws ParameterParseException {
            List<TType> result = new ArrayList<>();
            int cursor = buffer.getCursor();
            do {
                result.add(delegate.parse(context, buffer));
            } while (cursor != (cursor = buffer.getCursor()) && buffer.hasNext());
            return result;
        }
    
        @Override
        public List<TType> getDefaultValue(ParameterParseContext context) throws ParameterParseException {
            return new ArrayList<>();
        }
    
        private static class ConfigurableRepeatingParameterType<TType> extends RepeatingParameterType<TType> implements Configurable {
        
            private ConfigurableRepeatingParameterType(ParameterType<TType> delegate) {
                super(delegate);
            }
        
            @Override
            public Object deriveOptions(ExecutorReadContext context, Parameter reflectParameter) {
                return ((Configurable) delegate).deriveOptions(context, reflectParameter);
            }
        }
    }
    
}
