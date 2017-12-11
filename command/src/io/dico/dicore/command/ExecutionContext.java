package io.dico.dicore.command;

import io.dico.dicore.Formatting;
import io.dico.dicore.command.parameter.ArgumentBuffer;
import io.dico.dicore.command.parameter.IParameter;
import io.dico.dicore.command.parameter.ParameterList;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;

import java.lang.reflect.Array;
import java.util.*;

/**
 * The context of execution.
 * <p>
 * This class is responsible for the control flow of parameter parsing, as well as caching and providing the parsed parameter values.
 * It is also responsible for keeping track of the parameter to complete in the case of a tab completion.
 */
public class ExecutionContext {
    private final CommandSender sender;
    private final ICommandAddress address;
    private final ArgumentBuffer originalBuffer;
    private final ArgumentBuffer processedBuffer;
    
    // caches the buffer's cursor before parsing. This is needed to provide the original input of the player.
    private final int cursorStart;
    
    // The parsed parameter values, mapped by parameter name.
    // This also includes default values. All parameters from the parameter list are present if parsing was successful.
    private Map<String, Object> parameterValues;
    // this set contains the names of the parameters that were present in the command, and not given a default value.
    private Set<String> parsedParameters;
    
    // when the context starts parsing parameters, this flag is set, and any subsequent calls to #parseParameters() throw an IllegalStateException.
    private boolean attemptedToParse;
    
    // these fields store information required to provide completions.
    // the parameter to complete is the parameter that threw an exception when it was parsing.
    // the exception's message was discarded because it is a completion.
    private IParameter<?> parameterToComplete;
    // this is the cursor that the ArgumentBuffer is reset to when suggested completions are requested.
    private int parameterToCompleteCursor = -1;
    
    // if this flag is set, any messages sent through the sendMessage methods are discarded.
    private boolean quiet;
    
    /**
     * Construct an execution context, making it ready to parse the parameter values.
     *
     * @param sender      the sender
     * @param address     the address
     * @param buffer      the arguments
     * @param tabComplete true if this execution is a tab-completion
     */
    public ExecutionContext(CommandSender sender, ICommandAddress address, ArgumentBuffer buffer, boolean tabComplete) {
        //System.out.println("In ExecutionContext.ctor");
        this.sender = Objects.requireNonNull(sender);
        this.address = Objects.requireNonNull(address);
        this.quiet = tabComplete;
        
        if (!address.hasCommand()) {
            throw new IllegalArgumentException("no command found");
        }
        
        //System.out.println("buffer: " + Arrays.toString(buffer.getArrayFromIndex(0)) + ", cursor: " + buffer.getCursor());
        
        this.originalBuffer = buffer;
        this.processedBuffer = buffer.preprocessArguments(getParameterList().getArgumentPreProcessor());
        this.cursorStart = buffer.getCursor();
    }
    
