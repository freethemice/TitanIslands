package com.firesoftitan.play.titanbox.islands.listeners;

import com.firesoftitan.play.titanbox.islands.TitanIslands;
import com.firesoftitan.play.titanbox.islands.guis.IslandTopGui;
import com.firesoftitan.play.titanbox.islands.managers.LangManager;
import com.firesoftitan.play.titanbox.islands.managers.PlayerManager;
import com.firesoftitan.play.titanbox.islands.runnables.CompassRunnable;
import org.bukkit.Location;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;

import static com.firesoftitan.play.titanbox.islands.TitanIslands.instance;

public class IslandTopGUIListener implements Listener
{

    public IslandTopGUIListener() {

    }
    public void registerEvents(){
        PluginManager pm = instance.getServer().getPluginManager();
        pm.registerEvents(this, instance);
    }
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked().getOpenInventory().getTitle().equals(IslandTopGui.guiName)) {
            event.setCancelled(true);
            HumanEntity whoClicked = event.getWhoClicked();
            IslandTopGui gui = IslandTopGui.getGui((Player) whoClicked);
            if (gui != null)
            {
                Player player = (Player)whoClicked;
                if (event.getSlot() > -1 && event.getSlot() < gui.getSize()) {
                    ItemStack clicked = event.getInventory().getItem(event.getSlot());
                    if (!TitanIslands.tools.getItemStackTool().isEmpty(clicked)) {
                        if (TitanIslands.tools.getNBTTool().containsKey(clicked, "buttonaction")) {
                            String action = TitanIslands.tools.getNBTTool().getString(clicked, "buttonaction");
                            if (action != null && action.length() > 1) {
                                Location compassTarget = null;
                                if (action.equalsIgnoreCase("location")) {
                                    Location location = TitanIslands.tools.getNBTTool().getLocation(clicked, "location");
                                    compassTarget = location.clone();
                                }
                                if (compassTarget != null)
                                {
                                    if (!CompassRunnable.instance.hasCompass(player)) CompassRunnable.instance.add(player, PlayerManager.getHome(player));
                                    CompassRunnable.instance.changeLocation(player, compassTarget);
                                    player.sendMessage(LangManager.instants.getMessage("compass_set") + compassTarget.getBlockX() + ", " + compassTarget.getBlockZ());
                                    player.closeInventory();
                                }
                                else
                                {
                                    player.sendMessage(LangManager.instants.getMessage("error.no_structure"));
                                    player.closeInventory();
                                }

                            }
                        }
                    }
                }
            }
        }
    }
}
