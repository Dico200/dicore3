package io.dico.dicore.command.chat.help.defaults;

import io.dico.dicore.Formatting;
import io.dico.dicore.command.EMessageType;
import io.dico.dicore.command.ExecutionContext;
import io.dico.dicore.command.ICommandAddress;
import io.dico.dicore.command.chat.IChatController;
import io.dico.dicore.command.chat.help.IPageBorder;
import io.dico.dicore.command.chat.help.IPageLayout;
import io.dico.dicore.command.chat.help.PageBorders;
import io.dico.dicore.command.predef.PredefinedCommand;
import org.bukkit.permissions.Permissible;

public class DefaultPageLayout implements IPageLayout {
    
    @Override
    public PageBorders getPageBorders(ICommandAddress target, Permissible viewer, ExecutionContext context, int pageNum) {
        IChatController c = context.getAddress().getChatController();
        String prefix = c.getMessagePrefixForType(EMessageType.INFORMATIVE);
        Formatting informative = c.getChatFormatForType(EMessageType.INFORMATIVE);
        Formatting number = c.getChatFormatForType(EMessageType.NEUTRAL);
        
        String nextPageCommand;
        ICommandAddress executor = context.getAddress();
        if (executor instanceof PredefinedCommand) {
            nextPageCommand = executor.getAddress() + ' ' + (pageNum + 1);
        } else {
            //todo
        }
        
        String header = prefix + informative + "Help page " + number + pageNum + informative +
                '/' + number + "%pageCount%" + informative + " for /" + target.getAddress();
        String footer = informative + "Type /" + context.getAddress().getAddress() + " help " + (pageNum + 1) + " for the next page";
        
        IPageBorder headerBorder = PageBorders.simpleBorder(header);
        IPageBorder footerBorder = PageBorders.disappearingBorder(pageNum, footer);
        return new PageBorders(headerBorder, footerBorder);
    }
    
}
