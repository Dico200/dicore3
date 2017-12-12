package io.dico.dicore.command.parameter;

import io.dico.dicore.command.ArgumentBuffer;
import io.dico.dicore.command.reflect.TypeToken;

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
    
    public boolean supportsPrimitiveResult() {
        return false;
    }

    public abstract TType parse(ParameterParseContext context, ArgumentBuffer buffer) throws ParameterParseException;
    
    /*
    Repeating type stuff below
     */
    
    public final ParameterType<List<TType>> repeat() {
        if (this instanceof RepeatingParameterType || this instanceof ConfigurableRepeatingParameterType) {
            throw new IllegalStateException();
        }
        if (repeatingType == null) {
            repeatingType = makeRepeatingType();
        }
        return repeatingType;
    }
    
    private ParameterType<List<TType>> makeRepeatingType() {
        if (this instanceof ConfigurableParameterType) {
            return new ConfigurableRepeatingParameterType<>(this);
        }
        return new RepeatingParameterType<>(this);
    }
    
    final List<TType> repeatParse(ParameterParseContext context, ArgumentBuffer buffer) throws ParameterParseException {
        List<TType> result = new ArrayList<>();
        int cursor = buffer.getCursor();
        do {
            result.add(parse(context, buffer));
        } while (cursor != (cursor = buffer.getCursor()) && buffer.hasNext());
        return result;
    }
    
    private static Type listOf(Type type) {
        return TypeToken.getParameterized(List.class, type).getType();
    }
    
    private static final class RepeatingParameterType<TType> extends ParameterType<List<TType>> {
        private ParameterType<TType> delegate;
    
        RepeatingParameterType(ParameterType<TType> delegate) {
            super(listOf(delegate.returnType));
            this.delegate = delegate;
        }
    
        @Override
        public List<TType> parse(ParameterParseContext context, ArgumentBuffer buffer) throws ParameterParseException {
            return delegate.repeatParse(context, buffer);
        }
    }
    
    private static final class ConfigurableRepeatingParameterType<TType> extends ConfigurableParameterType<List<TType>, Object> {
        private ConfigurableParameterType<TType, ?> delegate;
    
        ConfigurableRepeatingParameterType(ParameterType<TType> delegate) {
            super(listOf(delegate.returnType));
            this.delegate = (ConfigurableParameterType<TType, ?>) delegate;
        }
    
        @Override
        public Object deriveOptions(Parameter reflectParameter) {
            return delegate.deriveOptions(reflectParameter);
        }
    
        @Override
        public List<TType> parse(ParameterParseContext context, ArgumentBuffer buffer) throws ParameterParseException {
            return delegate.repeatParse(context, buffer);
        }
    }
    
}
