package io.dico.dicore.command;

import io.dico.dicore.command.ExecutionException;

public class ParameterParseException extends ExecutionException {
    
    public ParameterParseException() {
    }
    
    public ParameterParseException(String message) {
        super(message);
    }
    
    public ParameterParseException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public ParameterParseException(Throwable cause) {
        super(cause);
    }
    
}
