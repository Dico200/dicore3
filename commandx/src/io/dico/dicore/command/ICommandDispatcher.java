package io.dico.dicore.command;

public interface ICommandDispatcher {
    
    /**
     * dispatch the command
     *
     * @param actor   the actor
     * @param command the command
     * @return true if a command has executed
     */
    boolean dispatchCommand(IActor actor, String[] command);
    
    /**
     * dispatch the command
     *
     * @param actor     the actor
     * @param usedLabel the label (word after the /)
     * @param args      the arguments
     * @return true if a command has executed
     */
    boolean dispatchCommand(IActor actor, String usedLabel, String[] args);
    
    /**
     * dispatch the command
     *
     * @param actor  the actor
     * @param buffer the command
     * @return true if a command has executed
     */
    boolean dispatchCommand(IActor actor, ArgumentBuffer buffer);
    
}
