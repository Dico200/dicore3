package io.dico.dicore.command.chat.help.defaults;

import io.dico.dicore.Formatting;
import io.dico.dicore.command.EMessageType;
import io.dico.dicore.command.ExecutionContext;
import io.dico.dicore.command.ICommandAddress;
import io.dico.dicore.command.chat.help.IHelpComponent;
import io.dico.dicore.command.chat.help.IHelpTopic;
import io.dico.dicore.command.chat.help.SimpleHelpComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permissible;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SubcommandsHelpTopic implements IHelpTopic {
    
    @Override
    public List<IHelpComponent> getComponents(ICommandAddress target, Permissible viewer, ExecutionContext context) {
        List<IHelpComponent> out = new ArrayList<>();
        Map<String, ? extends ICommandAddress> children = target.getChildren();
        if (children.isEmpty()) {
            System.out.println("No subcommands");
            return out;
        }
    
        CommandSender sender = viewer instanceof CommandSender ? (CommandSender) viewer : context.getSender();
        children.values().stream().distinct().forEach(child -> {
            if (!child.hasCommand() || child.getCommand().isVisibleTo(sender)) {
                out.add(getComponent(child, viewer, context));
            }
        });
       
        return out;
    }
    
    public IHelpComponent getComponent(ICommandAddress child, Permissible viewer, ExecutionContext context) {
        Formatting instruction = context.getAddress().getChatController().getChatFormatForType(EMessageType.INSTRUCTION);
        String description = child.hasCommand() ? child.getCommand().getShortDescription() : null;
        String address = instruction + "/" + child.getAddress();
        if (description != null) {
            Formatting informative = context.getAddress().getChatController().getChatFormatForType(EMessageType.INFORMATIVE);
            return new SimpleHelpComponent(address, informative + description);
        }
        return new SimpleHelpComponent(address);
    }
    
}
