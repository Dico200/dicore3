package io.dico.dicore.nms;

import io.dico.dicore.nms.inventory.IAnvilInventoryHandle;
import org.bukkit.entity.Player;

public interface IInventoryDriver {
    
    /**
     * Get an anvil inventory handle for the given player.
     * If the view is not a view over an anvil inventory, {@code null} is returned.
     *
     * @param player the player
     * @return an anvil inventory handle for the player.
     */
    IAnvilInventoryHandle getAnvilInventoryHandle(Player player);
    
}
