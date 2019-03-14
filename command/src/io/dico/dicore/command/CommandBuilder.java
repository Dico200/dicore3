package io.dico.dicore.command;

import io.dico.dicore.command.parameter.type.IParameterTypeSelector;
import io.dico.dicore.command.parameter.type.MapBasedParameterTypeSelector;
import io.dico.dicore.command.parameter.type.ParameterType;
import io.dico.dicore.command.predef.HelpCommand;
import io.dico.dicore.command.predef.PredefinedCommand;
import io.dico.dicore.command.predef.SyntaxCommand;
import io.dico.dicore.command.registration.reflect.ReflectiveRegistration;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Mimic of WorldEdit's CommandGraph
 */
public final class CommandBuilder {
    private final RootCommandAddress root;
    private ModifiableCommandAddress cur;
    private IParameterTypeSelector selector = new MapBasedParameterTypeSelector(true);
    
    /**
     * Instantiate a new CommandBuilder with a new command root system
     * Commands registered to this command builder might interfere with
     * commands registered to other commands builders or by other plugins.
     */
    public CommandBuilder() {
        this(new RootCommandAddress());
    }
    
    /**
     * Instantiate a new CommandBuilder with a specified root address.
     * If the root address is identical to that of another command builder,
     * they will modify the same tree.
     *
     * @param root the root address
     */
    public CommandBuilder(RootCommandAddress root) {
        this.root = Objects.requireNonNull(root);
        this.cur = root;
    }
    
    /**
     * Add a sub command at the current address
     * The current address can be inspected using {@link #getAddress()}
     *
     * @param name    the name of the command
     * @param command the command executor
     * @param aliases any aliases
     * @return this
     */
    public CommandBuilder addSubCommand(String name, Command command, String... aliases) {
        ChildCommandAddress address = new ChildCommandAddress(command);
        address.addNameAndAliases(name, aliases);
        return addSubCommand(address);
    }
    
    /**
     * Add a subcommand as an address at the current address
     * The result of this call is the same as
     * {@code addSubCommand(address.getMainKey(), address.getCommand(), address.getNames().sublist(1).toArray(new String[0]))}
     *
     * @param address the address
     * @return this
     * @throws IllegalArgumentException if {@code address.isRoot()}
     */
    public CommandBuilder addSubCommand(ICommandAddress address) {
        cur.addChild(address);
        return this;
    }
    
    /**
     * Search the given class for any (static) methods using command annotations
     * The class gets a localized parameter type selector if it defines parameter types.
     * Any commands found are registered as sub commands to the current address.
     *
     * @param clazz the clazz
     * @return this
     * @throws IllegalArgumentException if an exception occurs while parsing the methods of this class
     * @see #registerCommands(Class, Object)
     */
    public CommandBuilder registerCommands(Class<?> clazz) {
        return registerCommands(clazz, null);
    }
    
    /**
     * Search the given object's class for methods using command annotations.
     * If the object is null, only static methods are checked. Otherwise, instance methods are also checked.
     * The class gets a localized parameter type selector if it defines parameter types.
     * Any commands found are registered as sub commands to the current address.
     *
     * @param object the object
     * @return this
     * @throws IllegalArgumentException if an exception occurs while parsing the methods of this class
     * @see #registerCommands(Class, Object)
     */
    public CommandBuilder registerCommands(Object object) {
        return registerCommands(object.getClass(), object);
    }
    
    /**
     * Search the given class for methods using command annotations.
     * The class gets a localized parameter type selector if it defines parameter types.
     * Any commands found are registered as sub commands to the current address.
     * The instance is used to invoke non-static methods.
     *
     * @param clazz    the class
     * @param instance the instance, null if only static methods
     * @return this
     * @throws IllegalArgumentException if instance is not null and it's not an instance of the class
     * @throws IllegalArgumentException if another exception occurs while parsing the methods of this class
     */
    public CommandBuilder registerCommands(Class<?> clazz, Object instance) {
        try {
            ReflectiveRegistration.parseCommandGroup(cur, selector, clazz, instance);
            return this;
        } catch (Exception ex) {
            throw new IllegalArgumentException(ex);
        }
    }
    
    /**
     * register the {@link HelpCommand} as a sub command at the current address
     *
     * @return this
     */
    public CommandBuilder registerHelpCommand() {
        HelpCommand.registerAsChild(cur);
        return this;
    }
    
    /**
     * register the {@link SyntaxCommand} as a sub command a the current address
     *
     * @return this
     */
    public CommandBuilder registerSyntaxCommand() {
        SyntaxCommand.registerAsChild(cur);
        return this;
    }
    
