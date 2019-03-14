package io.dico.dicore.command.parameter.type;

import io.dico.dicore.command.parameter.ArgumentBuffer;
import io.dico.dicore.command.parameter.IParameter;
import io.dico.dicore.command.CommandException;
import org.bukkit.command.CommandSender;

/**
 * An abstraction for parameter types that only parse a single argument
 *
 * @param <TReturn>    the parameter type
 * @param <TParamInfo> parameter info object type
 */
public abstract class SimpleParameterType<TReturn, TParamInfo> extends ParameterType<TReturn, TParamInfo> {
    
    public SimpleParameterType(Class<TReturn> returnType) {
        super(returnType);
    }
    
    public SimpleParameterType(Class<TReturn> returnType, ParameterConfig<?, TParamInfo> paramConfig) {
        super(returnType, paramConfig);
    }
    
    protected abstract TReturn parse(IParameter<TReturn> parameter, CommandSender sender, String input) throws CommandException;
    
    @Override
    public TReturn parse(IParameter<TReturn> parameter, CommandSender sender, ArgumentBuffer buffer) throws CommandException {
        return parse(parameter, sender, buffer.requireNext(parameter.getName()));
    }
    
}
