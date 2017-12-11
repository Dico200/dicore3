package io.dico.dicore;

import org.bukkit.Server;
import org.bukkit.event.*;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredListener;

import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Consumer;

/**
 * This class acts as a utility to register event listeners in a functional manner.
 * Listeners passed are always {@code <T> Consumer<T extends Event>} objects.
 * <p>
 * Registrations are made using its
 * * {@link #registerListener(Class, Consumer)}
 * * {@link #registerListener(Class, EventPriority, Consumer)}
 * * {@link #registerListener(Class, boolean, Consumer)}
 * * {@link #registerListener(Class, EventPriority, boolean, Consumer)}
 * methods.
 * <p>
 * Listeners registered in this way are generally a bit faster than when registered through {@link org.bukkit.plugin.PluginManager#registerEvents(Listener, Plugin)}
 * Because it does not use reflection to call the event handlers.
 *
 * @implNote This class uses only one {@link Listener listener object} across all its instances, by fooling spigot into
 * thinking they're all distinct ones (in other words, by violating the {@link Object#equals(Object)} contract).
 * <p>
 * Standard Registrator instances also use a fake plugin identity to register its listeners.
 * You can use the {{@link #Registrator(Plugin)}} constructor to use real plugin identities.
 */
public final class Registrator {
    private static final Registrator instance;
    private static final Listener universalListenerObject;
    private static final Plugin defaultFakePlugin;
    private static final Map<Class<?>, HandlerListInfo> handlerListCache;
    
    static {
        handlerListCache = new IdentityHashMap<>();
        defaultFakePlugin = new RegistratorPlugin();
        instance = new Registrator();
        universalListenerObject = new Listener() {
            @Override
            public boolean equals(Object obj) {
                /* return false here to fool the HandlerList into believing each registration is from another Listener.
                 * as a result, no exceptions will be thrown when registering multiple listeners for the same event and priority.
                 *
                 * Another option is to have this for each instance:
                 * <p>
                 *
                 *      private Listener getListenerFor(HandlerList list, EventPriority priority) {
                            int needed = (int) (listeners.get(list).stream().filter(listener -> listener.getPriority() == priority).count() + 1);
                            while (needed > myListeners.size()) {
                                myListeners.add(new Listener() {});
                            }
                            return myListeners.get(needed - 1);
                        }
                    <p>
                    Where {@code myListeners} is a List<Listener>
    }
                 *
                 */
                return false;
            }
        };
    }
    
    private final List<Registration> registrations;
    private Plugin plugin;
    private Registration pluginEnableListener;
    private Registration pluginDisableListener;
    private boolean enabled;
    
    /**
     * Constructs a new instance using the {@link #defaultFakePlugin universal plugin object}
     */
    public Registrator() {
        this(false);
    }
    
