package com.firesoftitan.play.titanbox.islands.listeners;

import com.firesoftitan.play.titanbox.islands.TitanIslands;
import com.firesoftitan.play.titanbox.islands.guis.CompassGui;
import com.firesoftitan.play.titanbox.islands.managers.CubeManager;
import com.firesoftitan.play.titanbox.islands.managers.IslandManager;
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

public class CompassGUIListener  implements Listener
{

    public CompassGUIListener() {

    }
    public void registerEvents(){
        PluginManager pm = instance.getServer().getPluginManager();
        pm.registerEvents(this, instance);
    }
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked().getOpenInventory().getTitle().equals(CompassGui.guiName)) {
            event.setCancelled(true);
            HumanEntity whoClicked = event.getWhoClicked();
            CompassGui gui = CompassGui.getGui((Player) whoClicked);
            if (gui != null)
            {
                Player player = (Player)whoClicked;
                ItemStack clicked = event.getInventory().getItem(event.getSlot());
                if (event.getSlot() > -1 && event.getSlot() < gui.getSize()) {
                    if (!TitanIslands.tools.getItemStackTool().isEmpty(clicked)) {
                        if (TitanIslands.tools.getNBTTool().containsKey(clicked, "buttonaction")) {
                            String action = TitanIslands.tools.getNBTTool().getString(clicked, "buttonaction");
                            if (action != null && action.length() > 1) {
                                Location compassTargert = null;
                                switch (action.toLowerCase()) {
                                    case "none" -> {
                                        CompassRunnable.instance.remove(player);
                                        return;
                                    }
                                    case "home" -> compassTargert = PlayerManager.instants.getHome(player);
                                    case "closest" -> {
                                        IslandManager closestExcluding = IslandManager.getClosestExcluding(player.getLocation(), player);
                                        if (closestExcluding != null) {
                                            compassTargert = closestExcluding.getLocation();
                                        }
                                    }
                                    case "newest" -> {
                                        IslandManager earliest = IslandManager.getNewest(player.getLocation());
                                        if (earliest != null) {
                                            compassTargert = earliest.getLocation();
                                        }
                                    }
                                    case "random" -> {
                                        IslandManager randomExcluding = IslandManager.getRandomExcluding(player);
                                        if (randomExcluding != null) {
                                            compassTargert = randomExcluding.getLocation();
                                        }
                                    }
                                }
                                if (compassTargert != null)
                                {
                                    if (!CompassRunnable.instance.hasCompass(player)) CompassRunnable.instance.add(player, PlayerManager.instants.getHome(player));
                                    CompassRunnable.instance.changeLocation(player, compassTargert);
                                    player.sendMessage(LangManager.instants.getMessage("done"));
                                    player.closeInventory();
                                }
                                else
                                {
                                    player.sendMessage(LangManager.instants.getMessage("error.nostructer"));
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
