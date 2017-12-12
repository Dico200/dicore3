package io.dico.dicore.command;

import io.dico.dicore.command.parameter.Parameter;
import io.dico.dicore.command.parameter.ParameterParser;

import java.util.List;
import java.util.Map;

public abstract class Group {
    private Map<String, Group> children;
    private List<Parameter<?>> flags;
    private String name;
    private Parameter<?> parameter;
    
    public String getName() {
        return name;
    }
    
    public boolean isParameter() {
        return parameter != null;
    }
    
    public Parameter<?> getParameter() {
        return parameter;
    }
    
    public boolean isInvisibleTo(IActor actor) {
        return false;
    }
    
    public void addChild(Group child) throws CommandException{
        if (children.putIfAbsent(child.getName(), child) != null) {
            throw new CommandException();
        }
    }
    
    public Group advance(ParameterParser parser) throws CommandException {
        parser.tryParseFlag();
        
        ArgumentBuffer buffer = parser.getBuffer();
        if (!buffer.hasNext()) {
            return this;
        }
        
        String key = buffer.peekNext();
        Group child = children.get(key);
        
        if (child == null || child.isInvisibleTo(parser.getActor())) {
            child = children.values().stream().filter(Group::isParameter).findFirst().orElse(null);
            if (child == null || child.isInvisibleTo(parser.getActor())) {
                return this;
            }
        }
        
        if (this instanceof Command) {
            parser.setReadOnly(true);
            try {
                if (((Command) this).shouldExecuteInsteadOfChild(child, parser)) {
                    return this;
                }
            } finally {
                parser.setReadOnly(false);
            }
        }
        
        if (child.isParameter()) {
            parser.consumeParameter(child.getParameter());
        } else {
            buffer.advance();
        }
        
        parser.addFlags(flags);
        return child;
    }
    
}
