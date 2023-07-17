package com.firesoftitan.play.titanbox.islands.runnables;

import com.firesoftitan.play.titanbox.islands.TitanIslands;
import com.firesoftitan.play.titanbox.islands.managers.CubeSelectorManager;
import com.firesoftitan.play.titanbox.islands.managers.LangManager;
import com.firesoftitan.play.titanbox.islands.managers.PlayerManager;
import com.firesoftitan.play.titanbox.islands.managers.StructureManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class IslandSpawnerRunnable extends BukkitRunnable {
    private static Random random = new Random(System.currentTimeMillis());
    public static IslandSpawnerRunnable instance;
    public IslandSpawnerRunnable() {
        super();
        instance = this;
    }

    @Override
    public void run() {
        List<Player> playerList = new ArrayList<Player>(Bukkit.getOnlinePlayers());
        Location location = null;
        if (!playerList.isEmpty()) {
            int i = random.nextInt(playerList.size());
            Player player = playerList.get(i);
            location = PlayerManager.instants.getHome(player);
        }
        else
        {
            //no ones online
            List<Location> homes = PlayerManager.instants.getHomes();
            if (homes.isEmpty()) return;
            int i = random.nextInt(homes.size());
            location = homes.get(i).clone();
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

        int count_max = TitanIslands.configManager.getCount_max() + 1;
        int count_min = TitanIslands.configManager.getCount_min();
        int size = random.nextInt(count_max - count_min) + count_min;
        for(int i2 = 0; i2 < size; i2++) {
            StructureManager structure = StructureManager.getRandomIsland();
            Location check = CubeSelectorManager.adjustLocation(structure.getName(), randomLoc);
            CubeSelectorManager build = structure.build(check);
            if (!CubeSelectorManager.isOverlapping(build)) {
                build.place(structure.getName());
            }
        }
        if (TitanIslands.configManager.isAnnounce()) {
            List<Player> playerList = new ArrayList<Player>(Bukkit.getOnlinePlayers());
            if (!playerList.isEmpty()) {
                for (Player playerA : playerList) {
                    playerA.sendMessage(LangManager.instants.getMessage("announce") + randomLoc.getBlockX() + ", " + randomLoc.getBlockZ());
                }
            }
        }
    }
}
