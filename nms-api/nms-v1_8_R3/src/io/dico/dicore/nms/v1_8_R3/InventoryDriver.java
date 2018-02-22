package io.dico.dicore.nms.v1_8_R3;

import io.dico.dicore.nms.IInventoryDriver;
import io.dico.dicore.nms.inventory.IAnvilInventoryHandle;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

final class InventoryDriver implements IInventoryDriver {
    
    @Override
    public IAnvilInventoryHandle getAnvilInventoryHandle(Player player) {
        return CustomAnvilContainer.replaceExisting(((CraftPlayer) player).getHandle());
    }
    
}
