package io.dico.dicore.command;

public class ExecutorReadException extends CommandException {
    
    public ExecutorReadException() {
    }
    
    public ExecutorReadException(String message) {
        super(message);
    }
    
    public ExecutorReadException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public ExecutorReadException(Throwable cause) {
        super(cause);
    }
    
    public ExecutorReadException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
