package io.dico.dicore.command;

public final class ParameterParseContext {
    private ExecutionContextBuilder contextBuilder;
    private Parameter<?> currentParameter;
    
    public ParameterParseContext(ExecutionContextBuilder contextBuilder) {
        this.contextBuilder = contextBuilder;
    }
    
    void setCurrentParameter(Parameter<?> currentParameter) {
        this.currentParameter = currentParameter;
    }
    
    public ExecutionContextBuilder getContextBuilder() {
        return contextBuilder;
    }
    
    public Parameter<?> getParameter() {
        return currentParameter;
    }
    
    public <TOptions> TOptions getOptions() {
        try {
            //noinspection unchecked
            return (TOptions) currentParameter.getOptions();
        } catch (ClassCastException ex) {
            Constants.logger.severe("ParameterType " + currentParameter.getType() +
                    " for type " + currentParameter.getType().getReturnType().getTypeName() +
                    " requested its options in the wrong type: ");
            Constants.logger.throwing(ParameterParseContext.class.getName(), "Object getOptions()", ex);
            return null;
        }
    }
    
    public boolean isFlag() {
        return currentParameter.isFlag();
    }
    
    public ParameterParseException invalidSyntax() {
        // TODO
        return new ParameterParseException("invalid syntax");
    }
    
}
