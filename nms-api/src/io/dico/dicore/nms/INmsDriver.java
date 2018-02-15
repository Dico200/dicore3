package io.dico.dicore.nms;

import io.dico.dicore.nbt.INbtList;
import io.dico.dicore.nbt.INbtMap;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

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
    void sendSoundPacket(Player player, Sound sound, float volume, float pitch);
    
    /**
     * Send a sound packet with custom location data to only one player
     */
    void sendSoundPacket(Player player, Sound sound, Location loc, float volume, float pitch);
    
    /**
     * Check if the chunk is loaded
     *
     * @param world world of the chunk
     * @param cx x coordinate of the chunk (floor(blockx/16))
     * @param cz z coordinate of the chunk (floor(blockz/16))
     * @return true if the chunk is loaded
     */
    boolean isChunkLoaded(World world, int cx, int cz);
    
    /**
     * Check if the block's chunk is loaded
     *
     * @param block the block
     * @return true if the chunk is loaded
     */
    boolean isChunkLoaded(Block block);
    
    /**
     * Actually drop the item naturally
     *
     * @param block the location
     * @param item the item
     * @return the entity representing the item drop
     */
    Item dropItemNaturally(Block block, ItemStack item);
    
}
