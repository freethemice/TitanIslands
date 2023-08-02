package com.firesoftitan.play.titanbox.islands.managers;

import com.firesoftitan.play.titanbox.islands.TitanIslands;
import com.firesoftitan.play.titanbox.islands.enums.StructureTypeEnum;
import com.firesoftitan.play.titanbox.libs.managers.SaveManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;


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
    public void remove(Player player, FragmentManager fragmentManager)
    {
        playerData.delete(player.getUniqueId() + ".fragments." + fragmentManager.getId());
    }
    public void add(Player player, IslandManager islandManager)
    {
        playerData.set(player.getUniqueId() + ".islands." + islandManager.getId() + ".key", islandManager.getId());

    }
    public void add(Player player, FragmentManager fragmentManager)
    {
        playerData.set(player.getUniqueId() + ".fragments." + fragmentManager.getId() + ".key", fragmentManager.getId());
        int count = 0;
        if (playerData.contains(player.getUniqueId() + ".counts." + fragmentManager.getNamespace()  + ":" +  fragmentManager.getType().getName()  + ":" +  fragmentManager.getName()))
        {
            count = playerData.getInt(player.getUniqueId() + ".counts." + fragmentManager.getNamespace()  + ":" +  fragmentManager.getType().getName()  + ":" +  fragmentManager.getName());
        }
        count++;
        playerData.set(player.getUniqueId() + ".counts." + fragmentManager.getNamespace()  + ":" +  fragmentManager.getType().getName()  + ":" +  fragmentManager.getName(), count);
    }
    public void setCount(Player player, StructureManager structureManager, int amount)
    {
        setCount(player.getUniqueId(), structureManager, amount);
    }
    public void setCount(UUID uuid, StructureManager structureManager, int amount)
    {
        playerData.set(uuid + ".counts." + structureManager.getNamespace() + ":" + structureManager.getType().getName() + ":" + structureManager.getName(), amount);
    }

    public int getCount(Player player, FragmentManager fragmentManager)
    {
        return getCount(player.getUniqueId(), fragmentManager);
    }
    public int getCount(UUID uuid, FragmentManager fragmentManager)
    {
        StructureManager structure = StructureManager.getStructure(fragmentManager.getNamespace(), fragmentManager.getType(), fragmentManager.getName());
        return getCount(uuid, structure);
    }
    public int getCount(Player player, StructureManager structureManager)
    {
        return getCount(player.getUniqueId(), structureManager);
    }
    public List<StructureManager> getCountList(Player player)
    {
        return getCountList(player.getUniqueId());
    }
    public List<StructureManager> getCountList(UUID uuid)
    {
        List<StructureManager> managers = new ArrayList<StructureManager>();
        for(String key: playerData.getKeys(uuid + ".counts"))
        {
            managers.add(StructureManager.getStructure(key));
        }
        return managers;
    }
    public int getCount(UUID uuid, StructureManager structureManager)
    {
        int count = 0;
        if (playerData.contains(uuid + ".counts." + structureManager.getNamespace() + ":" + structureManager.getType().getName() + ":" + structureManager.getName()))
        {
            count = playerData.getInt(uuid + ".counts." + structureManager.getNamespace() + ":" + structureManager.getType().getName() + ":" + structureManager.getName());
        }
        return count;
    }
    public void unlock(Player player,StructureManager structureManager)
    {
        unlock(player.getUniqueId(), structureManager);
    }
    public void unlock(UUID uuid, StructureManager structureManager)
    {
        List<String> stringList = playerData.getStringList(uuid + ".unlocked");
        if (stringList == null) stringList = new ArrayList<String>();
        if (!stringList.contains(structureManager.getNamespace() + ":" + structureManager.getType().getName() + ":" + structureManager.getName())) stringList.add(structureManager.getNamespace() + ":" + structureManager.getType().getName() + ":" + structureManager.getName());
        playerData.set(uuid + ".unlocked", stringList);
    }
    public boolean isUnlocked(Player player, StructureManager structureManager)
    {
        return isUnlocked(player.getUniqueId(), structureManager);
    }
    public boolean isUnlocked(UUID uuid, StructureManager structureManager)
    {
        List<String> stringList = playerData.getStringList(uuid + ".unlocked");
        if (stringList == null) stringList = new ArrayList<String>();
        return stringList.contains(structureManager.getNamespace() + ":" + structureManager.getType().getName() + ":" + structureManager.getName());

    }
    public List<String> getUnlocked(Player player, String namespace)
    {
        List<String> stringList = playerData.getStringList(player.getUniqueId() + ".unlocked");
        if (stringList == null) stringList = new ArrayList<String>();
        List<String> out = new ArrayList<String>();
        for(String key: stringList)
        {
            if (key.startsWith(namespace + ":" )) out.add(key);
        }
        return out;
    }

    public List<String> getUnlocked(Player player, String namespace, StructureTypeEnum structureTypeEnum)
    {
        List<String> stringList = playerData.getStringList(player.getUniqueId() + ".unlocked");
        if (stringList == null) stringList = new ArrayList<String>();
        List<String> out = new ArrayList<String>();
        for(String key: stringList)
        {
            if (key.startsWith(namespace + ":" + structureTypeEnum.getName() + ":")) out.add(key);
        }
        return out;
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
    public boolean isOwnedByPlayer(Player player, FragmentManager fragmentManager)
    {
        return playerData.contains(player.getUniqueId() + ".fragments." + fragmentManager.getId());
    }

    public UUID getOwner(FragmentManager fragmentManager)
    {
        for (String key: playerData.getKeys()) {
            if (playerData.contains(key + ".fragments." + fragmentManager.getId()))
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
