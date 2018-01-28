package io.dico.dicore.inventory.multigui;

import io.dico.dicore.Registrator;
import io.dico.dicore.event.ChainedListenerHandle;
import io.dico.dicore.event.ChainedListenerHandles;
import io.dico.dicore.event.ListenerHandle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.bukkit.event.EventPriority.HIGHEST;

public class MultiGuiDriver implements Listener {
    private Map<Inventory, MultiGui> guis = new HashMap<>();
    
    public void addGui(MultiGui gui) {
        guis.put(gui.getInventory(), gui);
    }
    
    public void removeGui(MultiGui gui) {
        guis.remove(gui.getInventory(), gui);
    }
    
    public MultiGuiDriver registerListeners(Registrator registrator) {
        registrator.registerListener(InventoryClickEvent.class, HIGHEST, false, this::onInventoryClick);
        registrator.registerListener(InventoryDragEvent.class, HIGHEST, false, this::onInventoryDrag);
        registrator.registerListener(InventoryCloseEvent.class, HIGHEST, false, this::onInventoryClose);
        registrator.registerListener(InventoryOpenEvent.class, HIGHEST, false, this::onInventoryOpen);
        return this;
    }
    
    public ListenerHandle makeListenerHandle(Registrator registrator) {
        ChainedListenerHandle rv = ChainedListenerHandles.empty();
        rv = rv.withElement(registrator.makeListenerHandle(InventoryClickEvent.class, HIGHEST, false, this::onInventoryClick));
        rv = rv.withElement(registrator.makeListenerHandle(InventoryDragEvent.class, HIGHEST, false, this::onInventoryDrag));
        rv = rv.withElement(registrator.makeListenerHandle(InventoryCloseEvent.class, HIGHEST, false, this::onInventoryClose));
        rv = rv.withElement(registrator.makeListenerHandle(InventoryOpenEvent.class, HIGHEST, false, this::onInventoryOpen));
        return rv;
    }
    
    public void closeAllGuis() {
        for (MultiGui gui : new ArrayList<>(guis.values())) {
            gui.dispose(this);
        }
    }
    
    @EventHandler(priority = HIGHEST)
    private void onInventoryClick(InventoryClickEvent event) {
        Inventory inventory = event.getView().getTopInventory();
        MultiGui gui = guis.get(inventory);
        if (gui != null) {
            gui.onInventoryClick(event);
        }
    }
    
    @EventHandler(priority = HIGHEST)
    private void onInventoryDrag(InventoryDragEvent event) {
        Inventory inventory = event.getView().getTopInventory();
        MultiGui gui = guis.get(inventory);
        if (gui != null) {
            gui.onInventoryDrag(event);
        }
    }
    
    @EventHandler(priority = HIGHEST)
    private void onInventoryClose(InventoryCloseEvent event) {
        Inventory inventory = event.getView().getTopInventory();
        MultiGui gui = guis.get(inventory);
        if (gui != null) {
            gui.onInventoryClose(this, event);
        }
    }
    
    @EventHandler(priority = HIGHEST)
    private void onInventoryOpen(InventoryOpenEvent event) {
        Inventory inventory = event.getInventory();
        MultiGui gui = guis.get(inventory);
        if (gui != null) {
            gui.onInventoryOpen(event);
        }
    }
    
}
