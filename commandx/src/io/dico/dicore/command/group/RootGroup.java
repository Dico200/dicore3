package io.dico.dicore.command.group;

import io.dico.dicore.command.*;

public class RootGroup extends Group implements ICommandDispatcher {
    
    @Override
    public boolean dispatchCommand(IActor actor, String[] command) {
        return dispatchCommand(actor, new ArgumentBuffer(command));
    }
    
    @Override
    public boolean dispatchCommand(IActor actor, String usedLabel, String[] args) {
        return dispatchCommand(actor, new ArgumentBuffer(usedLabel, args));
    }
    
    @Override
    public boolean dispatchCommand(IActor actor, ArgumentBuffer buffer) {
        ExecutionContextBuilder contextBuilder = new ExecutionContextBuilder(actor, this, buffer);
        try {
            ExecutionContext context = contextBuilder.build();
            //todo
        } catch (CommandException e) {
            e.printStackTrace();
        }
        return false;
    }
    
}
