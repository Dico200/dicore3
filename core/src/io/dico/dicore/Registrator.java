package io.dico.dicore;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.*;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.*;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.*;
import java.util.*;
import java.util.function.Consumer;
import java.util.logging.Logger;

/**
 * This class acts as a utility to register event listeners in a functional manner.
 * Listeners passed are always {@code <T> Consumer<T extends Event>} objects.
 * <p>
 * Registrations are made using its
 * * {@link #registerListener(Class, Consumer)}
 * * {@link #registerListener(Class, EventPriority, Consumer)}
 * * {@link #registerListener(Class, boolean, Consumer)}
 * * {@link #registerListener(Class, EventPriority, boolean, Consumer)}
 * * {@link #registerListeners(Class)}
 * * {@link #registerListeners(Object)}
 * * {@link #registerListeners(Class, Object)}
 * * {@link #registerPlayerLeaveListener(Consumer)}
 * methods.
 * <p>
 * Listeners registered in this way are generally a bit faster than when registered through {@link org.bukkit.plugin.PluginManager#registerEvents(Listener, Plugin)}
 * Because it does not use reflection to call the event handlers.
 *
 * @implNote This class uses only one {@link Listener listener object} across all its instances, by fooling spigot into
 * thinking they're all distinct ones (by violating the {@link Object#equals(Object)} contract).
 * <p>
 * Standard Registrator instances also use a fake plugin identity to register its listeners.
 * You can use the {{@link #Registrator(Plugin)}} constructor to use real plugin identities.
 */
public final class Registrator {

    // ############################################
    // # Public static methods
    // ############################################

    public static Registrator getInstance() {
        return instance;
    }

    // ############################################
    // # Static fields and initializer
    // ############################################

    private static final Registrator instance;
    private static final Listener universalListenerObject;
    private static final Plugin defaultFakePlugin;
    private static final Map<Class<?>, HandlerListInfo> handlerListCache;

    static {
        handlerListCache = new IdentityHashMap<>();
        defaultFakePlugin = new RegistratorPlugin("Default");
        instance = new Registrator();
        universalListenerObject = new Listener() {

            //@formatter:off
            /** return false here to fool the HandlerList into believing each registration is from another Listener.
             * as a result, no exceptions will be thrown when registering multiple listeners for the same event and priority.
             *
             * Another option is to have this for each instance:
             *
             *
             * <pre>{@code
            private Listener getListenerFor(HandlerList list, EventPriority priority) {
            int needed = (int) (listeners.get(list).stream().filter(listener -> listener.getPriority() == priority).count() + 1);
            while (needed > myListeners.size()) {
            myListeners.add(new Listener() {});
            }
            return myListeners.get(needed - 1);
            }
             * }</pre>
             *
             *
             * Where {@code myListeners} is a List<Listener>
             *
             *
             */
            //@formatter:on
            @Override
            public boolean equals(Object obj) {

                return false;
            }
        };
    }

    // ############################################
    // # Instance fields and constructors
    // ############################################

    private final List<Registration> registrations;
    private Plugin plugin;
    private Registration pluginEnableListener;
    private Registration pluginDisableListener;
    private boolean enabled;

    /**
     * Constructs a new instance using the {@link #defaultFakePlugin universal plugin object}
     */
    public Registrator() {
        this(defaultFakePlugin);
    }

    /**
     * Constructs a new instance using a newly instantiated artificial plugin.
     *
     * @param name an indicator for the name to use with the artificial plugin.
     */
    public Registrator(String name) {
        this(new RegistratorPlugin(name));
    }

    /**
     * Constructs a new instance using the given plugin
     *
     * @param plugin The plugin to register the listeners with
     * @throws NullPointerException if plugin is null
     */
    public Registrator(Plugin plugin) {
        this.registrations = new ArrayList<>();
        setPlugin(plugin);
    }

    // ############################################
    // # Internal static methods
    // ############################################

    /**
     * static {@link EventExecutor} instantiator to make sure executors don't reference any objects unnecessarily.
     */
    @SuppressWarnings("unchecked")
    private static <T extends Event> EventExecutor newEventExecutor(Class<T> eventClass, Consumer<? super T> handler) {
        if (getHandlerListInfoOf(eventClass).requiresFilter) {
            return (ignored, event) -> {
                if (eventClass.isInstance(event)) {
                    handler.accept((T) event);
                }
            };
        }
        return (ignored, event) -> handler.accept((T) event);
    }

