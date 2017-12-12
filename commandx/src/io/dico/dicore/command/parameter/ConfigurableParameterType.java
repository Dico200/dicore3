package io.dico.dicore.command.parameter;

import java.lang.reflect.Parameter;
import java.lang.reflect.Type;

public abstract class ConfigurableParameterType<TType, TOptions> extends ParameterType<TType> {
    
    public ConfigurableParameterType(Type returnType) {
        super(returnType);
    }
    
    public abstract TOptions deriveOptions(Parameter reflectParameter/*, some other argument*/);
    
}
