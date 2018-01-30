package io.dico.dicore.command.predef;

import io.dico.dicore.command.*;
import io.dico.dicore.command.annotation.Range;
import io.dico.dicore.command.parameter.IParameter;
import io.dico.dicore.command.parameter.Parameter;
import io.dico.dicore.command.parameter.type.ParameterTypes;
import org.bukkit.command.CommandSender;

/**
 * The help command
 */
public class HelpCommand extends PredefinedCommand<HelpCommand> {
    private static final IParameter<Integer> pageParameter = new Parameter<>("page", "the page number",
            ParameterTypes.INTEGER, new Range.Memory(1, Integer.MAX_VALUE, 1));
    public static final HelpCommand INSTANCE = new HelpCommand(false);
    
    private HelpCommand(boolean modifiable) {
        super(modifiable);
        getParameterList().addParameter(pageParameter);
        getParameterList().setRequiredCount(0);
        setDescription("Shows this help page");
    }
    
    @Override
    protected HelpCommand newModifiableInstance() {
        return new HelpCommand(true);
    }
    
    @Override
    public String execute(CommandSender sender, ExecutionContext context) throws CommandException {
        //System.out.println("In HelpCommand.execute");
        context.getAddress().getChatController().sendHelpMessage(sender, context, context.getAddress().getParent(), context.<Integer>get("page") - 1);
        return null;
    }
    
    public static void registerAsChild(ICommandAddress address) {
        registerAsChild(address, "help");
    }
    
    public static void registerAsChild(ICommandAddress address, String main, String... aliases) {
        ((ModifiableCommandAddress) address).addChild(new ChildCommandAddress(INSTANCE, main, aliases));
    }
    
}
