package io.dico.dicore.inventory.gui;

import io.dico.dicore.Registrator;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.InventoryView;

import java.util.HashMap;
import java.util.Map;

public class GuiDriver {
    private Map<InventoryView, Gui> openGuis = new HashMap<>();
    
    public GuiDriver(Registrator registrator) {
        registrator.registerListener(InventoryCloseEvent.class, EventPriority.HIGHEST, false, this::onInventoryClose);
        registrator.registerListener(InventoryClickEvent.class, EventPriority.HIGHEST, false, this::onInventoryClick);
        //registrator.registerListener(InventoryDragEvent.class, EventPriority.HIGHEST, false, this::onInventoryDrag);
    }
    
    public void guiOpened(Gui gui) {
        openGuis.put(gui.getView(), gui);
    }

    private void onInventoryClose(InventoryCloseEvent event) {
        Gui gui = openGuis.get(event.getView());
        if (gui != null) {
            gui.closed();
        }
    }
    
    public void guiClosed(Gui gui) {
        openGuis.remove(gui.getView(), gui);
    }

    private void onInventoryClick(InventoryClickEvent event) {
        Gui gui = openGuis.get(event.getView());
        if (gui != null) {
            gui.onInventoryClick(event, event.getRawSlot());
        }
    }
    
    private void onInventoryDrag(InventoryDragEvent event) {
    
    }
    
}
