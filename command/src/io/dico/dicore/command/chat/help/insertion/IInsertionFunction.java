package io.dico.dicore.command.chat.help.insertion;

import io.dico.dicore.command.ExecutionContext;
import io.dico.dicore.command.ICommandAddress;
import org.bukkit.permissions.Permissible;

public interface IInsertionFunction {

    int insertionIndex(int componentCount, ICommandAddress target, Permissible viewer, ExecutionContext context);
    
}