    /**
     * Parse the parameters. If no exception is thrown, they were parsed successfully, and the command may continue post-parameter execution.
     *
     * @throws CommandException if an error occurs while parsing the parameters.
     */
    public void parseParameters() throws CommandException {
        if (attemptedToParse) {
            throw new IllegalStateException();
        }
        
        attemptedToParse = true;
        
        ParameterList parameterList = getParameterList();
        ArgumentBuffer buffer = this.processedBuffer;
        Map<String, Object> parameterValues = this.parameterValues = new HashMap<>();
        Set<String> parsedParameters = this.parsedParameters = new HashSet<>();
        List<IParameter> indexedParameters = parameterList.getIndexedParameters();
        IParameter repeated = parameterList.getRepeatedParameter();
        
        int indexCount = indexedParameters.size();
        int requiredCount = parameterList.getRequiredCount();
        //System.out.println("requiredCount = " + requiredCount);
        IParameter param = null;
        boolean repeatedSeen = false;
        int index = 0;
        // used for completions. The buffer is reset to this cursor when requesting completions from the parameter which threw an exception.
        int cursorOnStartParamParse = -1;
        try {
            
            // traverse each argument one-by-one
            String arg;
            while (buffer.hasNext()) {
                arg = buffer.next();
                
                //noinspection StatementWithEmptyBody
                if (arg.startsWith("-")
                        && (param = parameterList.getParameterByName(arg)) != null
                        && param.isFlag()
                        && !parsedParameters.contains(arg)
                        && (param.getFlagPermission() == null || sender.hasPermission(param.getFlagPermission()))) {
                    
                    // do nothing; param is set
                    // buffer has advanced
                } else {
                    // need parser to include the arg, so rewind
                    buffer.rewind();
                    
                    if (repeatedSeen) {
                        // if a repeated (flag) parameter occurs before a required index parameter, this will make it end up throwing an error
                        // when it finds that the required parameter isn't set.
                        param = repeated;
                    } else if (index < indexCount) {
                        // if the repeated parameter was selected in other ways, default to it.
                        param = indexedParameters.get(index++);
                    } else {
                        throw new CommandException("Too many arguments");
                    }
                }
                
                cursorOnStartParamParse = buffer.getCursor();
                parsedParameters.add(param.getName());
                if (param == repeated) {
                    repeatedSeen = true;
                    
                    List<Object> list;
                    Object cur = parameterValues.get(param.getName());
                    if (cur instanceof List) {
                        //noinspection unchecked
                        list = (List<Object>) cur;
                    } else {
                        //System.out.println("Making collection for repeated parameter");
                        list = new ArrayList<>();
                        if (cur != null) {
                            list.add(cur);
                        }
                        parameterValues.put(param.getName(), list);
                    }
                    
                    list.add(param.parse(this, buffer));
                } else {
                    parameterValues.put(param.getName(), param.parse(this, buffer));
                }
                
            }
            
            //System.out.println("index = " + index);
            //System.out.println("requiredCount = " + requiredCount);
            if (index < requiredCount) {
                throw new CommandException("The argument '" + indexedParameters.get(index).getName() + "' is required");
            }
            
        } catch (CommandException ex) {
            parameterToComplete = param;
            parameterToCompleteCursor = cursorOnStartParamParse;
            throw ex;
        } finally {
            
            // add default values for unset parameters
            for (Map.Entry<String, IParameter> entry : parameterList.getParametersByName().entrySet()) {
                String name = entry.getKey();
                if (!parsedParameters.contains(name)) {
                    if (repeated == entry.getValue()) {
                        parameterValues.put(name, Collections.emptyList());
                    } else {
                        parameterValues.put(name, entry.getValue().getDefaultValue(this, buffer));
                    }
                }
            }
            
        }
        
        //array-ify for varargs
        if (repeated != null) {
            //System.out.println("Array-ifying repeated parameter");
            parameterValues.computeIfPresent(repeated.getName(), (k, v) -> {
                List list = (List) v;
                Class<?> returnType = repeated.getType().getReturnType();
                Object array = Array.newInstance(returnType, list.size());
                ArraySetter setter = ArraySetter.getSetter(returnType);
                for (int i = 0, n = list.size(); i < n; i++) {
                    setter.set(array, i, list.get(i));
                }
                
                return array;
            });
        }
        
    }
    
    /*
    Need to implement weird shit because java primitives are just awesome.
     */
    private interface ArraySetter {
        void set(Object array, int index, Object value);
    
        static ArraySetter getSetter(Class<?> clazz) {
            if (!clazz.isPrimitive()) {
                return Array::set;
            }
        
            switch (clazz.getSimpleName()) {
                case "boolean":
                    return (array, index, value) -> Array.setBoolean(array, index, (boolean) value);
                case "int":
                    return (array, index, value) -> Array.setInt(array, index, (int) value);
                case "double":
                    return (array, index, value) -> Array.setDouble(array, index, (double) value);
                case "long":
                    return (array, index, value) -> Array.setLong(array, index, (long) value);
                case "short":
                    return (array, index, value) -> Array.setShort(array, index, (short) value);
                case "byte":
                    return (array, index, value) -> Array.setByte(array, index, (byte) value);
                case "float":
                    return (array, index, value) -> Array.setFloat(array, index, (float) value);
                case "char":
                    return (array, index, value) -> Array.setChar(array, index, (char) value);
                case "void":
                default:
                    throw new InternalError("This should not happen");
            }
        }
    }
    
    /**
     * Attempts to parse parameters, without throwing an exception or sending any message.
     * This method is typically used by tab completions.
     * After calling this method, the context is ready to provide completions.
     */
    public void parseParametersQuietly() {
        boolean before = quiet;
        quiet = true;
        try {
            parseParameters();
        } catch (CommandException ignored) {
            
        } finally {
            quiet = before;
        }
    }
    
    /**
     * Sender of the command
     *
     * @return the sender of the command
     */
    public CommandSender getSender() {
        return sender;
    }
    
    /**
     * Command's address
     *
     * @return the command's address
     */
    public ICommandAddress getAddress() {
        return address;
    }
    
    /**
     * The command
     *
     * @return the command
     */
    public Command getCommand() {
        return address.getCommand();
    }
    
    /**
     * The command's parameter definition.
     *
     * @return the parameter list
     */
    public ParameterList getParameterList() {
        return address.getCommand().getParameterList();
    }
    
