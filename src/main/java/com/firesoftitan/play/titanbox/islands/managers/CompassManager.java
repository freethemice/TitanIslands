package com.firesoftitan.play.titanbox.islands.managers;

import com.firesoftitan.play.titanbox.libs.managers.HologramManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;

import static com.firesoftitan.play.titanbox.islands.TitanIslands.tools;

public class CompassManager {

    private final Player player;
    private Location location;
    private Arrow arrow;

    private HologramManager hologramManager;
    public CompassManager(Player player, Location location) {
        this.player = player;
        this.location = location;
        this.hologramManager = tools.getHologramTool().addHologram(player.getLocation().clone().add(0, 3, 0));
    }

    public Player getPlayer() {
        return player;
    }

    public void setLocation(Location location) {
        if (location != null) {
            this.location = location.clone();
        }
    }

    public Location getLocation() {
        return location.clone();
    }

    public Arrow getArrow() {
        return arrow;
    }
    public void removeHologram()
    {
        this.hologramManager.setText("");
        this.hologramManager.delete();
        this.hologramManager = null;
    }
    public void removeArrow()
    {
        if (arrow != null && !arrow.isDead())
        {
            arrow.removePassenger(this.hologramManager.getArmorStand());
            arrow.remove();
        }
    }

    public void setArrow(Arrow arrow) {
        this.arrow = arrow;
        this.arrow.addPassenger(this.hologramManager.getArmorStand());
        if (this.hologramManager == null) this.hologramManager = tools.getHologramTool().addHologram(player.getLocation().clone().add(0, 3, 0));
        this.hologramManager.setText(ChatColor.GREEN + String.valueOf((int)arrow.getLocation().distance(this.location)) + LangManager.instants.getMessage("blocks"));
    }
}
