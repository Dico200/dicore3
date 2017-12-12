package io.dico.dicore.command.parameter;

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
    
    
    
    /*
    / **
     * Parses the input to create a value of the type of this parameter.
     * For example, a name might be split off the buffer by calling its {@link ArgumentBuffer#next()} method
     * and a {@link org.bukkit.entity.Player} object might be parsed off it.
     * <p>
     * It is possible that none of the arguments in the buffer are requested.
     * This might be the case with flags that take no argument.
     *
     * @param context the context of the execution
     * @param buffer  the arguments
     * @return a value computed by parsing the input
     * @throws ParameterParseException if the input is not understood, or any other reason the parse might fail,
     *                                 in which case a message explaining the problem is included.
     * /
    */
    
}
