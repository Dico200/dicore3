package io.dico.dicore.command;

import java.lang.reflect.Parameter;
import java.lang.reflect.Type;

public abstract class ConfigurableParameterType<TType, TOptions> extends ParameterType<TType> implements Configurable {
    
    public ConfigurableParameterType(Type returnType) {
        super(returnType);
    }
    
    @Override
    public abstract TOptions deriveOptions(ExecutorReadContext context, Parameter reflectParameter);
    
}
