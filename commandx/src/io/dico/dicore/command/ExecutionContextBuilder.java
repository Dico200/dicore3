package io.dico.dicore.command;

import io.dico.dicore.command.ExecutionContext.ProvidedObjectInfo;
import io.dico.dicore.command.group.Group;
import io.dico.dicore.command.group.RootGroup;

import java.util.*;

public final class ExecutionContextBuilder {
    private IActor actor;
    private Group currentGroup;
    private ParameterParseContext parseContext;
    private ArgumentBuffer buffer;
    private int lastCursor;
    private List<Parameter<?>> declarationOrder;
    private Map<Parameter<?>, Object> parameterValues;
    private Map<String, Parameter<?>> flags;
    private Set<Parameter<?>> parsedParameters;
    private IOutputHandler outputHandler;
    private boolean isReadOnly;
    private int inputParameterStartMark = -1;
    
    public ExecutionContextBuilder(IActor actor, RootGroup root, ArgumentBuffer buffer) {
        this.actor = actor;
        this.currentGroup = root;
        this.parseContext = new ParameterParseContext(this);
        this.buffer = buffer;
        this.declarationOrder = new ArrayList<>();
        this.parameterValues = new HashMap<>();
        this.flags = new HashMap<>();
        this.parsedParameters = new HashSet<>();
        this.outputHandler = null;//TODO
    }
    
    public IActor getActor() {
        return actor;
    }
    
    public Group getCurrentGroup() {
        return currentGroup;
    }
    
    public ParameterParseContext getParseContext() {
        return parseContext;
    }
    
    public ArgumentBuffer getBuffer() {
        return isReadOnly ? buffer.getUnaffectingCopy() : buffer;
    }
    
    public IOutputHandler getOutputHandler() {
        return outputHandler;
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
            declarationOrder.add(parameter);
        }
    }
    
    private boolean canConsumeParameter(Parameter<?> parameter) {
        return !isReadOnly && !parsedParameters.contains(parameter);
    }
    
    public void consumeParameter(Parameter<?> parameter) throws ParameterParseException {
        if (!canConsumeParameter(parameter)) return;
        consumeParameterInternal(parameter);
    }
    
    private void consumeParameterInternal(Parameter<?> parameter) throws ParameterParseException {
        if (lastCursor == buffer.getCursor()) return;
        if (!parameter.isFlag() || !flags.containsKey(parameter.getName())) {
            declarationOrder.add(parameter);
        }
        parseContext.setCurrentParameter(parameter);
        Object value = parameter.getType().parse(parseContext, buffer);
        parameterValues.put(parameter, value);
        parsedParameters.add(parameter);
        lastCursor = buffer.getCursor();
    }
    
    public void tryParseFlags() throws ParameterParseException {
        if (isReadOnly) return;
        String flagKey = buffer.peekNext();
        Parameter<?> flag;
        while (flagKey != null
                && flagKey.startsWith("-")
                && (flag = flags.get(flagKey)) != null
                && canConsumeParameter(flag)) {
            buffer.advance();
            consumeParameterInternal(flag);
            flagKey = buffer.peekNext();
        }
    }
    
    private void parse() throws CommandException {
        //noinspection StatementWithEmptyBody
        while (currentGroup != (currentGroup = currentGroup.advance(this))) {
        
        }
        
        if (!(currentGroup instanceof Command)) {
            throw new CommandException(); // TODO
        }
        
        inputParameterStartMark = buffer.getCursor();
        
        Command target = (Command) currentGroup;
        ParameterSet parameters = target.getParameters();
        addFlags(parameters.getFlags());
        
        for (Parameter<?> parameter : parameters.getOrdered()) {
            tryParseFlags();
            consumeParameter(parameter);
        }
        
        tryParseFlags();
    }
    
    public ExecutionContext build() throws CommandException {
        parse();
        
        Map<Parameter<?>, Object> parameterValues = this.parameterValues;
        List<ProvidedObjectInfo> ordered = new ArrayList<>();
        Map<String, ProvidedObjectInfo> map = new HashMap<>();
        Object absent = new Object();
        
        for (Parameter parameter : declarationOrder) {
            ProvidedObjectInfo info = map.getOrDefault(parameter.getName(), ProvidedObjectInfo.empty);
            Object value = parameterValues.getOrDefault(parameter, absent);
            if (value == absent) {
                info = info.addValue(parameter, parameter.getDefaultValue(this), true);
            } else {
                info = info.addValue(parameter, value, false);
            }
            map.put(parameter.getName(), info);
            
            if (!parameter.isFlag()) {
                ordered.add(info);
            }
        }
        
        ProvidedObjectInfo[] orderedArray = ordered.toArray(new ProvidedObjectInfo[ordered.size()]);
        return new ExecutionContext(actor, buffer, inputParameterStartMark, map, orderedArray);
    }
    
}
