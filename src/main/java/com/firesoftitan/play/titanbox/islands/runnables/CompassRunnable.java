package com.firesoftitan.play.titanbox.islands.runnables;

import com.firesoftitan.play.titanbox.islands.managers.CompassManager;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

import static com.firesoftitan.play.titanbox.islands.TitanIslands.tools;

public class CompassRunnable extends BukkitRunnable {

    public static CompassRunnable instance;
    private final Map<UUID, CompassManager> compassManagers = new HashMap<UUID, CompassManager>();
    private final List<CompassManager> managersToTick = new ArrayList<CompassManager>();
    public CompassRunnable() {
        instance = this;
    }
    public void changeLocation(Player player, Location location)
    {
        UUID playerId = player.getUniqueId();
        CompassManager manager = compassManagers.get(playerId);
        if (manager != null) {
            manager.setLocation(location);
        }
    }
    public Location getLocation(Player player)
    {
        UUID playerId = player.getUniqueId();
        CompassManager manager = compassManagers.get(playerId);
        if (manager != null) {
            return manager.getLocation();
        }
        return null;
    }
    public boolean hasCompass(Player player)
    {
        return compassManagers.containsKey(player.getUniqueId());
    }
    public void add(Player player, Location location)
    {
        CompassManager compassManager = new CompassManager(player, location);
        compassManagers.put(player.getUniqueId(), compassManager);
    }
    public void remove(Player player) {
        UUID playerId = player.getUniqueId();
        remove(playerId);
    }
    public void remove(UUID playerId) {

        CompassManager manager = compassManagers.get(playerId);
        if (manager != null) {
            compassManagers.remove(playerId);
            managersToTick.remove(manager);
            manager.removeArrow();
            manager.removeHologram();
        }
    }
    public void shutdown()
    {
        try {
            this.cancel();
        } catch (IllegalStateException ignored) {

        }
        for(UUID uuid: compassManagers.keySet())
        {
            remove(uuid);
        }
    }
    @Override
    public void run() {
        if (managersToTick.isEmpty())
        {
            managersToTick.addAll(compassManagers.values());
            return;
        }
        for (int i = 0; i<Math.min(10, managersToTick.size()); i++)
        {
            CompassManager compassManager = managersToTick.get(i);
            managersToTick.remove(i);

            compassManager.removeArrow();
            Location eyeLocation = compassManager.getPlayer().getEyeLocation();
            Vector direction = eyeLocation.getDirection();
            Location targetLocation = eyeLocation.clone().add(direction.multiply(3));
            Location home = compassManager.getLocation();

            targetLocation.setY(compassManager.getPlayer().getLocation().getY() + 1);
            double x = home.getX() - targetLocation.getX();
            double y = home.getY() - targetLocation.getY();
            double z = home.getZ() - targetLocation.getZ();
            if (targetLocation.distance(home) < 7) {
                x = 0;
                y = -7;
                z = 0;
                //return;
                targetLocation = home.clone().add(0, 3, 0);
            }

            Vector normalize = new Vector(x, y, z).normalize();


            float yaw = (float) Math.toDegrees(Math.atan2(x, z));
            float pitch = (float) Math.toDegrees(Math.atan2(y, Math.sqrt(x * x + z * z)));

            targetLocation.setPitch(pitch);
            targetLocation.setYaw(yaw);
            Arrow arrow = spawnArrow(targetLocation, normalize.multiply(0.1));
            compassManager.setArrow(arrow);
        }

    }
    // Method to spawn arrow
    private Arrow spawnArrow(Location loc, Vector vector) {
        World world = loc.getWorld();
        Arrow arrow = (Arrow) tools.getEntityTool().summonEntity(world, EntityType.ARROW,  "{Motion:[" +  vector.getX() + "," + vector.getY() + "," + vector.getZ() + "],NoGravity:1b}", loc);
        arrow.setGravity(false);
        arrow.setGlowing(true);
        arrow.setPersistent(true);
        arrow.setDamage(0);


        return arrow;
    }
}