    /**
     * Reflectively acquire the HandlerList for the given event type.
     *
     * @param eventClass the Event type
     * @return its HandlerList, or null if one can't be found
     */
    private static HandlerList getHandlerListOf(Class<?> eventClass) {
        try {
            return getHandlerListInfoOf(eventClass).handlerList;
        } catch (RuntimeException e) {
            return null;
        }
    }

    private static HandlerListInfo getHandlerListInfoOf(Class<?> eventClass) {
        return handlerListCache.computeIfAbsent(eventClass, clz -> {
            Method method = Reflection.deepSearchMethod(clz, "getHandlerList");
            boolean requiresFilter = clz != method.getDeclaringClass();
            return new HandlerListInfo(Reflection.invokeStaticMethod(method), requiresFilter);
        });
    }

    // ############################################
    // # Public instance methods
    // ############################################

    /**
     * Change the plugin used by the listeners of this registrator.
     *
     * @param plugin the plugin to use
     * @throws NullPointerException  if plugin is null
     * @throws IllegalStateException if this registrator was returned by {@link #getInstance()}
     */
    public void setPlugin(Plugin plugin) {
        Objects.requireNonNull(plugin);
        if (this.plugin == plugin) {
            return;
        }

        if (this.plugin != null) {
            if (this == instance) {
                throw new IllegalStateException("You may not modify the plugin used by the universal Registrator instance");
            }

            setEnabled(false);
            setPluginListenerRegisteredStates(false, false);
            setListenersPluginTo(plugin);
        }

        this.plugin = plugin;
        initPluginListeners();
        updatePluginListeners(plugin.isEnabled());
        if (plugin.isEnabled()) {
            setEnabled(true);
        }
    }

    /**
     * @return The plugin object used when registering the listeners
     */
    public Plugin getRegistrationPlugin() {
        return plugin;
    }

    /**
     * @return True if the plugin used was artificial / not an actual plugin on the server / fooled the bukkit api
     */
    public boolean hasFakePlugin() {
        return plugin instanceof RegistratorPlugin;
    }

    /**
     * @return An unmodifiable view of the registrations made by this {@link Registrator}
     */
    public List<Registration> getListeners() {
        return Collections.unmodifiableList(registrations);
    }

    /**
     * Register a listener for the given event type.
     * the event priority is set to {@link EventPriority#HIGHEST}
     * the ignore cancelled flag is set to {@code true}
     *
     * @param eventClass The event type
     * @param handler    the listener
     * @param <T>        the event type
     * @return this
     */
    public <T extends Event> Registrator registerListener(Class<T> eventClass, Consumer<? super T> handler) {
        return registerListener(eventClass, EventPriority.HIGHEST, handler);
    }

    /**
     * Register a listener for the given event type.
     * The ignoreCancelled flag is set to false if {@code priority} is {@link EventPriority#LOW} or {@link EventPriority#LOWEST}
     * otherwise, it is set to true.
     *
     * @param eventClass The event type
     * @param priority   the event priority
     * @param handler    the listener
     * @param <T>        the event type
     * @return this
     */
    public <T extends Event> Registrator registerListener(Class<T> eventClass, EventPriority priority, Consumer<? super T> handler) {
        boolean ignoreCancelled = Cancellable.class.isAssignableFrom(eventClass) && priority.getSlot() > EventPriority.LOW.getSlot();
        return registerListener(eventClass, priority, ignoreCancelled, handler);
    }

    /**
     * Register a listener for the given event type.
     * If {@code ignoreCancelled} is true, the event priority is set to {@link EventPriority#HIGHEST}
     * Otherwise, it is set to {@link EventPriority#LOW}
     *
     * @param eventClass      The event type
     * @param ignoreCancelled the ignoreCancelled flag of the listener
     * @param handler         The listener
     * @param <T>             The event type
     * @return this
     */
    public <T extends Event> Registrator registerListener(Class<T> eventClass, boolean ignoreCancelled, Consumer<? super T> handler) {
        return registerListener(eventClass, ignoreCancelled ? EventPriority.HIGHEST : EventPriority.LOW, ignoreCancelled, handler);
    }

