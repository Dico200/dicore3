package io.dico.dicore.command.parameter;

public final class ParameterParseContext {
    private ParameterParser parser;
    private Parameter<?> currentParameter;
    
    public ParameterParseContext(ParameterParser parser) {
        this.parser = parser;
    }
    
    void setCurrentParameter(Parameter<?> currentParameter) {
        this.currentParameter = currentParameter;
    }
    
    public ParameterParser getParser() {
        return parser;
    }
    
    public Parameter<?> getParameter() {
        return currentParameter;
    }
    
    public <TOptions> TOptions getOptions() {
        try {
            //noinspection unchecked
            return (TOptions) currentParameter.getOptions();
        } catch (ClassCastException ex) {
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