    /**
     * Generate the predefined commands.
     * These are presets.
     * Examples include {@code help} and {@code syntax}.
     * <p>
     * Predefined commands can be registered through {@link PredefinedCommand#registerPredefinedCommandGenerator(String, Consumer)}
     *
     * @param commands the commands
     * @return this
     */
    public CommandBuilder generatePredefinedCommands(String... commands) {
        for (String value : commands) {
            Consumer<ICommandAddress> subscriber = PredefinedCommand.getPredefinedCommandGenerator(value);
            if (subscriber == null) {
                System.out.println("[Command Warning] generated command '" + value + "' could not be found");
            } else {
                subscriber.accept(cur);
            }
        }
        return this;
    }
    
    /**
     * Jump to the sub-address with the given name as main key.
     * If an address with the exact name as main key exists,
     * that address becomes the current address.
     * <p>
     * Otherwise, a new addresses is registered with the name and aliases.
     * <p>
     * After this call, any registered commands are registered as a sub command
     * to the new address. To restore the previous state, a call to {@link #parent()}
     * should be made.
     * <p>
     * If the address is the target of a command, it will provide information about its sub commands
     * using the HelpCommand.
     *
     * @param name    the main key
     * @param aliases the aliases
     * @return this
     */
    public CommandBuilder group(String name, String... aliases) {
        ChildCommandAddress address = cur.getChild(name);
        if (address == null || !name.equals(address.getMainKey())) {
            cur.addChild(address = ChildCommandAddress.newPlaceHolderCommand(name, aliases));
        }
        cur = address;
        return this;
    }
    
    /**
     * Sets the description of a group created by {@link #group(String, String...)}
     * Should be called subsequently to making a call to {@link #group(String, String...)}
     *
     * @param shortDescription a short description
     * @param description      the lines of a full description.
     * @return this
     */
    public CommandBuilder setGroupDescription(String shortDescription, String... description) {
        Command command = cur.getCommand();
        if (command == null || !(command instanceof HelpCommand)) {
            throw new IllegalStateException("Not in a group created by #group()");
        }
        
        cur.setCommand(((HelpCommand) command)
                .setShortDescription(shortDescription)
                .setDescription(description));
        return this;
    }
    
    /**
     * Jump up a level in the address
     *
     * @return this
     * @throws IllegalStateException if the address is empty
     *                               // has a depth of 0 // is at level 0
     */
    public CommandBuilder parent() {
        if (cur.hasParent()) {
            cur = cur.getParent();
            return this;
        }
        throw new IllegalStateException("No parent exists at this address");
    }
    
    /**
     * Jump to the root (empty) address,
     * such that a subsequent call to {@link #parent()}
     * will throw a {@link IllegalStateException}
     *
     * @return this
     */
    public CommandBuilder root() {
        cur = root;
        return this;
    }
    
    /**
     * Get the current address, as a space-separated string
     *
     * @return the current address
     */
    public String getAddress() {
        return cur.getAddress();
    }
    
    /**
     * Get the depth of the current address.
     * This is equivalent to {@code getAddress().split(" ").length}.
     * If the address is empty, the depth is 0.
     *
     * @return the depth
     */
    public int getDepth() {
        return cur.getDepth();
    }
    
    /**
     * Set the command at the current group. The command is set
     * a level higher than it would be if this were a call to {@link #addSubCommand(String, Command, String...)}
     * <p>
     * If a call to {@link #setGroupDescription(String, String...)} was made at the same address before,
     * the description is copied to the given executor.
     *
     * @param command the executor
     * @return this
     * @throws IllegalArgumentException if the command at the address is present and declared by the user,
     *                                  in other words, it's not a {@link PredefinedCommand}
     */
    public CommandBuilder setCommand(Command command) {
        Command current = cur.getCommand();
        if (current instanceof HelpCommand && current != HelpCommand.INSTANCE) {
            command.setShortDescription(current.getShortDescription());
            command.setDescription(current.getDescription());
        }
        
        cur.setCommand(command);
        return this;
    }
    
    /**
     * Add the parameter type to this builder's selector.
     *
     * @param type the type
     * @param <T> the return type of the parameter type
     * @return this
     */
    public <T> CommandBuilder addParameterType(ParameterType<T, Void> type) {
        selector.addType(false, type);
        return this;
    }
    
    /**
     * Add the parameter type to this builder's selector.
     *
     * @param infolessAlias whether to also register the type with an infoless alias.
     *                      this increases the priority assigned to the type if no info object is present.
     * @param type the type
     * @param <T> the return type of the parameter type
     * @param <C> the parameter config type (info object)
     * @return this
     */
    public <T, C> CommandBuilder addParameterType(boolean infolessAlias, ParameterType<T, C> type) {
        selector.addType(infolessAlias, type);
        return this;
    }
    
    /**
     * Get the dispatcher for the root address.
     * The dispatcher should be used to finally register all commands,
     * after they are all declared.
     *
     * @return the dispatcher
     */
    public ICommandDispatcher getDispatcher() {
        return root;
    }
    
    
    
}
