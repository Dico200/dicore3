package io.dico.dicore.command;

public final class Parameter<T> {
    private ParameterType<T> type;
    private String name;
    private String flagPermission;
    private Object options;
    
    public Parameter(ParameterType<T> type, String name, String flagPermission, Object options) {
        this.type = type;
        this.name = name;
        this.flagPermission = flagPermission;
        this.options = options;
    }
    
    /**
     * @return The type of this parameter, which is responsible for parsing
     */
    public ParameterType<T> getType() {
        return type;
    }
    
    /**
     * @return The name of this parameter, which is automatically prefixed by a '-' if it is a flag.
     */
    public String getName() {
        return name;
    }
    
    /**
     * @return true if this parameter is a flag
     */
    public boolean isFlag() {
        return flagPermission != null;
    }
    
    /**
     * Get the permission required to use this flag parameter. If no such permission exists, or this is not a flag parameter, returns {@code null}.
     *
     * @return the flag permission
     */
    public String getFlagPermission() {
        return flagPermission;
    }
    
    /**
     * Get the options object for the parameter parser
     *
     * @return the options object
     */
    public Object getOptions() {
        return options;
    }
    
    public T getDefaultValue(ExecutionContextBuilder builder) {
    
    }
    
}