    /**
     * Constructs a new instance using an artificial plugin.
     *
     * @param distinctPlugin true if the artificial plugin should be distinct from the {@link #defaultFakePlugin universal plugin object}
     */
    public Registrator(boolean distinctPlugin) {
        this(distinctPlugin ? new RegistratorPlugin() : defaultFakePlugin);
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
    
    public static Registrator getInstance() {
        return instance;
    }
    
    /**
     * static {@link EventExecutor} instantiator to make sure executors don't reference any unneeded objects.
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
    
    private static HandlerListInfo getHandlerListInfoOf(Class<?> eventClass) {
        return handlerListCache.computeIfAbsent(eventClass, clz -> {
            Method method = Reflection.deepSearchMethod(clz, "getHandlerList");
            boolean requiresFilter = clz != method.getDeclaringClass();
            return new HandlerListInfo(Reflection.invokeStaticMethod(method), requiresFilter);
        });
    }
    
    /**
     * Reflectively acquire the HandlerList for the given event type.
     *
     * @param eventClass the Event type
     * @return its HandlerList
     * @throws IllegalStateException if the HandlerList can't be found
     */
    private static HandlerList getHandlerListOf(Class<?> eventClass) {
        return getHandlerListInfoOf(eventClass).handlerList;
    }
    
    @SuppressWarnings("UnusedReturnValue")
    private boolean setEnabled(boolean enabled) {
        if (this.enabled != enabled) {
            this.enabled = enabled;
            if (enabled) {
                registerAll();
            } else {
                unregisterAll();
            }
            return true;
        }
        return false;
    }
    
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
    
    private void initPluginListeners() {
        if (hasFakePlugin()) {
            pluginEnableListener = pluginDisableListener = null;
        } else {
            if (pluginEnableListener != null) {
                pluginEnableListener = pluginEnableListener.setPlugin(plugin);
            } else {
                pluginEnableListener = createRegistration(false, EventPriority.NORMAL, false, PluginEnableEvent.class, this::onPluginEnable);
            }
            if (pluginDisableListener != null) {
                pluginDisableListener = pluginDisableListener.setPlugin(plugin);
            } else {
                pluginDisableListener = createRegistration(false, EventPriority.NORMAL, false, PluginDisableEvent.class, this::onPluginDisable);
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
    
    private <T extends Event> Registration createRegistration(boolean findCaller, EventPriority priority, boolean ignoreCancelled, Class<T> eventClass, Consumer<? super T> handler) {
        StackTraceElement caller = null;
        if (findCaller) {
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            if (stackTrace.length > 0) {
                String className = Registrator.class.getName();
                for (StackTraceElement element : stackTrace) {
                    if (!element.getClassName().equals(className)) {
                        caller = element;
                        break;
                    }
                }
            }
        }
        
        EventExecutor executor = newEventExecutor(eventClass, handler);
        return new Registration(eventClass, caller, executor, priority, plugin, ignoreCancelled);
    }
    
    private void registerAll() {
        for (Registration registration : registrations) {
            registration.register();
        }
    }
    
    private void unregisterAll() {
        for (Registration registration : registrations) {
            registration.unregister();
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
        Registration listener = createRegistration(true, priority, ignoreCancelled, eventClass, handler);
        registrations.add(listener);
        
        if (enabled) {
            listener.register();
        }
        return this;
    }
    
    public Registrator onPlayerQuit(Consumer<? super PlayerEvent> handler) {
        registerListener(PlayerQuitEvent.class, EventPriority.NORMAL, handler);
        return registerListener(PlayerKickEvent.class, EventPriority.NORMAL, handler);
    }
    
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
    
    private static class RegistratorPlugin implements Plugin {
        @Override
        public java.io.File getDataFolder() {
            return null;
        }
        
        @Override
        public org.bukkit.plugin.PluginDescriptionFile getDescription() {
            return null;
        }
        
        @Override
        public org.bukkit.configuration.file.FileConfiguration getConfig() {
            return null;
        }
        
        @Override
        public java.io.InputStream getResource(String s) {
            return null;
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
        public org.bukkit.plugin.PluginLoader getPluginLoader() {
            return null;
        }
        
        @Override
        public Server getServer() {
            return null;
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
        public com.avaje.ebean.EbeanServer getDatabase() {
            return null;
        }
        
        @Override
        public org.bukkit.generator.ChunkGenerator getDefaultWorldGenerator(String s, String s1) {
            return null;
        }
        
        @Override
        public java.util.logging.Logger getLogger() {
            return null;
        }
        
        @Override
        public String getName() {
            return null;
        }
        
        @Override
        public boolean onCommand(org.bukkit.command.CommandSender commandSender, org.bukkit.command.Command command, String s, String[] strings) {
            return false;
        }
        
        @Override
        public List<String> onTabComplete(org.bukkit.command.CommandSender commandSender, org.bukkit.command.Command command, String s, String[] strings) {
            return null;
        }
        
        @Override
        public boolean equals(Object obj) {
            return this == obj;
        }
    }
}