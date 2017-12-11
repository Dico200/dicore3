package io.dico.dicore.nms;

import io.dico.dicore.nbt.INbtList;
import io.dico.dicore.nbt.INbtMap;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;

/**
 * The nms driver
 *
 * An instance of this interface can be retrieved from {@link NmsFactory#getDriver()}
 */
public interface INmsDriver {
    
    /**
     * @return The current server tick
     * @implNote this references the MinecraftServer.currentTick field
     */
    int getServerTick();
    
    /**
     * Gets the {@link IWorldDriver} for the given world
     * <p>
     * <i>An NWorld supplies world-specific data and methods</i>
     *
     * @param world The world
     * @return The {@link IWorldDriver} implementation for world
     */
    IWorldDriver getWorldDriver(World world);

    /**
     * @return The {@link IEntityDriver} implementation
     */
    IEntityDriver getEntityDriver();

    /**
     * @return The {@link IItemDriver} implementation
     */
    IItemDriver getItemDriver();
    
    /**
     * Wraps a new nms NBT Tag Compound
     *
     * @return A new nbt map with a wrapper implementation
     */
    INbtMap newWrappedNBTMap();
    
    /**
     * Wraps a new nms NBT Tag List
     *
     * @return A new nbt list with a wrapper implementation
     */
    INbtList newWrappedNBTList();
    
    // -------- Less important -------------
    
    /**
     * @return The nutritional value of the given material
     */
    int getNutritionalValue(Material material);
    
    /**
     * Send a sound packet efficiently to only one player
     */
    public void sendSoundPacket(Player player, Sound sound, float volume, float pitch);/* {
        EntityPlayer p = ((CraftPlayer) player).getHandle();
        p.playerConnection.sendPacket(new PacketPlayOutNamedSoundEffect(CraftSound.getSound(sound), p.locX, p.locY, p.locZ, volume, pitch));
    }*/
    
    /**
     * Send a sound packet with custom location data to only one player
     */
    public void sendSoundPacket(Player player, Sound sound, Location loc, float volume, float pitch);/* {
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(new PacketPlayOutNamedSoundEffect(CraftSound.getSound(sound), loc.getX(), loc.getY(), loc.getZ(), volume, pitch));
    }*/
    
}
