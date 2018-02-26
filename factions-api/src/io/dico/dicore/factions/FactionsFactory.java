package io.dico.dicore.factions;

import io.dico.dicore.exceptions.ExceptionHandler;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.function.Consumer;

public class FactionsFactory {
    private static final IFactionsDriver driver;
    
    public static IFactionsDriver getDriver() {
        return driver;
    }
    
    public static boolean isDriverPresent() {
        return driver != null;
    }
    
    private static void logError(String description, Throwable error) {
        Consumer<String> target = msg -> Bukkit.getLogger().severe("[Factions API] " + msg);
        ExceptionHandler.log(target, description, error);
    }
    
    static {
        String[] implementations = {"factionsone_1_2_2"};
        
        IFactionsDriver localDriver = null;
        for (String impl : implementations) {
            localDriver = getDriver0(impl);
            if (localDriver != null) {
                break;
            }
        }
        
        if (localDriver == null) {
            localDriver = new DefaultFactionsDriver();
        }
        driver = localDriver;
    }
    
    private static IFactionsDriver getDriver0(String impl) {
        String className = "io.dico.dicore.factions." + impl + ".FactionsDriverImpl";
        
        Class<?> driverClass;
        try {
            driverClass = Thread.currentThread().getContextClassLoader().loadClass(className);
        } catch (ClassNotFoundException e) {
            try {
                driverClass = Class.forName(className);
            } catch (ClassNotFoundException e1) {
                return null;
            }
        }
        
        try {
            Method checkMethod = driverClass.getDeclaredMethod("checkPluginMatch");
            checkMethod.setAccessible(true);
            checkMethod.invoke(null);
        } catch (ReflectiveOperationException ex) {
            return null;
        }
    
        try {
            Constructor<?> constructor = driverClass.getConstructor();
            constructor.setAccessible(true);
            return (IFactionsDriver) constructor.newInstance();
        } catch (ReflectiveOperationException | ClassCastException ex) {
            return null;
        }
    }
    
    private static final class DefaultFactionsDriver implements IFactionsDriver {
        
        @Override
        public IFactionsPlayer getFactionsPlayer(Player player) {
            return new IFactionsPlayer() {
                @Override
                public Player getPlayer() {
                    return player;
                }
                
                @Override
                public Relation getRelationTo(IFactionsPlayer otherPlayer) {
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