    /**
     * Register a listener for the given event type.
     *
     * @param eventClass      The event type
     * @param priority        the event priority
     * @param ignoreCancelled the ignoreCancelled flag of the listener
     * @param handler         the listener
     * @param <T>             the event type
     * @return this
     */
    public <T extends Event> Registrator registerListener(Class<T> eventClass, EventPriority priority, boolean ignoreCancelled, Consumer<? super T> handler) {
        registerListener(createRegistration(priority, ignoreCancelled, eventClass, handler));
        return this;
    }

    public Registrator registerListeners(Class<?> clazz, Object instance) {
        for (ListenerFieldInfo fieldInfo : getListenerFields(clazz, instance)) {
            registerListener(fieldInfo.eventClass, fieldInfo.anno.priority(), fieldInfo.anno.ignoreCancelled(), fieldInfo.lambda);
        }
        return this;
    }

    public Registrator registerListeners(Class<?> clazz) {
        return registerListeners(clazz, null);
    }

    public Registrator registerListeners(Object instance) {
        return registerListeners(instance.getClass(), instance);
    }

    public Registrator registerPlayerLeaveListener(Consumer<? super PlayerEvent> handler) {
        registerListener(PlayerQuitEvent.class, EventPriority.NORMAL, handler);
        return registerListener(PlayerKickEvent.class, EventPriority.NORMAL, handler);
    }

    @Override
    public String toString() {
        return "Registrator{" +
                "plugin: " + plugin +
                ", enabled: " + enabled +
                ", registrations: " + registrations.size() +
                '}';
    }

    public String toStringWithAllRegistrations() {
        StringBuilder sb = new StringBuilder("Registrator {");
        sb.append("\n  plugin: ").append(plugin);
        sb.append("\n  enabled: ").append(enabled);
        sb.append("\n  registrations: [");

        Iterator<Registration> iterator = registrations.iterator();
        if (iterator.hasNext()) {
            sb.append("\n    ").append(iterator.next().toString());
        }
        while (iterator.hasNext()) {
            sb.append(',').append("\n    ").append(iterator.next().toString());
        }
        if (!registrations.isEmpty()) {
            sb.append("\n  ");
        }
        sb.append("]\n}");
        return sb.toString();
    }

    // ############################################
    // # Public types
    // ############################################

    public static final class Registration extends RegisteredListener {

        private final EventExecutor executor;
        private final Class<?> eventClass;
        private final StackTraceElement caller;
        private boolean registered;

        Registration(Class<?> eventClass, StackTraceElement caller, EventExecutor executor, EventPriority priority, Plugin plugin, boolean ignoreCancelled) {
            super(universalListenerObject, executor, priority, plugin, ignoreCancelled);
            this.executor = executor;
            this.eventClass = eventClass;
            this.caller = caller;
        }

        Registration setPlugin(Plugin plugin) {
            if (getPlugin() == plugin) {
                return this;
            }
            boolean registered = this.registered;
            unregister();
            Registration out = new Registration(eventClass, caller, executor, getPriority(), plugin, isIgnoringCancelled());
            if (registered) {
                out.register();
            }
            return out;
        }

        public Class<?> getEventClass() {
            return eventClass;
        }

        public StackTraceElement getCaller() {
            return caller;
        }

        public boolean isRegistered() {
            return registered;
        }

        void register() {
            if (!registered) {
                registered = true;
                getHandlerListOf(eventClass).register(this);
            }
        }

        void unregister() {
            if (registered) {
                registered = false;
                getHandlerListOf(eventClass).unregister(this);
            }
        }

        @Override
        public String toString() {
            return "Listener for " + eventClass.getSimpleName() + (caller == null ? "" : " registered at " + caller.toString());
        }

    }

    // ############################################
    // # Internal instance methods
    // ############################################

    @SuppressWarnings("UnusedReturnValue")
    private boolean setEnabled(boolean enabled) {
        if (this.enabled != enabled) {
            this.enabled = enabled;
            if (enabled) {
                registerAllListeners();
            } else {
                unregisterAllListeners();
            }
            return true;
        }
        return false;
    }

    private void initPluginListeners() {
        if (hasFakePlugin()) {
            pluginEnableListener = pluginDisableListener = null;
        } else {
            if (pluginEnableListener != null) {
                pluginEnableListener = pluginEnableListener.setPlugin(plugin);
            } else {
                pluginEnableListener = createRegistration(null, EventPriority.NORMAL, false, PluginEnableEvent.class, this::onPluginEnable);
            }
            if (pluginDisableListener != null) {
                pluginDisableListener = pluginDisableListener.setPlugin(plugin);
            } else {
                pluginDisableListener = createRegistration(null, EventPriority.NORMAL, false, PluginDisableEvent.class, this::onPluginDisable);
            }
        }
    }

