package io.dico.dicore.nms;

import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

/**
 * This interface contains methods that query and modify {@link org.bukkit.entity.Entity entities}
 * <p>
 * There are methods that get and set pieces of equipment. These methods query and modify entities too, not items.
 * <p>
 * An instance of this interface can be retrieved from the shortcut {@link NmsFactory#getEntityDriver()}
 */
public interface IEntityDriver {
    
    /**
     * Checks if an entity is in water
     *
     * @param entity the entity
     * @return true if entity is in water
     * @throws NullPointerException if entity is null
     */
    boolean isInWater(Entity entity);
    
    /**
     * Checks if an entity is considered to be in a block of the given material
     *
     * @param entity the entity
     * @return true if entity is considered to be in a block of the given material
     * @throws NullPointerException if entity is null
     * @implNote use {@code entity.world.a(entity.getBoundingBox().grow({minecraft's constants}), material)}
     */
    boolean isInOtherBlock(Entity entity, Material type);
    
    /**
     * Gets the time at which a player last performed an action
     * This timestamp is retrieved from {@link System#currentTimeMillis()},
     * as such it can be compared to current time using that method.
     *
     * @param player The player
     * @return The time at which the player last performed an action
     * @throws NullPointerException if player is null
     */
    long getTimeOfLastAction(Player player);
    
    /**
     * Get the actual item at the given equipment slot. Bukkit's implementation returns an ItemStack instance, discarding any non-itemmeta nbt data.
     *
     * @param entity        the entity whose item to get
     * @param equipmentSlot the equipment slot
     * @return a CraftItemStack mirror of the item, if present. If not present, this returns an item of type AIR.
     * @throws NullPointerException if either argument is null
     */
    ItemStack getEquipmentItem(LivingEntity entity, EquipmentSlot equipmentSlot);
    
    // -------------- Less important ------------------
    
    /**
     * Make the server believe the given monster attacked the entity
     *
     * @return true? idk
     */
    boolean commenceMobAttack(Monster bukkitMonster, Entity bukkitTarget);
    
    /**
     * Make the server believe the given player attacked the entity
     */
    void commencePlayerAttack(Player bukkitPlayer, Entity bukkitTarget);
    
    /**
     * Ignite a creeper such that it blows up
     *
     * @param creeper the creeper
     */
    void igniteCreeper(Creeper creeper);
    
    /**
     * @param entity a living entity
     * @return true if the entity's AI has been disabled
     */
    boolean isAiDisabled(LivingEntity entity);
    
    /**
     * @param entity a living entity
     * @param disabled true if the entity's AI should be disabled, false if it should be enabled
     */
    void setAiDisabled(LivingEntity entity, boolean disabled);
    
    
    
}
