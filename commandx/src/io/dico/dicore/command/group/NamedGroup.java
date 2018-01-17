package io.dico.dicore.command.group;

import io.dico.dicore.command.*;

import java.util.*;

public class NamedGroup extends Group {
    private String name;
    private List<String> aliases;
    private List<Parameter<?>> flags;
    private Parameter<?> parameter;
    
    public NamedGroup(String name) {
        this.name = name;
        this.flags = Collections.emptyList();
        this.aliases = Collections.emptyList();
    }
    
    public NamedGroup(Parameter<?> parameter) {
        this(parameter.getName());
        this.parameter = parameter;
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public List<String> getAliases() {
        return aliases;
    }
    
    public boolean isParameter() {
        return parameter != null;
    }
    
    public Parameter<?> getParameter() {
        return parameter;
    }
    
    public boolean isInvisibleTo(IActor actor) {
        return false; //todo
    }
    
    public void addAliases(String... aliases) {
        if (!(this.aliases instanceof ArrayList)) this.aliases = new ArrayList<>();
        this.aliases.addAll(Arrays.asList(aliases));
    }
    
    public void addFlag(Parameter<?> flag) throws CommandException {
        if (!flag.isFlag()) throw new CommandException("Expected a flag");
        if (!(flags instanceof ArrayList)) flags = new ArrayList<>();
        flags.add(flag);
    }
    
    @Override
    public NamedGroup advance(ExecutionContextBuilder contextBuilder) throws CommandException {
        Group result = super.advance(contextBuilder);
        if (result != this) {
            contextBuilder.addFlags(flags);
        }
        return (NamedGroup) result;
    }
}