    private void updatePluginListeners(boolean pluginEnabled) {
        if (hasFakePlugin()) {
            setPluginListenerRegisteredStates(false, false);
        } else {
            setPluginListenerRegisteredStates(!pluginEnabled, pluginEnabled);
        }
    }

    private void setPluginListenerRegisteredStates(boolean enableListenerRegistered, boolean disableListenerRegistered) {
        if (pluginEnableListener != null) {
            if (enableListenerRegistered) {
                PluginEnableEvent.getHandlerList().register(pluginEnableListener);
            } else {
                PluginEnableEvent.getHandlerList().unregister(pluginEnableListener);
            }
        }
        if (pluginDisableListener != null) {
            if (disableListenerRegistered) {
                PluginDisableEvent.getHandlerList().register(pluginDisableListener);
            } else {
                PluginDisableEvent.getHandlerList().unregister(pluginDisableListener);
            }
        }
    }

    private void onPluginEnable(PluginEnableEvent event) {
        if (event.getPlugin() == plugin) {
            setEnabled(true);
            updatePluginListeners(true);
        }
    }

    private void onPluginDisable(PluginDisableEvent event) {
        if (event.getPlugin() == plugin) {
            setEnabled(false);
            updatePluginListeners(false);
        }
    }

    private void setListenersPluginTo(Plugin plugin) {
        List<Registration> registrations = this.registrations;
        for (int n = registrations.size(), i = 0; i < n; i++) {
            registrations.set(i, registrations.get(i).setPlugin(plugin));
        }
    }

    private void registerListener(Registration registration) {
        registrations.add(registration);
        if (enabled) {
            registration.register();
        }
    }

    private <T extends Event> Registration createRegistration(EventPriority priority,
                                                              boolean ignoreCancelled,
                                                              Class<T> eventClass,
                                                              Consumer<? super T> handler) {
        StackTraceElement caller = null;
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        if (stackTrace.length > 0) {
            String className = Registrator.class.getName();
            for (StackTraceElement element : stackTrace) {
                if (!element.getClassName().equals(className) && !element.getClassName().startsWith("java.lang")) {
                    caller = element;
                    break;
                }
            }
        }

        return createRegistration(caller, priority, ignoreCancelled, eventClass, handler);
    }

    private <T extends Event> Registration createRegistration(StackTraceElement caller,
                                                              EventPriority priority,
                                                              boolean ignoreCancelled,
                                                              Class<T> eventClass,
                                                              Consumer<? super T> handler) {
        EventExecutor executor = newEventExecutor(eventClass, handler);
        return new Registration(eventClass, caller, executor, priority, plugin, ignoreCancelled);
    }

    private void registerAllListeners() {
        for (Registration registration : registrations) {
            registration.register();
        }
    }

    private void unregisterAllListeners() {
        for (Registration registration : registrations) {
            registration.unregister();
        }
    }

