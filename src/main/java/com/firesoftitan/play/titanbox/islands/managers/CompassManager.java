package com.firesoftitan.play.titanbox.islands.managers;

import org.bukkit.Location;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;

public class CompassManager {

    private Player player;
    private Location location;
    private Arrow arrow;
    public CompassManager(Player player, Location location) {
        this.player = player;
        this.location = location;
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
    public void removeArrow()
    {
        if (arrow != null && !arrow.isDead()) arrow.remove();
    }

    public void setArrow(Arrow arrow) {
        this.arrow = arrow;
    }
}
