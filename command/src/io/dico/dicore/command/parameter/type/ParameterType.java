package io.dico.dicore.command.parameter.type;

import io.dico.dicore.command.annotation.Range;
import io.dico.dicore.command.CommandException;
import io.dico.dicore.command.ExecutionContext;
import io.dico.dicore.command.parameter.ArgumentBuffer;
import io.dico.dicore.command.parameter.IParameter;
import io.dico.dicore.Reflection;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * A parameter type.
 * Takes care of parsing, default values as well as completions.
 *
 * @param <TReturn>    type of the parameter
 * @param <TParamInfo> the info object type for the parameter (Example: {@link Range.Memory}
 */
public abstract class ParameterType<TReturn, TParamInfo> {
    private final Class<TReturn> returnType;
    private final ParameterConfig<?, TParamInfo> parameterConfig;
    protected final ParameterType<TReturn, TParamInfo> otherType; // flag or non-flag, depending on current
    
    public ParameterType(Class<TReturn> returnType) {
        this(returnType, null);
    }
    
    public ParameterType(Class<TReturn> returnType, ParameterConfig<?, TParamInfo> paramConfig) {
        this.returnType = Objects.requireNonNull(returnType);
        this.parameterConfig = paramConfig;
        
        ParameterType<TReturn, TParamInfo> otherType = flagTypeParameter();
        this.otherType = otherType == null ? this : otherType;
    }
    
    protected ParameterType(Class<TReturn> returnType, ParameterConfig<?, TParamInfo> parameterConfig, ParameterType<TReturn, TParamInfo> otherType) {
        this.returnType = returnType;
        this.parameterConfig = parameterConfig;
        this.otherType = otherType;
    }
    
    public int getExpectedAmountOfConsumedArguments() {
        return 1;
    }
    
    public boolean canBeFlag() {
        return this == otherType;
    }
    
    public boolean isFlagExplicitly() {
        return this instanceof FlagParameterType;
    }
    
    /**
     * @return The return type
     */
    public final Class<TReturn> getReturnType() {
        return returnType;
    }
    
    public final Class<?> getAnnotationClass() {
        return parameterConfig == null ? null : parameterConfig.getAnnotationClass();
    }
    
    public final ParameterConfig<?, TParamInfo> getParameterConfig() {
        return parameterConfig;
    }
    
    public ParameterKey getTypeKey() {
        return new ParameterKey(returnType, parameterConfig != null ? parameterConfig.getAnnotationClass() : null);
    }
    
    public ParameterKey getInfolessTypeKey() {
        return new ParameterKey(returnType, null);
    }
    
    protected FlagParameterType<TReturn, TParamInfo> flagTypeParameter() {
        return null;
    }
    
    public ParameterType<TReturn, TParamInfo> asFlagParameter() {
        return canBeFlag() ? this : otherType;
    }
    
    public ParameterType<TReturn, TParamInfo> asNormalParameter() {
        return isFlagExplicitly() ? otherType : this;
    }
    
    public abstract TReturn parse(IParameter<TReturn> parameter, CommandSender sender, ArgumentBuffer buffer) throws CommandException;
    
    public TReturn parseForContext(IParameter<TReturn> parameter, ExecutionContext context, ArgumentBuffer buffer) throws CommandException {
        return parse(parameter, context.getSender(), buffer);
    }
    
    public TReturn getDefaultValue(IParameter<TReturn> parameter, CommandSender sender, ArgumentBuffer buffer) throws CommandException {
        return null;
    }
    
    public TReturn getDefaultValueForContext(IParameter<TReturn> parameter, ExecutionContext context, ArgumentBuffer buffer) throws CommandException {
        return getDefaultValue(parameter, context.getSender(), buffer);
    }
    
    public List<String> complete(IParameter<TReturn> parameter, CommandSender sender, Location location, ArgumentBuffer buffer) {
        return Collections.emptyList();
    }
    
    public List<String> completeForContext(IParameter<TReturn> parameter, ExecutionContext context, Location location, ArgumentBuffer buffer) {
        return complete(parameter, context.getSender(), location, buffer);
    }
    
    protected static abstract class FlagParameterType<TResult, TParamInfo> extends ParameterType<TResult, TParamInfo> {
        
        protected FlagParameterType(ParameterType<TResult, TParamInfo> otherType) {
            super(otherType.returnType, otherType.parameterConfig, otherType);
        }
        
        @Override
        public int getExpectedAmountOfConsumedArguments() {
            return otherType.getExpectedAmountOfConsumedArguments();
        }
        
        @Override
        public boolean canBeFlag() {
            return true;
        }
        
        @Override
        protected final FlagParameterType<TResult, TParamInfo> flagTypeParameter() {
            return this;
        }
        
        @Override
        public ParameterType<TResult, TParamInfo> asFlagParameter() {
            return this;
        }
        
        @Override
        public ParameterType<TResult, TParamInfo> asNormalParameter() {
            return otherType;
        }
        
    }
    
    public @interface Reference {
        
        /**
         * The path to the static field holding the parameter type referred.
         *
         * @return The path
         */
        String value();
    }
    
    public static class ReferenceUtil {
        
        private ReferenceUtil() {
            
        }
        
        
        /**
         * Get the ParameterType with the associated Reference
         *
         * @param ref the reference
         * @return the parameter type object
         * @throws IllegalArgumentException if the class is found, but the field doesn't exist.
         * @throws IllegalStateException    if this method fails to find the object for any other reason
         */
        public static Object getReference(Reference ref) {
            String[] path = ref.value().split("\\.");
            if (path.length < 2) {
                throw new IllegalStateException();
            }
            
            String fieldName = path[path.length - 1];
            String className = String.join(".", Arrays.copyOfRange(path, 0, path.length - 1));
            
            Class<?> clazz;
            try {
                clazz = Class.forName(className);
            } catch (ClassNotFoundException ex) {
                throw new IllegalArgumentException(ex);
            }
            
            Object result = Reflection.getStaticFieldValue(clazz, fieldName);
            
            if (result == null) {
                throw new IllegalStateException();
            }
            
            return result;
        }
        
    }
    
}
