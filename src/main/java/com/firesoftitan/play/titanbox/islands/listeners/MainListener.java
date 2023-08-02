package com.firesoftitan.play.titanbox.islands.listeners;

import com.firesoftitan.play.titanbox.islands.enums.MoveThresholdEnum;
import com.firesoftitan.play.titanbox.islands.enums.StructureTypeEnum;
import com.firesoftitan.play.titanbox.islands.managers.*;
import com.firesoftitan.play.titanbox.islands.runnables.CompassRunnable;
import com.firesoftitan.play.titanbox.islands.runnables.IslandSpawnerRunnable;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;

import static com.firesoftitan.play.titanbox.islands.TitanIslands.*;

public class MainListener implements Listener {

    public MainListener(){

    }
    public void registerEvents(){
        PluginManager pm = instance.getServer().getPluginManager();
        pm.registerEvents(this, instance);
    }
    private final HashMap<UUID, Location> playerMove = new HashMap<UUID, Location>();
    private final HashMap<UUID, Long> playerSpawn = new HashMap<UUID, Long>();

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {

    }
    @EventHandler
    public void onVehicleEnterEvent(VehicleEnterEvent event) {

    }

    @EventHandler
    public void onVehicleExitEvent(VehicleExitEvent event) {

    }
    @EventHandler
    public void onPlayerMoveEvent(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location last = playerMove.get(player.getUniqueId());
        Location location = player.getLocation().clone();
        // The Location you want to point to

        if (!Objects.requireNonNull(location.getWorld()).getName().equals(ConfigManager.getInstants().getWorld().getName())) return;
        //arrow.setRotation(yaw, pitch);

        if (last == null) {
            playerMove.put(player.getUniqueId(), location);
            last = location;
        }
        // Check if the player has moved at least 7 blocks from the previous location
        if (last.distance(player.getLocation()) > MoveThresholdEnum.BLOCKS_7.getDistance()) {
            playerMove.put(player.getUniqueId(), location);
            // Run unlock logic asynchronously 1 sec after player moves
            Bukkit.getScheduler().runTaskLater(instance, () -> {
                FragmentManager fragment = FragmentManager.getFragment(player.getLocation());
                if (fragment != null) {
                    StructureManager structure = StructureManager.getStructure(fragment.getNamespace(), fragment.getType(), fragment.getName());
                    if (structure != null) {
                        String name = fragment.getName();
                        boolean unlocked = playerManager.isUnlocked(player, structure);
                        if (!unlocked && !name.equalsIgnoreCase("water") && !name.equalsIgnoreCase("air")) {
                            playerManager.unlock(player, structure);
                            messageTool.sendMessagePlayer(player, LangManager.instants.getMessage("unlocked") + structure.getTitle());
                            int personalLimit = structure.getPersonalLimit();
                            String txtAmount = String.valueOf(personalLimit);
                            if (personalLimit == -1) txtAmount = LangManager.instants.getMessage("unlimited");
                            if (personalLimit == 0) txtAmount = LangManager.instants.getMessage("none");
                            txtAmount = txtAmount + LangManager.instants.getMessage("these");
                            messageTool.sendMessagePlayer(player, LangManager.instants.getMessage("build") + txtAmount);
                            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
                        }
                    }
                }else {
                    FragmentManager closest = FragmentManager.getClosest(location);
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
    public void onPlayerQuitEvent(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        CompassRunnable.instance.remove(player);
    }
    @EventHandler
    public void onPlayerJoinEvent(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (!playerManager.hasPlayerJoinedBefore(player))
        {
            World world = configManager.getWorld();
            Random random = new Random(System.currentTimeMillis());
            int x;
            int z;
            int y;
            if (configManager.isWorld_boarder()) {
                double size = player.getWorld().getWorldBorder().getSize() / 2;
                if (size > 100000) size = 100000;
                x = (int) (random.nextInt((int) (size * 2)) - size);
                z = (int) (random.nextInt((int) (size * 2)) - size);
            } else {
                x = random.nextInt(configManager.getX() * 2) - configManager.getX();
                z = random.nextInt(configManager.getZ() * 2) - configManager.getZ();
            }
            y = 150;
            Location location = new Location(world, x, y, z);
            new BukkitRunnable() {
                @Override
                public void run() {
                    Location location2 = new Location(world, location.getBlockX(), world.getHighestBlockYAt(location) + 2, location.getBlockZ());
                    playerManager.setHome(player, location2.clone());

                    player.teleport(location2.clone());
                    for(int i = 0; i < 100; i++)
                    {
                        if (LangManager.instants.contains("welcome.line_" + i))
                            player.sendMessage(LangManager.instants.getMessage("welcome.line_" + i));
                    }
                }
            }.runTaskLater(instance, 10);
            String emptyType = "empty";
            if (ConfigManager.getInstants().getType().equalsIgnoreCase("air")) emptyType = "empty_air";
            StructureManager structure = StructureManager.getStructure("primary", StructureTypeEnum.INLAND, emptyType);
            playerManager.unlock(player, structure);
            IslandManager.generateIsland(player, location);

        }
    }
}


