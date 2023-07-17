package com.firesoftitan.play.titanbox.islands.managers;

import com.firesoftitan.play.titanbox.islands.TitanIslands;
import com.firesoftitan.play.titanbox.libs.managers.SaveManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class PlayerManager {
    private SaveManager playerData = new SaveManager(TitanIslands.instance.getName(), "player_data");
    public static PlayerManager instants;
    public PlayerManager() {
        instants = this;
    }
    private HashMap<String, Integer> structureCount = new HashMap<String, Integer>();
    public boolean hasPlayerJoinedBefore(Player player)
    {
        return playerData.contains(player.getUniqueId().toString());
    }
    public void add(Player player, CubeSelectorManager cubeSelectorManager)
    {
        playerData.set(player.getUniqueId() + ".cubes." + cubeSelectorManager.getKey()+ ".key", cubeSelectorManager.getName());
        int count = 0;
        if (playerData.contains(player.getUniqueId() + ".counts." + cubeSelectorManager.getName()))
        {
            count = playerData.getInt(player.getUniqueId() + ".counts." + cubeSelectorManager.getName());
        }
        count++;
        playerData.set(player.getUniqueId() + ".counts." + cubeSelectorManager.getName(), count);
    }
    public int getCount(Player player, CubeSelectorManager cubeSelectorManager)
    {
        return getCount(player,cubeSelectorManager.getName());
    }
    public int getCount(Player player, String name)
    {
        int count = 0;
        if (playerData.contains(player.getUniqueId() + ".counts." + name))
        {
            count = playerData.getInt(player.getUniqueId() + ".counts." + name);
        }
        return count;
    }
    public void unlock(Player player, String name)
    {
        name = name.toLowerCase();
        List<String> stringList = playerData.getStringList(player.getUniqueId() + ".unlocked");
        if (stringList == null) stringList = new ArrayList<String>();
        if (!stringList.contains(name)) stringList.add(name);
        playerData.set(player.getUniqueId() + ".unlocked", stringList);
    }
    public boolean isUnlocked(Player player, String name)
    {
        name = name.toLowerCase();
        List<String> stringList = playerData.getStringList(player.getUniqueId() + ".unlocked");
        if (stringList == null) stringList = new ArrayList<String>();
        boolean contains = stringList.contains(name);
        return contains;

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
    public boolean isOwnedByPlayer(Player player, CubeSelectorManager cubeSelectorManager)
    {
        return playerData.contains(player.getUniqueId() + ".cubes." + cubeSelectorManager.getKey());
    }
    public Player getOwner(CubeSelectorManager cubeSelectorManager)
    {
        for (String key: playerData.getKeys()) {
            if (playerData.contains(key + ".cubes." + cubeSelectorManager.getKey()))
            {
                UUID uuid = UUID.fromString(key);
                Player player = Bukkit.getPlayer(uuid);
                return player;
            }
        }
        return null;
    }
    public void save()
    {

        playerData.save();
    }
}
