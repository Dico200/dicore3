package io.dico.dicore.command;

import java.util.Map;

@SuppressWarnings("unchecked")
public class ExecutionContext {
    private IActor actor;
    private ArgumentBuffer rawInput;
    private int inputParameterStartMark;
    
    private Map<String, ProvidedObjectInfo> parametersByName;
    private ProvidedObjectInfo[] orderedParameters;
    
    ExecutionContext(IActor actor, ArgumentBuffer rawInput, int inputParameterStartMark, Map<String, ProvidedObjectInfo> parametersByName, ProvidedObjectInfo[] orderedParameters) {
        this.actor = actor;
        this.rawInput = rawInput;
        this.inputParameterStartMark = inputParameterStartMark;
        this.parametersByName = parametersByName;
        this.orderedParameters = orderedParameters;
    }
    
    public String[] getRawArguments() {
        return rawInput.getArrayFromIndex(inputParameterStartMark);
    }
    
    public String getRawInput() {
        return rawInput.getRawInput();
    }
    
    public <T> T get(int index) throws ExecutionException {
        if (index < 0 || index >= orderedParameters.length) {
            throw new ExecutionException();
        }
        return cast(orderedParameters[index].value);
    }
    
    public <T> T get(String name) throws ExecutionException {
        return get(name, -1);
    }
    
    public <T> T get(String name, int paramIndex) throws ExecutionException {
        return requireNonNull(parametersByName.get(requireNonNull(name))).get(paramIndex);
    }
    
    public <T> T get(Parameter<T> parameter) throws ExecutionException {
        for (ProvidedObjectInfo info : parametersByName.values()) {
            do {
                if (info.parameter == parameter) {
                    return info.get();
                }
            } while ((info = info.previous) != null);
        }
        throw new ExecutionException("parameter not relevant");
    }
    
    public <T> T getFlag(String flag) throws ExecutionException {
        return getFlag(flag, -1);
    }
    
    public <T> T getFlag(String flag, int paramIndex) throws ExecutionException {
        return get(flag == null ? null : "-" + flag, paramIndex);
    }
    
    private static <T> T cast(Object object) throws ExecutionException {
        try {
            return (T) object;
        } catch (Exception ex) {
            throw new ExecutionException(ex);
        }
    }
    
    private static <T> T requireNonNull(T object) throws ExecutionException {
        if (object == null) {
            throw new ExecutionException();
        }
        return object;
    }
    
    static final class ProvidedObjectInfo {
        static final ProvidedObjectInfo empty = new ProvidedObjectInfo(null, null, false, null);
        final Parameter parameter;
        final Object value;
        final boolean isDefault;
        final ProvidedObjectInfo previous;
        final int index;
    
        private ProvidedObjectInfo(Parameter parameter, Object value, boolean isDefault, ProvidedObjectInfo previous) {
            this.parameter = parameter;
            this.value = value;
            this.isDefault = isDefault;
            this.previous = previous;
            this.index = previous == null ? 0 : previous.index + 1;
        }
        
        <T> T get() throws ExecutionException{
            return cast(value);
        }
        
        <T> T get(int paramIndex) throws ExecutionException {
            if (paramIndex < -1) {
                throw new ExecutionException();
            }
            
            ProvidedObjectInfo target = this;
            while (paramIndex != -1 && paramIndex < target.index) {
                target = target.previous;
            }
            
            return target.get();
        }
    
        ProvidedObjectInfo addValue(Parameter parameter, Object value, boolean isDefault) {
            return new ProvidedObjectInfo(parameter, value, isDefault, this.parameter == null ? null : this);
        }
        
    }
    
}
