package io.dico.dicore.command.parameter;

import io.dico.dicore.command.CommandException;
import io.dico.dicore.command.ExecutionContext;
import io.dico.dicore.command.parameter.type.ParameterType;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

/**
 * Interface version of {@link IParameter} to remove the need for the second type parameter (most often {@link Void}) in command registration.
 * TODO: Make sure that this interface is referred to rather than the implementation of {@link IParameter} throughout the library.
 *
 * @param <TResult> the parameter type.
 */
public interface IParameter<TResult> {
    
    static <TResult> IParameter<TResult> newParameter(String name, String description, ParameterType<TResult, ?> type) {
        return new Parameter<>(name, description, type, null);
    }
    
    static <TResult, TParamInfo> IParameter<TResult> newParameter(String name, String description, ParameterType<TResult, TParamInfo> type, TParamInfo info) {
        return new Parameter<>(name, description, type, info);
    }
    
    static <TResult, TParamInfo> IParameter<TResult> newParameter(String name, String description, ParameterType<TResult, TParamInfo> parameterType, TParamInfo paramInfo, boolean flag, String flagPermission) {
        return new Parameter<>(name, description, parameterType, paramInfo, flag, flagPermission);
    }
    
    /**
     * @return The name of this parameter, which is automatically prefixed by a '-' if it is a flag.
     */
    String getName();
    
    /**
     * @return A description of (the purpose of) this parameter.
     */
    String getDescription();
    
    /**
     * @return the type of this parameter, which is responsible for parsing any input and providing completions.
     */
    ParameterType<TResult, ?> getType();
    
    /**
     * @return true if this parameter is a flag
     */
    boolean isFlag();
    
    /**
     * Get the permission required to use this flag parameter. If no such permission exists, or this is not a flag parameter, returns {@code null}.
     *
     * @return the flag permission
     */
    String getFlagPermission();
    
    /**
     * @return true if this parameter expects any input.
     * This is not the case for a flag parameter whose value is a boolean based on whether it is present or not.
     * It does not take any additional input after the flag.
     */
    boolean expectsInput();
    
    /**
     * Get the paramter info object for the type of this parameter
     * <p>
     * The paramter info object conveys the information given by an annotation,
     * but can be created without using an annotation because it isn't an annotation.
     *
     * @return the paramter info object, or {@code null} if it does not exist.
     */
    Object getParamInfo();
    
    /**
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
     * @throws CommandException if the input is not understood, or any other reason the parse might fail,
     *                          in which case a message explaining the problem is included.
     */
    TResult parse(ExecutionContext context, ArgumentBuffer buffer) throws CommandException;
    
    /**
     * Requests a value of this parameter's type without consuming any arguments.
     * Calling this method instead of {@link #parse(ExecutionContext, ArgumentBuffer)} indicates that no arguments are left.
     *
     * @param context the context of the execution
     * @param buffer  the arguments
     * @return a default value which might be derived based on information from the context or the arguments.
     * @throws CommandException if no default value can be computed, in which case a message explaining the problem is included.
     */
    TResult getDefaultValue(ExecutionContext context, ArgumentBuffer buffer) throws CommandException;
    
    /**
     * Requests completions from the parameter.
     *
     * @param context  the context of the execution/completion
     * @param location the location, as passed to {@link org.bukkit.command.Command#tabComplete(CommandSender, String, String[], Location)}
     * @param buffer   the arguments,
     * @return the completions, if any. {@code null} or {@link Collections#emptyList()} can be returned.
     * These values, as well as any empty list, have the same meaning: that no completions could be computed
     */
    List<String> complete(ExecutionContext context, Location location, ArgumentBuffer buffer);
    
}
