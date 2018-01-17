package io.dico.dicore.command;

import io.dico.dicore.command.group.NamedGroup;

public class Command extends NamedGroup {
    private ParameterSet parameters;
    
    public Command(String name) {
        super(name);
        parameters = new ParameterSet();
    }
    
    public boolean shouldExecuteInsteadOfChild(NamedGroup child, ExecutionContextBuilder contextBuilder) {
        return false;
    }
    
    public ParameterSet getParameters() {
        return parameters;
    }
    
    public void execute(ExecutionContext context) throws ExecutionException {
    
    }
    
}
