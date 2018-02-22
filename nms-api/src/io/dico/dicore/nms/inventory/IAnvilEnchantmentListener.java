package io.dico.dicore.nms.inventory;

import org.bukkit.inventory.ItemStack;

public interface IAnvilEnchantmentListener {

    ItemStack onEnchantmentsComputed(ItemStack in1, ItemStack in2, ItemStack resultStack);

}
