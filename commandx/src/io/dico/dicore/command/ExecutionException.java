package io.dico.dicore.command;

public class ExecutionException extends CommandException {
    
    public ExecutionException() {
    }
    
    public ExecutionException(String message) {
        super(message);
    }
    
    public ExecutionException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public ExecutionException(Throwable cause) {
        super(cause);
    }
    
    public static ExecutionException missingArgument(String parameterName) {
        return new ExecutionException("Missing argument for " + parameterName);
    }
    
    public static ExecutionException invalidArgument(String parameterName, String syntaxHelp) {
        return new ExecutionException("Invalid input for " + parameterName + ", should be " + syntaxHelp);
    }
    
}
