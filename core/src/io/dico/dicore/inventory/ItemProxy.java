package io.dico.dicore.inventory;

import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public abstract class ItemProxy {
    protected ItemStack item;
    
    private static ItemStack air() {
        return new ItemStack(Material.AIR);
    }
    
    public static ItemProxy forInventorySlot(Inventory inventory, int slot) {
        return new ItemProxy() {
            @Override
            public void set(ItemStack item) {
                inventory.setItem(slot, item);
            }
            
            @Override
            public ItemStack get() {
                return inventory.getItem(slot);
            }
        };
    }
    
    public ItemStack getItem() {
        return item;
    }
    
    public void setItem(ItemStack item) {
        this.item = item == null ? air() : item;
    }
    
    protected abstract void set(ItemStack item);
    
    protected abstract ItemStack get();
    
    public ItemStack pull() {
        setItem(get());
        return item;
    }
    
    public void push() {
        set(item);
    }
    
    public void push(ItemStack item) {
        setItem(item);
        push();
    }
    
    public boolean wasModified() {
        ItemStack current = item;
        return !current.equals(pull());
    }
    
    public void add(int amt) {
        item.setAmount(item.getAmount() + amt);
        if (item.getAmount() <= 0) {
            item = air();
        }
    }
    
}
