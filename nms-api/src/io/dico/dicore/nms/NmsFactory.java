package io.dico.dicore.nms;

import io.dico.dicore.Version;
import io.dico.dicore.exceptions.ExceptionHandler;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.lang.reflect.Constructor;
import java.util.function.Consumer;

public final class NmsFactory {
    private static final String NMS_DRIVER_CLASS = "io.dico.dicore.nms.%s.NmsDriver";
    private static final INmsDriver driver;
    private static final String nmsPackage;
    private static final Version nmsVersion;
    private static final Version apiVersion;
    
    private NmsFactory() {
        
    }
    
    private static void logError(String description, Throwable error) {
        Consumer<String> target = msg -> Bukkit.getLogger().severe("[Nms API] " + msg);
        ExceptionHandler.log(target, description, error);
    }
    
    static {
        /*
        Static initialization should never throw an error
        This would cause the error to propagate to the code that first uses the driver,
        causing arbitrary parts of the server code to fail and other's to get null.
        If an exception occurs during initialization, this is instead printed to the console.
         */
        apiVersion = new Version(2, 1);
        
        // get the nms package from the server
        String[] split = Bukkit.getServer().getClass().getPackage().getName().split("\\.");
        nmsPackage = split[split.length - 1];
        
        // parse the package into a version
        String nmsVersionString = nmsPackage.replace('_', '.').replaceAll("[a-zA-Z]+", "");
        Version localNmsVersion;
        try {
            localNmsVersion = Version.parseVersion(nmsVersionString);
        } catch (Exception ex) {
            localNmsVersion = null;
            logError("parsing a Version object for the current minecraft version", ex);
        }
        nmsVersion = localNmsVersion;
        
        // instantiate the driver
        INmsDriver localDriver;
        try {
            String driverClassName = String.format(NMS_DRIVER_CLASS, nmsPackage);
            Class<?> driverClass = Class.forName(driverClassName);
            // get no-arg constructor explicitly, because direct newInstance() requires the driver to be public
            Constructor<?> constructor = driverClass.getConstructor();
            constructor.setAccessible(true);
            localDriver = (INmsDriver) constructor.newInstance();
        } catch (Exception ex) {
            localDriver = null;
            logError("reflectively instantiating the Nms Driver for version " + nmsPackage, ex);
        }
        
        driver = localDriver;
    }
    
    /**
     * Get the package used by the implementation of {@link org.bukkit.Server}
     * Example: v1_11_R1
     *
     * @return the package used by the implementation of the server
     */
    public static String getNmsPackage() {
        return nmsPackage;
    }
    
    /**
     * Get a Version object representing the nms package.
     * This represents the package name without any of the letters,
     * and any _ replaced with a .
     * <p>
     * As such, if the nms package is v1_11_R2,
     * the major would be 1
     * the minor would be 11
     * and the build would be 2
     *
     * @return the nms package as a version
     */
    public static Version getNmsVersion() {
        return nmsVersion;
    }
    
    /**
     * Get the version of the nms API
     *
     * @return the version of the nms API
     */
    public static Version getApiVersion() {
        return apiVersion;
    }
    
    /**
     * Check if a driver is present
     * <p>
     * This can be used by plugins that depend on the Nms API, to disable before initializing.
     *
     * @return true if and only if a driver is present
     */
    public static boolean isDriverPresent() {
        return driver != null;
    }
    
    /**
     * Get the INmsDriver implementation
     *
     * @return the driver
     */
    public static INmsDriver getDriver() {
        return driver;
    }
    
    /**
     * Get the entity driver.
     * This is a shortcut for {@code NmsFactory.getDriver().getEntityDriver()}.
     *
     * @return the entity driver
     */
    public static IEntityDriver getEntityDriver() {
        return driver.getEntityDriver();
    }
    
    /**
     * Get the item driver.
     * This is a shortcut for {@code NmsFactory.getDriver().getItemDriver()}.
     *
     * @return the item driver
     */
    public static IItemDriver getItemDriver() {
        return driver.getItemDriver();
    }
    
    /**
     * Gets the {@link IWorldDriver} for the given world
     * <p>
     * <i>An NWorld supplies world-specific data and methods</i>
     *
     * @param world The world
     * @return The {@link IWorldDriver} implementation for world
     * @see INmsDriver#getWorldDriver(World)
     */
    static IWorldDriver getWorldDriver(World world) {
        return driver.getWorldDriver(world);
    }
    
}
