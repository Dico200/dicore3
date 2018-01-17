package io.dico.dicore.command.group;

import io.dico.dicore.command.ArgumentBuffer;
import io.dico.dicore.command.Command;
import io.dico.dicore.command.CommandException;
import io.dico.dicore.command.ExecutionContextBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Group {
    private Map<String, NamedGroup> children = new HashMap<>();
    
    public String getName() {
        return null;
    }
    
    public List<String> getAliases() {
        return null;
    }
    
    public boolean isRoot() {
        return false;
    }
    
    public void addChild(NamedGroup child) throws CommandException {
        if (children.putIfAbsent(child.getName(), child) != null) {
            // Child with exact same name exists, not exact same alias
            throw new CommandException();
        }
        
        for (String name : child.getAliases()) {
            children.putIfAbsent(name, child);
        }
    }
    
    public Group advance(ExecutionContextBuilder contextBuilder) throws CommandException {
        contextBuilder.tryParseFlags();
        
        ArgumentBuffer buffer = contextBuilder.getBuffer();
        if (!buffer.hasNext()) {
            return this;
        }
        
        String key = buffer.peekNext();
        NamedGroup child = children.get(key);
        
        if (child == null || child.isInvisibleTo(contextBuilder.getActor())) {
            child = children.values().stream().filter(NamedGroup::isParameter).findFirst().orElse(null);
            if (child == null || child.isInvisibleTo(contextBuilder.getActor())) {
                return this;
            }
        }
        
        if (this instanceof Command) {
            contextBuilder.setReadOnly(true);
            try {
                if (((Command) this).shouldExecuteInsteadOfChild(child, contextBuilder)) {
                    return this;
                }
            } finally {
                contextBuilder.setReadOnly(false);
            }
        }
        
        if (child.isParameter()) {
            contextBuilder.consumeParameter(child.getParameter());
        } else {
            buffer.advance();
        }
        
        return child;
    }
    
}
