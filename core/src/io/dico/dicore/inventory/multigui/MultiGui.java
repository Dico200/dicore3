package io.dico.dicore.inventory.multigui;

import io.dico.dicore.InventoryEventUtil;
import io.dico.dicore.SpigotUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.Map;

import static org.bukkit.event.Event.Result.DENY;

public class MultiGui {
    protected static final boolean Deny = false;
    protected static final boolean Allow = true;
    protected final Inventory inventory;
    protected boolean computeNewItem = false;
    private boolean disposingOnClose = true;
    protected int maxViewers = Integer.MAX_VALUE;
    
    protected MultiGui() {
        this.inventory = createInventory();
        refreshItems(inventory);
    }
    
    protected MultiGui(Inventory inventory) {
        this.inventory = inventory;
    }
    
    protected MultiGui(MultiGuiDriver driver) {
        this();
        driver.addGui(this);
    }
    
    protected MultiGui(MultiGuiDriver driver, Inventory inventory) {
        this(inventory);
        driver.addGui(this);
    }
    
    public final Inventory getInventory() {
        return inventory;
    }
    
    public final void open(HumanEntity entity) {
        entity.openInventory(inventory);
    }
    
    public final void refreshItems() {
        refreshItems(inventory);
    }
    
    public final boolean isDisposingOnClose() {
        return disposingOnClose;
    }
    
    public final void setDisposingOnClose(boolean disposingOnClose) {
        this.disposingOnClose = disposingOnClose;
    }
    
    public final boolean hasViewers() {
        return !inventory.getViewers().isEmpty();
    }
    
    public final void closeAllViews() {
        closeAllViews(null);
    }
    
    public final void dispose(MultiGuiDriver driver) {
        dispose(driver, null);
    }
    
    public final void updateInventory() {
        for (HumanEntity viewer : inventory.getViewers()) {
            if (viewer instanceof Player) {
                ((Player) viewer).updateInventory();
            }
        }
    }
    
    private boolean onSlotChange(InventoryInteractEvent event, int slot, ItemStack oldItem, ItemStack newItem) {
        if (SpigotUtil.isItemPresent(oldItem) ? oldItem.equals(newItem) : !SpigotUtil.isItemPresent(newItem)) {
            return Allow;
        }
        if (!allowSlotChange(event, slot, oldItem, newItem)) {
            event.setCancelled(true);
            return Deny;
        }
        return Allow;
    }
    
    private void closeAllViews(HumanEntity closer) {
        Collection<HumanEntity> viewers = inventory.getViewers();
        for (HumanEntity viewer : viewers.toArray(new HumanEntity[viewers.size()])) {
            if (viewer != closer) {
                viewer.closeInventory();
            }
        }
    }
    
    private void dispose(MultiGuiDriver driver, HumanEntity closer) {
        onDispose();
        driver.removeGui(this);
        closeAllViews(closer);
    }
    
    protected Inventory createInventory() {
        return Bukkit.createInventory(null, 9);
    }
    
    protected void refreshItems(Inventory inventory) {
    
    }
    
    protected void onInventoryOpen(InventoryOpenEvent event) {
        if (inventory.getViewers().size() > maxViewers) {
            event.setCancelled(true);
        }
    }
    
    protected void onInventoryClose(MultiGuiDriver driver, InventoryCloseEvent event) {
        if (disposingOnClose && inventory.getViewers().size() <= 1) {
            dispose(driver, event.getPlayer());
        }
    }
    
    protected void onInventoryClick(InventoryClickEvent event) {
        if (event.getResult() == DENY) {
            return;
        }
        
        Inventory clicked = event.getClickedInventory();
        if (clicked != event.getView().getTopInventory()) {
            // they clicked in their own inventory
            
            if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
                ItemStack added = event.getCurrentItem();
                if (!SpigotUtil.isItemPresent(added)) {
                    return;
                }
                
                Inventory inventory = this.inventory;
                Map<Integer, ItemStack> changes = InventoryEventUtil.deduceChangesIfItemAdded(getInventory(), added, computeNewItem);
                for (Map.Entry<Integer, ItemStack> entry : changes.entrySet()) {
                    if (!onSlotChange(event, entry.getKey(), inventory.getItem(entry.getKey()), entry.getValue())) {
                        break;
                    }
                }
            }
        } else {
            // they clicked in the gui
            
            onSlotChange(event, event.getSlot(), clicked.getItem(event.getSlot()), computeNewItem ? InventoryEventUtil.getNewItem(event) : null);
        }
    }
    
    protected void onInventoryDrag(InventoryDragEvent event) {
        Inventory inventory = this.inventory;
        Map<Integer, ItemStack> newItems = event.getNewItems();
        InventoryView view = event.getView();
        for (Map.Entry<Integer, ItemStack> entry : newItems.entrySet()) {
            int rawSlot = entry.getKey();
            int slot = view.convertSlot(rawSlot);
            if (slot != rawSlot) {
                // change in the bottom inventory
                continue;
            }
            if (!onSlotChange(event, slot, inventory.getItem(slot), entry.getValue())) {
                break;
            }
        }
    }
    
    protected boolean allowSlotChange(InventoryInteractEvent event, int slot, ItemStack oldItem, ItemStack newItem) {
        return Allow;
    }
    
    protected void onDispose() {
    
    }

}
