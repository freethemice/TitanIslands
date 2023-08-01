package com.firesoftitan.play.titanbox.islands.managers;

import com.firesoftitan.play.titanbox.islands.TitanIslands;
import com.firesoftitan.play.titanbox.libs.managers.SaveManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;


public class PlayerManager {
    private final SaveManager playerData = new SaveManager(TitanIslands.instance.getName(), "player_data");
    public static PlayerManager instants;
    public PlayerManager() {
        instants = this;
    }
    private final HashMap<String, Integer> structureCount = new HashMap<String, Integer>();
    public boolean hasPlayerJoinedBefore(Player player)
    {
        return playerData.contains(player.getUniqueId().toString());
    }
    public void remove(Player player, IslandManager islandManager)
    {
        playerData.delete(player.getUniqueId() + ".islands." + islandManager.getId());
    }
    public void remove(Player player, CubeManager cubeManager)
    {
        playerData.delete(player.getUniqueId() + ".cubes." + cubeManager.getId());
    }
    public void add(Player player, IslandManager islandManager)
    {
        playerData.set(player.getUniqueId() + ".islands." + islandManager.getId() + ".key", islandManager.getId());

    }
    public void add(Player player, CubeManager cubeManager)
    {
        playerData.set(player.getUniqueId() + ".cubes." + cubeManager.getId() + ".key", cubeManager.getId());
        int count = 0;
        if (playerData.contains(player.getUniqueId() + ".counts." + cubeManager.getName()))
        {
            count = playerData.getInt(player.getUniqueId() + ".counts." + cubeManager.getName());
        }
        count++;
        playerData.set(player.getUniqueId() + ".counts." + cubeManager.getName(), count);
    }
    public void setCount(Player player, String name, int amount)
    {
        setCount(player.getUniqueId(), name, amount);
    }
    public void setCount(UUID uuid, String name, int amount)
    {
        playerData.set(uuid + ".counts." + name, amount);
    }

    public int getCount(Player player, CubeManager cubeManager)
    {
        return getCount(player, cubeManager.getName());
    }
    public int getCount(UUID uuid, CubeManager cubeManager)
    {
        return getCount(uuid, cubeManager.getName());
    }
    public int getCount(Player player, String name)
    {
        return getCount(player.getUniqueId(), name);
    }
    public List<String> getCountList(Player player)
    {
        return getCountList(player.getUniqueId());
    }
    public List<String> getCountList(UUID uuid)
    {
        return new ArrayList<String>(playerData.getKeys(uuid + ".counts"));
    }
    public int getCount(UUID uuid, String name)
    {
        int count = 0;
        if (playerData.contains(uuid + ".counts." + name))
        {
            count = playerData.getInt(uuid + ".counts." + name);
        }
        return count;
    }
    public void unlock(Player player, String name)
    {
        unlock(player.getUniqueId(), name);
    }
    public void unlock(UUID uuid, String name)
    {
        name = name.toLowerCase();
        List<String> stringList = playerData.getStringList(uuid + ".unlocked");
        if (stringList == null) stringList = new ArrayList<String>();
        if (!stringList.contains(name)) stringList.add(name);
        playerData.set(uuid + ".unlocked", stringList);
    }
    public boolean isUnlocked(Player player, String name)
    {
        return isUnlocked(player.getUniqueId(), name);
    }
    public boolean isUnlocked(UUID uuid, String name)
    {
        name = name.toLowerCase();
        List<String> stringList = playerData.getStringList(uuid + ".unlocked");
        if (stringList == null) stringList = new ArrayList<String>();
        return stringList.contains(name);

    }
    public List<String> getUnlocked(Player player)
    {
        List<String> stringList = playerData.getStringList(player.getUniqueId() + ".unlocked");
        if (stringList == null) stringList = new ArrayList<String>();
        return stringList;
    }
    public void setHome(Player player, Location location)
    {
        playerData.set(player.getUniqueId() + ".home", location);
    }
    public Location getHome(Player player)
    {
        return playerData.getLocation(player.getUniqueId() + ".home");
    }
    public List<Location> getHomes()
    {
        List<Location> locations = new ArrayList<Location>();
        for (String key: playerData.getKeys()) {
            Location location = playerData.getLocation(key + ".home");
            if (location != null) locations.add(location.clone());
        }
        return locations;
    }
    public boolean isOwnedByPlayer(Player player, IslandManager islandManager)
    {
        return playerData.contains(player.getUniqueId() + ".islands." + islandManager.getId());
    }
    public boolean isOwnedByPlayer(Player player, CubeManager cubeManager)
    {
        return playerData.contains(player.getUniqueId() + ".cubes." + cubeManager.getId());
    }

    public UUID getOwner(CubeManager cubeManager)
    {
        for (String key: playerData.getKeys()) {
            if (playerData.contains(key + ".cubes." + cubeManager.getId()))
            {
                return UUID.fromString(key);
            }
        }
        return null;
    }
    public UUID getOwner(IslandManager islandManager)
    {
        for (String key: playerData.getKeys()) {
            if (playerData.contains(key + ".islands." + islandManager.getId()))
            {
                return UUID.fromString(key);
            }
        }
        return null;
    }
    public void save()
    {

        playerData.save();
    }
}
