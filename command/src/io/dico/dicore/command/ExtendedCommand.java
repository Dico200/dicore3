package io.dico.dicore.command;

import io.dico.dicore.command.parameter.IArgumentPreProcessor;
import io.dico.dicore.command.parameter.IParameter;
import io.dico.dicore.command.parameter.type.ParameterType;

@SuppressWarnings("unchecked")
public abstract class ExtendedCommand<T extends ExtendedCommand<T>> extends Command {
    protected final boolean modifiable;
    
    public ExtendedCommand() {
        this(true);
    }
    
    public ExtendedCommand(boolean modifiable) {
        this.modifiable = modifiable;
    }
    
    protected T newModifiableInstance() {
        return (T) this;
    }
    
    @Override
    public T addParameter(IParameter parameter) {
        return modifiable ? (T) super.addParameter(parameter) : newModifiableInstance().addParameter(parameter);
    }
    
    @Override
    public <TType> T addParameter(String name, String description, ParameterType<TType, Void> type) {
        return modifiable ? (T) super.addParameter(name, description, type) : newModifiableInstance().addParameter(name, description, type);
    }
    
    @Override
    public <TType, TParamInfo> T addParameter(String name, String description, ParameterType<TType, TParamInfo> type, TParamInfo tParamInfo) {
        return modifiable ? (T) super.addParameter(name, description, type, tParamInfo) : newModifiableInstance().addParameter(name, description, type, tParamInfo);
    }
    
    @Override
    public <TType> T addFlag(String name, String description, ParameterType<TType, Void> type) {
        return modifiable ? (T) super.addFlag(name, description, type) : newModifiableInstance().addFlag(name, description, type);
    }
    
    @Override
    public <TType, TParamInfo> T addFlag(String name, String description, ParameterType<TType, TParamInfo> type, TParamInfo tParamInfo) {
        return modifiable ? (T) super.addFlag(name, description, type, tParamInfo) : newModifiableInstance().addFlag(name, description, type, tParamInfo);
    }
    
    @Override
    public <TType> T addAuthorizedFlag(String name, String description, ParameterType<TType, Void> type, String permission) {
        return modifiable ? (T) super.addAuthorizedFlag(name, description, type, permission) : newModifiableInstance().addAuthorizedFlag(name, description, type, permission);
    }
    
    @Override
    public <TType, TParamInfo> T addAuthorizedFlag(String name, String description, ParameterType<TType, TParamInfo> type, TParamInfo tParamInfo, String permission) {
        return modifiable ? (T) super.addAuthorizedFlag(name, description, type, tParamInfo, permission) : newModifiableInstance().addAuthorizedFlag(name, description, type, tParamInfo, permission);
    }
    
    @Override
    public T requiredParameters(int requiredParameters) {
        return modifiable ? (T) super.requiredParameters(requiredParameters) : newModifiableInstance().requiredParameters(requiredParameters);
    }
    
    @Override
    public T repeatFinalParameter() {
        return modifiable ? (T) super.repeatFinalParameter() : newModifiableInstance().repeatFinalParameter();
    }
    
    @Override
    public T setDescription(String... description) {
        return modifiable ? (T) super.setDescription(description) : newModifiableInstance().setDescription(description);
    }
    
    @Override
    public T setShortDescription(String shortDescription) {
        return modifiable ? (T) super.setShortDescription(shortDescription) : newModifiableInstance().setShortDescription(shortDescription);
    }
    
    @Override
    public T preprocessArguments(IArgumentPreProcessor processor) {
        return modifiable ? (T) super.preprocessArguments(processor) : newModifiableInstance().preprocessArguments(processor);
    }
    
}
