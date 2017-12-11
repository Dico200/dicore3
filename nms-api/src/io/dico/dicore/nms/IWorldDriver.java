package io.dico.dicore.nms;

import org.bukkit.World;
import org.bukkit.entity.Entity;

import java.util.Map;
import java.util.UUID;

/**
 * An nms adapter for minecraft worlds.
 *
 * An instance of this interface can be retrieved with {@link NmsFactory#getWorldDriver(World)}
 */
public interface IWorldDriver {

    /**
     * @return The {@link World world} associated
     */
    World getBukkitWorld();

    /**
     * @param uuid the uuid of the target entity
     * @return The entity with the given uuid
     * @apiNote bukkit does not supply an indexed entity lookup.
     */
    Entity getEntityByUUID(UUID uuid);

    /**
     * Gets a collection of all entities in the world indexed by their UUID
     *
     * @return A map of UUID-Entity pairs
     * @apiNote Keep in mind that the map is converting {@link Entity} to minecraft's native Entity class and vice-versa constantly
     */
    Map<UUID, Entity> getEntities();

}
