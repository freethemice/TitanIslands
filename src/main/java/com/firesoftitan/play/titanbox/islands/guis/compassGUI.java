package com.firesoftitan.play.titanbox.islands.guis;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class compassGUI implements Listener {

    private Inventory inv;

    public compassGUI(Player player) {
        // Create inventory with 6 rows
        inv = Bukkit.createInventory(player, 6*9, "Compass Settings");

        // Put items into the inventory
        for (int i = 0; i < 54; i++) {
            inv.setItem(i, new ItemStack(Material.GRAY_STAINED_GLASS_PANE));
        }

        // Add scrollbar on right side
        inv.setItem(45, createScrollBar(0)); // Top arrow
        for (int i = 46; i <= 52; i++) {
            inv.setItem(i, createScrollBar(1)); // Scrollbar contents
        }
        inv.setItem(53, createScrollBar(2)); // Bottom arrow
    }

    // Listener for clicks
    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (e.getInventory().equals(inv)) {
            e.setCancelled(true);
            // Handle clicks
        }
    }

    // Create scroll bar items
    private ItemStack createScrollBar(int type) {
        ItemStack item = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        switch(type) {
            case 0:
                // Item for top arrow
                break;
            case 1:
                // Item for scrollbar contents
                break;
            case 2:
                // Item for bottom arrow
                break;
        }
        return item;
    }

}