package io.dico.dicore.factions;

import io.dico.dicore.exceptions.ExceptionHandler;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Constructor;
import java.util.function.Consumer;

public class FactionsFactory {
    private static final String factionsVersion;
    private static final String driverVersion;
    private static final FactionsDriver driver;

    public static FactionsDriver getDriver() {
        return driver;
    }
    
    public static boolean isDriverPresent() {
        return driver != null;
    }
    
    public static String getDriverVersion() {
        return driverVersion;
    }
    
    public static String getFactionsVersion() {
        return factionsVersion;
    }
    
    static {
        factionsVersion = getFactionsVersion0();
        driverVersion = getDriverVersion0(factionsVersion);
        driver = getDriver0(driverVersion);
    }
    
    private static String getFactionsVersion0() {
        Plugin factions = Bukkit.getServer().getPluginManager().getPlugin("Factions");
        return factions == null ? "none" : factions.getDescription().getVersion();
    }
    
    private static String getDriverVersion0(String factionsVersion) {
        if (factionsVersion.startsWith("1.6")) {
            return "1_6";
        }
        
        return "none";
    }
    
    private static FactionsDriver getDriver0(String driverVersion) {
        if ("none".equals(driverVersion)) {
            return new DefaultFactionsDriver();
        }
        
        String className = "io.dico.dicore.factions." + driverVersion + ".FactionsDriverImpl";
        
        try {
            Class<?> driverClass = Class.forName(className);
            Constructor<?> constructor = driverClass.getConstructor();
            constructor.setAccessible(true);
            return (FactionsDriver) constructor.newInstance();
        } catch (ReflectiveOperationException | ClassCastException ex) {

            ex.printStackTrace();
        }
        
        return new DefaultFactionsDriver();
    }
    
    private static void logError(String description, Throwable error) {
        Consumer<String> target = msg -> Bukkit.getLogger().severe("[Factions API] " + msg);
        ExceptionHandler.log(target, description, error);
    }
    
    private static final class DefaultFactionsDriver implements FactionsDriver {
    
        @Override
        public FactionsPlayer getFactionsPlayer(Player player) {
            return new FactionsPlayer() {
                @Override
                public Player getPlayer() {
                    return player;
                }
            
                @Override
                public Relation getRelationTo(FactionsPlayer otherPlayer) {
                    return Relation.NEUTRAL;
                }
            
                @Override
                public Relation getRelationToTerritory(World world, int chunkX, int chunkZ) {
                    return Relation.NEUTRAL;
                }
            };
        }
    
        @Override
        public Relation getRelationBetween(Player player1, Player player2) {
            return Relation.NEUTRAL;
        }
        
    }

}
