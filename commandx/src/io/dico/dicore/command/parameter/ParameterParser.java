package io.dico.dicore.command.parameter;

import io.dico.dicore.command.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ParameterParser {
    private IActor actor;
    private Group currentGroup;
    private ParameterParseContext context;
    private ArgumentBuffer buffer;
    private Map<Parameter<?>, Object> parameterValues;
    private Map<String, Parameter<?>> flags;
    private IOutputHandler outputHandler;
    private boolean isReadOnly;
    
    public ParameterParser(IActor actor, RootGroup root, ArgumentBuffer buffer) {
        this.actor = actor;
        this.currentGroup = root;
        this.context = new ParameterParseContext(this);
        this.buffer = buffer;
        this.parameterValues = new HashMap<>();
        this.flags = new HashMap<>();
        this.outputHandler = null;//TODO
    }
    
    public IActor getActor() {
        return actor;
    }
    
    public Group getCurrentGroup() {
        return currentGroup;
    }
    
    public ParameterParseContext getContext() {
        return context;
    }
    
    public ArgumentBuffer getBuffer() {
        return isReadOnly ? buffer.getUnaffectingCopy() : buffer;
    }
    
    public IOutputHandler getOutputHandler() {
        return outputHandler;
    }
    
    public void consumeParameter(Parameter<?> parameter) throws ParameterParseException {
        if (isReadOnly) return;
        context.setCurrentParameter(parameter);
        Object value = parameter.getType().parse(context, buffer);
        parameterValues.put(parameter, value);
    }
    
    public void setReadOnly(boolean readOnly) {
        this.isReadOnly = readOnly;
    }
    
    public void addFlags(List<Parameter<?>> flags) throws CommandException {
        if (isReadOnly) return;
        for (Parameter<?> parameter : flags) {
            if (!parameter.isFlag()) {
                throw new CommandException("Expected a flag");
            }
            // todo handle name collisions
            this.flags.put(parameter.getName(), parameter);
        }
    }
    
    public void tryParseFlag() {
        if (isReadOnly) return;
        // todo
    }
    
    private boolean advance() throws CommandException {
        return currentGroup != (currentGroup = currentGroup.advance(this));
    }
    
    public void parse() throws CommandException {
        //noinspection StatementWithEmptyBody
        while (advance()) {
        
        }
        
        if (!(currentGroup instanceof Command)) {
            return; //todo
        }
        
        Command target = (Command) currentGroup;
        ParameterSet parameters = target.getParameters();
        addFlags(parameters.getFlags());
        
        //todo
    }
    
}
