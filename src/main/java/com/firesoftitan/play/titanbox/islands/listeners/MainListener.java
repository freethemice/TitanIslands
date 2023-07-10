package com.firesoftitan.play.titanbox.islands.listeners;

import com.firesoftitan.play.titanbox.islands.TitanIslands;
import com.firesoftitan.play.titanbox.islands.enums.MoveThresholdEnum;
import com.firesoftitan.play.titanbox.islands.managers.ConfigManager;
import com.firesoftitan.play.titanbox.islands.managers.LangManager;
import com.firesoftitan.play.titanbox.islands.managers.StructureManager;
import com.firesoftitan.play.titanbox.islands.managers.CubeSelectorManager;
import com.firesoftitan.play.titanbox.islands.runnables.IslandSpawnerRunnable;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.bukkit.plugin.PluginManager;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import static com.firesoftitan.play.titanbox.islands.TitanIslands.*;

public class MainListener  implements Listener {

    public MainListener(){

    }
    public void registerEvents(){
        PluginManager pm = instance.getServer().getPluginManager();
        pm.registerEvents(this, instance);
    }
    private HashMap<UUID, Location> playerMove = new HashMap<UUID, Location>();
    private HashMap<UUID, Long> playerSpawn = new HashMap<UUID, Long>();
    @EventHandler
    public void onPlayerMoveEvent(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location last = playerMove.get(player.getUniqueId());
        Location location = player.getLocation().clone();
        if (last == null) {
            playerMove.put(player.getUniqueId(), location);
            last = location;
        }
        // Check if player has moved at least 7 blocks from previous location
        if (last.distance(player.getLocation()) > MoveThresholdEnum.BLOCKS_7.getDistance()) {
            playerMove.put(player.getUniqueId(), location);
            // Run unlock logic asynchronously 1 sec after player moves
            Bukkit.getScheduler().runTaskLater(instance, () -> {
                CubeSelectorManager cube = CubeSelectorManager.getCube(player.getLocation());
                if (cube != null) {
                    String name = cube.getName();
                    boolean unlocked = playerManager.isUnlocked(player, name);
                    StructureManager structure = StructureManager.getStructure(name);
                    Boolean unlockable = structure.isUnlockable();
                    if (!unlocked && unlockable) {
                        playerManager.unlock(player, name);
                        messageTool.sendMessagePlayer(player, LangManager.instants.getMessage("unlocked") + structure.getTitle());
                    }
                }else {
                    CubeSelectorManager closest = CubeSelectorManager.getClosest(location);
                    if (closest != null)
                    {
                        double distance = closest.getCenter().distance(location);
                        if (distance > ConfigManager.instants.getClosest())
                        {
                            if (!playerSpawn.containsKey(player.getUniqueId()) || playerSpawn.get(player.getUniqueId()) + 60000 < System.currentTimeMillis()) {
                                playerSpawn.put(player.getUniqueId(), System.currentTimeMillis());
                                IslandSpawnerRunnable.spawnRandomIsland(location);
                            }
                        }
                    }
                }
            }, 20);
        }
    }
    @EventHandler
    public void onPlayerJoinEvent(PlayerJoinEvent event) {
        Player player = event.getPlayer();;
        if (!playerManager.hasPlayerJoinedBefore(player))
        {
            World world = configManager.getWorld();
            Random random = new Random(System.currentTimeMillis());
            int x = 0;
            int z = 0;
            int y = 0;
            if (configManager.isWorld_boarder()) {
                double size = player.getWorld().getWorldBorder().getSize() / 2;
                if (size > 100000) size = 100000;
                x = (int) (random.nextInt((int) (size * 2)) - size);
                z = (int) (random.nextInt((int) (size * 2)) - size);
            } else {
                x = (int) (random.nextInt((int) (configManager.getX() * 2)) - configManager.getX());
                z = (int) (random.nextInt((int) (configManager.getZ() * 2)) - configManager.getZ());
            }
            y = 150;
            Location location = new Location(world, x, y, z);
            Location finalLocation = location;
            new BukkitRunnable() {
                @Override
                public void run() {
                    Location location2 = new Location(world, finalLocation.getBlockX(), world.getHighestBlockYAt(finalLocation) + 2, finalLocation.getBlockZ());
                    playerManager.setHome(player, location2.clone());
                    player.teleport(location2.clone());
                    for(int i = 0; i < 100; i++)
                    {
                        if (LangManager.instants.contains("welcome.line_" + i))
                            player.sendMessage(LangManager.instants.getMessage("welcome.line_" + i));
                    }
                }
            }.runTaskLater(instance, 10);
            List<String> starting = configManager.getStarting();
            int randomI = configManager.getRandomI();
            for (int i = 0; i < randomI; i++) {
                StructureManager randomIsland = StructureManager.getRandomIsland();
                starting.add(randomIsland.getName());
            }
            for(String iKey: starting)
            {
                Location check = CubeSelectorManager.adjustLocation(iKey, location);
                StructureManager structure = StructureManager.getStructure(iKey);
                CubeSelectorManager build = structure.build(check);
                if (!CubeSelectorManager.isOverlapping(build)) {
                    build.place(structure.getName());
                    playerManager.add(player, build);
                    //if (structure.isUnlockable()) playerManager.unlock(player, build.getName());
                    location = build.getCenter();
                }
            }
        }
    }



}


