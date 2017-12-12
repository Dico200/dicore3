package io.dico.dicore.command;

import io.dico.dicore.command.parameter.ParameterParser;
import io.dico.dicore.command.parameter.ParameterSet;

public class Command extends Group {
    private ParameterSet parameters;
    
    public boolean shouldExecuteInsteadOfChild(Group child, ParameterParser parser) {
        return false;
    }
    
    public ParameterSet getParameters() {
        return parameters;
    }
    
    public void execute(ExecutionContext context) throws ExecutionException {
    
    }
    
}
