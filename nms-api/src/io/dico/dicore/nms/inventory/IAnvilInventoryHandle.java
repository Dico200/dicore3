package io.dico.dicore.nms.inventory;

import io.dico.dicore.event.SimpleListener;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

public interface IAnvilInventoryHandle {
    
    HumanEntity getViewer();
    
    InventoryView getView();
    
    ItemStack getLeftInputItem();
    
    ItemStack getRightInputItem();
    
    ItemStack getResultItem();
    
    void setLeftInputItem(ItemStack item);
    
    void setRightInputItem(ItemStack item);
    
    void setResultItem(ItemStack item);
    
    void addCraftMatrixChangeListener(SimpleListener<IAnvilInventoryHandle> listener);
    
    void setEnchantmentListener(IAnvilEnchantmentListener listener);
    
    String getRepairedItemName();
    
    void setRepairedItemName(String repairedItemName);
    
    int getCost();
    
    void setCost(int cost);
    
}