    @SuppressWarnings("unchecked")
    private Collection<ListenerFieldInfo> getListenerFields(Class<?> clazz, Object instance) {
        Collection<ListenerFieldInfo> rv = new ArrayList<>();

        Field[] fields = clazz.getDeclaredFields();
        boolean isStatic = instance == null;
        if (!isStatic && !clazz.isInstance(instance)) {
            throw new IllegalArgumentException("Instance must be an instance of the given class");
        }

        fieldLoop:
        for (Field f : fields) {
            if (isStatic != Modifier.isStatic(f.getModifiers())
                    || !f.isAnnotationPresent(RegistratorListenerTag.class)) {
                continue;
            }

            if (!IRegistratorListener.class.isAssignableFrom(f.getType())) {
                handleListenerFieldError(new ListenerFieldError(f, "Field type cannot be assigned to IEventListener: " + f.getGenericType().getTypeName()));
                continue;
            }

            Type eventType = null;
            if (f.getType() == IRegistratorListener.class) {

                Type[] typeArgs;
                if (!(f.getGenericType() instanceof ParameterizedType)
                        || (typeArgs = ((ParameterizedType) f.getGenericType()).getActualTypeArguments()).length != 1) {
                    // TODO: if its a TypeVariable, in some cases it might be possible to get the type.
                    // Log a warning or throw an exception
                    handleListenerFieldError(new ListenerFieldError(f, "Failed to recognize event class from field type: " + f.getGenericType().getTypeName()));
                    continue;
                }
                eventType = typeArgs[0];

            } else {
                // field type is subtype of IEventListener.
                // TODO: link type arguments from field declaration (f.getGenericType()) to matching TypeVariables
                Type[] interfaces = f.getType().getGenericInterfaces();
                for (Type itf : interfaces) {
                    Class<?> itfClass;
                    Type[] arguments = null;
                    if (itf instanceof ParameterizedType) {
                        if (!(((ParameterizedType) itf).getRawType() instanceof Class)) {
                            // Should not happen: throw error
                            throw new InternalError("rawType of ParameterizedType expected to be a Class");
                        }
                        itfClass = (Class<?>) ((ParameterizedType) itf).getRawType();
                        arguments = ((ParameterizedType) itf).getActualTypeArguments();
                    } else if (itf instanceof Class<?>) {
                        itfClass = (Class<?>) itf;
                    } else {
                        // TypeVariable? Not sure
                        // Ignore
                        continue;
                    }

                    if (itfClass == IRegistratorListener.class) {
                        if (arguments == null || arguments.length != 1) {
                            // Log a warning or throw an exception
                            handleListenerFieldError(new ListenerFieldError(f, ""));
                            continue fieldLoop;
                        }

                        eventType = arguments[0];
                        break;
                    }
                }

                if (eventType == null) {
                    // Log a warning or throw an exception
                    handleListenerFieldError(new ListenerFieldError(f, "Failed to recognize event class from field type: " + f.getGenericType().getTypeName()));
                    continue;
                }
            }

            if (!(eventType instanceof Class)) {
                if (eventType instanceof ParameterizedType) {
                    Type rawType = ((ParameterizedType) eventType).getRawType();
                    if (!(rawType instanceof Class)) {
                        // Log a warning or throw an exception
                        handleListenerFieldError(new ListenerFieldError(f, "Failed to recognize event class from a Type: " + eventType));
                        continue;
                    }
                    eventType = rawType;
                } else {
                    // Log a warning or throw an exception
                    handleListenerFieldError(new ListenerFieldError(f, "Failed to recognize event class from a Type: " + eventType));
                    continue;
                }
            }

            Consumer<? super Event> lambda;
            try {
                f.setAccessible(true);
                lambda = (Consumer<? super Event>) f.get(instance);
            } catch (IllegalArgumentException | IllegalAccessException | ClassCastException e) {
                // Log a warning or throw an exception
                handleListenerFieldError(new ListenerFieldError(f, e));
                continue;
            }

            Class<? extends Event> baseEventClass = (Class<? extends Event>) eventType;

            RegistratorListenerTag anno = f.getAnnotation(RegistratorListenerTag.class);
            String[] eventClassNames = anno.events();
            if (eventClassNames.length > 0) {

                // The same field might get added here multiple times, to register it with multiple events.
                // This list is used to prevent adding its listener to the same handler list multiple times.
                // Allocation of a table is not necessary at this scale.
                List<HandlerList> handlerLists = new ArrayList<>();

                for (String eventClassName : eventClassNames) {
                    Class<? extends Event> eventClass = getEventClassByName(eventClassName);
                    if (eventClass != null && baseEventClass.isAssignableFrom(eventClass)) {
                        HandlerList handlerList = getHandlerListOf(eventClass);
                        if (handlerList == null) {
                            // multiple warnings could be raised here for the same field
                            handleListenerFieldError(new ListenerFieldError(f, "There is no HandlerList available for the event " + eventClass.getName()));
                            continue;
                        }

                        if (handlerLists.contains(handlerList)) {
                            // Ignore: it will work as intended
                            continue;
                        }

                        handlerLists.add(handlerList);
                        rv.add(new ListenerFieldInfo(eventClass, lambda, anno));
                    } else {
                        // Error: event class string is not recognized or cannot be assigned to the event type
                        // Log a warning or throw an exception
                        String msg = String.format("Event class '%s', resolved to '%s', is unresolved or cannot be assigned to '%s'",
                                eventClassName, eventClass == null ? null : eventClass.getName(), baseEventClass.getName());
                        handleListenerFieldError(new ListenerFieldError(f, msg));
                        // Don't add the field to the result list
                    }
                }

            } else {
                rv.add(new ListenerFieldInfo(baseEventClass, lambda, anno));
            }
        }
        return rv;
    }

