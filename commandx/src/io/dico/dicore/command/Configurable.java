package io.dico.dicore.command;

import java.lang.reflect.Parameter;

public interface Configurable {
    
    Object deriveOptions(ExecutorReadContext context, Parameter reflectParameter);
    
}
