package io.dico.dicore.inventory.gui;

import org.bukkit.inventory.ItemStack;

public interface PassableGui extends Gui {

    void pass(ItemStack item);

}