    private static void handleListenerFieldError(ListenerFieldError error) {
        // Log a warning or throw an exception. Behaviour can be changed.
        throw error;
    }

    private static Class<? extends Event> getEventClassByName(String name) {
        try {
            //noinspection unchecked
            return (Class<? extends Event>) Class.forName("org.bukkit.event." + name);
        } catch (ClassNotFoundException | ClassCastException e) {
            return null;
        }
    }

    // ############################################
    // # Internal types
    // ############################################

    private static final class HandlerListInfo {
        final HandlerList handlerList;
        // true if and only if the handler list resides in a super class of the event for which it was requested.
        // the filter is needed to filter out event instances not of the requested class.
        // See newEventExecutor(eventClass, handler)
        final boolean requiresFilter;

        HandlerListInfo(HandlerList handlerList, boolean requiresFilter) {
            this.handlerList = handlerList;
            this.requiresFilter = requiresFilter;
        }
    }

    private static final class ListenerFieldInfo {
        final Class<? extends Event> eventClass;
        final Consumer<? super Event> lambda;
        final RegistratorListenerTag anno;

        ListenerFieldInfo(Class<? extends Event> eventClass, Consumer<? super Event> lambda, RegistratorListenerTag anno) {
            this.eventClass = eventClass;
            this.lambda = lambda;
            this.anno = anno;
        }
    }

    /**
     * Error class to report fields that are intended to be listeners with illegal properties
     */
    static final class ListenerFieldError extends Error {
        private Field field;

        public ListenerFieldError(Field field, String message) {
            super(message);
            this.field = field;
        }

        public ListenerFieldError(Field field, Throwable cause) {
            super(cause);
            this.field = field;
        }

        public Field getField() {
            return field;
        }
    }

    /**
     * A fake plugin to be used by registrations made by {@link Registrator} if no plugin is given explicitly
     * This allows libraries that don't have a plugin to register with in the first place to listen for events as well.
     * Instances are not registered with the {@link org.bukkit.plugin.PluginManager}
     *
     * Package private: Only intended for use by {@link Registrator}
     *
     * This class attempts to provide implementations for methods such that
     * plugin checks don't fail because of null pointers being returned.
     */
    private static final class RegistratorPlugin implements Plugin {
        private static final String PREFIX = "Registrator_";
        private final PluginDescriptionFile pdf;

        RegistratorPlugin(String name) {
            pdf = new PluginDescriptionFile(PREFIX + Objects.requireNonNull(name), "0.1", getClass().getName());
        }

        @Override
        public String getName() {
            return pdf.getName();
        }

        @Override
        public File getDataFolder() {
            File file = new File("plugins/Registrators/" + getName().substring(PREFIX.length()));
            file.mkdirs();
            return file;
        }

        @Override
        public PluginDescriptionFile getDescription() {
            return pdf;
        }

        @Override
        public FileConfiguration getConfig() {
            return null;
        }

        @Override
        public InputStream getResource(String s) {
            return getClass().getResourceAsStream(s);
        }

        @Override
        public void saveConfig() {
        }

        @Override
        public void saveDefaultConfig() {
        }

        @Override
        public void saveResource(String s, boolean b) {
        }

        @Override
        public void reloadConfig() {
        }

        @Override
        public PluginLoader getPluginLoader() {
            return null;
        }

        @Override
        public Server getServer() {
            return Bukkit.getServer();
        }

        @Override
        public boolean isEnabled() {
            return true;
        }

        @Override
        public void onDisable() {
        }

        @Override
        public void onLoad() {
        }

        @Override
        public void onEnable() {
        }

        @Override
        public boolean isNaggable() {
            return false;
        }

        @Override
        public void setNaggable(boolean b) {
        }

        @Override
        public ChunkGenerator getDefaultWorldGenerator(String s, String s1) {
            return null;
        }

        @Override
        public Logger getLogger() {
            return Bukkit.getLogger();
        }

        @Override
        public boolean onCommand(org.bukkit.command.CommandSender commandSender, org.bukkit.command.Command command, String s, String[] strings) {
            return false;
        }

        @Override
        public List<String> onTabComplete(org.bukkit.command.CommandSender commandSender, org.bukkit.command.Command command, String s, String[] strings) {
            return null;
        }

    }

}