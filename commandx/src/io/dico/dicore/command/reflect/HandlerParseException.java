package io.dico.dicore.command.reflect;

import io.dico.dicore.command.CommandException;

public class HandlerParseException extends CommandException {
    
    public HandlerParseException() {
    }
    
    public HandlerParseException(String message) {
        super(message);
    }
    
    public HandlerParseException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public HandlerParseException(Throwable cause) {
        super(cause);
    }
    
    public HandlerParseException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
