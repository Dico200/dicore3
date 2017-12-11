package io.dico.dicore.command.chat;

import io.dico.dicore.command.ExecutionContext;
import io.dico.dicore.command.ICommandAddress;
import io.dico.dicore.command.chat.help.IHelpTopic;
import io.dico.dicore.command.chat.help.IPageBuilder;
import io.dico.dicore.command.chat.help.IPageLayout;
import io.dico.dicore.command.chat.help.defaults.DefaultPageBuilder;
import io.dico.dicore.command.chat.help.defaults.DefaultPageLayout;
import io.dico.dicore.command.chat.help.defaults.DescriptionHelpTopic;
import io.dico.dicore.command.chat.help.defaults.SubcommandsHelpTopic;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.List;

/**
 * Static factory methods for {@link IChatController}
 */
public class ChatControllers {
    private static final IChatController defaultChat;
    
    private ChatControllers() {
    
    }
    
    public static IChatController defaultChat() {
        return defaultChat;
    }
    
    static {
        defaultChat = new AbstractChatController() {
            IPageBuilder pageBuilder = new DefaultPageBuilder();
            IPageLayout pageLayout = new DefaultPageLayout();
            List<IHelpTopic> topics = Arrays.asList(new DescriptionHelpTopic(), new SubcommandsHelpTopic());
            
            @Override
            public void sendHelpMessage(CommandSender sender, ExecutionContext context, ICommandAddress address, int page) {
                System.out.println("In defaultChat.sendHelpMessage()");
                System.out.println("Target address = " + address.getAddress());
                sender.sendMessage(pageBuilder.getPage(topics, pageLayout, address, sender, context, page, 10));
            }
        };
    }
}
