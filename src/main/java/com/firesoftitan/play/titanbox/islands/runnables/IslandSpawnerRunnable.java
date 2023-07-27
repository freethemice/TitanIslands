package com.firesoftitan.play.titanbox.islands.runnables;

import com.firesoftitan.play.titanbox.islands.TitanIslands;
import com.firesoftitan.play.titanbox.islands.managers.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class IslandSpawnerRunnable extends BukkitRunnable {
    private static final Random random = new Random(System.currentTimeMillis());
    public static IslandSpawnerRunnable instance;
    private int count = 0;
    public IslandSpawnerRunnable() {
        super();
        instance = this;
    }

    @Override
    public void run() {
        List<Location> homes = PlayerManager.instants.getHomes();
        List<Player> playerList = new ArrayList<Player>(Bukkit.getOnlinePlayers());
        if (!playerList.isEmpty())
        {
            homes = new ArrayList<Location>();
            for(Player player: playerList)
            {
                if (player.getWorld().getName().equals(ConfigManager.instants.getWorld().getName())) homes.add(player.getLocation().clone());
            }
        }
        Location location;
        count++;
        if (count > ConfigManager.getInstants().getMajor())
        {
            location = ConfigManager.instants.getWorld().getSpawnLocation().clone();
            count = 0;
        }
        else {
            do {
                if (homes.isEmpty()) return;
                int i = random.nextInt(homes.size());
                location = homes.get(i).clone();
                homes.remove(i);
                if (ConfigManager.instants.getMax_islands() < 0) break;
            } while (IslandManager.getSurrounding(location).size() >= ConfigManager.instants.getMax_islands());
        }

        // Get minimum and maximum distance from player


        IslandSpawnerRunnable.spawnRandomIsland(location);

    }

    public static void spawnRandomIsland(Location location) {
        int minDistance = TitanIslands.configManager.getDistance_min();  // Blocks
        int maxDistance = TitanIslands.configManager.getDistance_max() + 1; // Blocks

        // Get random distance within range
        int distance = random.nextInt(maxDistance - minDistance) + minDistance;

        // Get random angle
        double angle = random.nextDouble() * 2 * Math.PI;

        // Calculate x and z offsets
        int xOffset = (int) (Math.sin(angle) * distance);
        int zOffset = (int) (Math.cos(angle) * distance);
        // Add offsets to player location
        Location randomLoc = location.clone().add(xOffset, 0, zOffset);

        IslandManager.generateIsland(randomLoc);

        if (TitanIslands.configManager.isAnnounce()) {
            List<Player> playerList = new ArrayList<Player>(Bukkit.getOnlinePlayers());
            if (!playerList.isEmpty()) {
                for (Player playerA : playerList) {
                    playerA.playSound(playerA.getLocation(), Sound.BLOCK_ANVIL_PLACE, 1, 1);
                    playerA.sendMessage(LangManager.instants.getMessage("announce") + randomLoc.getBlockX() + ", " + randomLoc.getBlockZ());
                }
            }
        }
    }
}