    /**
     * Get the buffer as it was before preprocessing the arguments.
     *
     * @return the original buffer
     */
    public ArgumentBuffer getOriginalBuffer() {
        return originalBuffer;
    }
    
    /**
     * The arguments
     *
     * @return the argument buffer
     */
    public ArgumentBuffer getProcessedBuffer() {
        return processedBuffer;
    }
    
    /**
     * The cursor start, in other words, the buffer's cursor before parameters were parsed.
     *
     * @return the cursor start
     */
    public int getCursorStart() {
        return cursorStart;
    }
    
    /**
     * The original arguments.
     *
     * @return original arguments.
     */
    public String[] getOriginal() {
        return originalBuffer.getArrayFromIndex(cursorStart);
    }
    
    /**
     * The full command as cached by the buffer. Might be incomplete depending on how it was dispatched.
     *
     * @return the full command
     */
    public String getRawInput() {
        return originalBuffer.getRawInput();
    }
    
    @SuppressWarnings("unchecked")
    public <T> T get(String name) {
        if (!parameterValues.containsKey(name)) {
            throw new IllegalArgumentException();
        }
        
        try {
            return (T) parameterValues.get(name);
        } catch (ClassCastException ex) {
            throw new IllegalArgumentException("Invalid type parameter requested for parameter " + name, ex);
        }
    }
    
    @SuppressWarnings("unchecked")
    public <T> T get(int index) {
        return get(getParameterList().getIndexedParameterName(index));
    }
    
    public <T> T getFlag(String flag) {
        return get("-" + flag);
    }
    
    /**
     * Checks if the parameter by the name was provided in the command's arguments.
     *
     * @param name the parameter name
     * @return true if it was provided
     */
    public boolean isProvided(String name) {
        return parsedParameters.contains(name);
    }
    
    /**
     * Checks if the parameter by the index was provided in the command's arguments.
     *
     * @param index the parameter index
     * @return true if it was provided
     */
    public boolean isProvided(int index) {
        return isProvided(getParameterList().getIndexedParameterName(index));
    }
    
    /**
     * The parameter to complete.
     * This parameter is requested suggestions
     *
     * @return the parameter to complete.
     */
    public IParameter getParameterToComplete() {
        return parameterToComplete;
    }
    
    /**
     * @return true if this context is for a tab completion.
     */
    public boolean isTabComplete() {
        return quiet;
    }
    
    /**
     * Get suggested completions.
     *
     * @param location The location as passed to {@link org.bukkit.command.Command#tabComplete(CommandSender, String, String[], Location)}, or null if requested in another way.
     * @return completions.
     */
    public List<String> getSuggestedCompletions(Location location) {
        if (parameterToComplete != null) {
            return parameterToComplete.complete(this, location, processedBuffer.getUnaffectingCopy().setCursor(parameterToCompleteCursor));
        }
        
        ParameterList parameterList = getParameterList();
        List<String> result = new ArrayList<>();
        for (String name : parameterValues.keySet()) {
            if (parameterList.getParameterByName(name).isFlag() && !parsedParameters.contains(name)) {
                result.add(name);
            }
        }
        return result;
    }
    
    public void sendMessage(String message) {
        sendMessage(true, message);
    }
    
    public void sendMessage(EMessageType messageType, String message) {
        sendMessage(messageType, true, message);
    }
    
    public void sendMessage(boolean translateColours, String message) {
        sendMessage(EMessageType.NEUTRAL, translateColours, message);
    }
    
    public void sendMessage(EMessageType messageType, boolean translateColours, String message) {
        if (!quiet) {
            if (translateColours) {
                message = Formatting.translateChars('&', message);
            }
            address.getChatController().sendMessage(this, messageType, message);
        }
    }
    
    public void sendMessage(String messageFormat, Object... args) {
        sendMessage(true, messageFormat, args);
    }
    
    public void sendMessage(EMessageType messageType, String messageFormat, Object... args) {
        sendMessage(messageType, true, messageFormat, args);
    }
    
    public void sendMessage(boolean translateColours, String messageFormat, Object... args) {
        sendMessage(EMessageType.NEUTRAL, translateColours, messageFormat, args);
    }
    
    public void sendMessage(EMessageType messageType, boolean translateColours, String messageFormat, Object... args) {
        sendMessage(messageType, translateColours, String.format(messageFormat, args));
    }
    
    public void sendHelpMessage(int page) {
        if (!quiet) {
            address.getChatController().sendHelpMessage(sender, this, address, page);
        }
    }
    
    public void sendSyntaxMessage() {
        if (!quiet) {
            address.getChatController().sendSyntaxMessage(sender, this, address);
        }
    }
    
}
