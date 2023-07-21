package com.firesoftitan.play.titanbox.islands.managers;

import com.firesoftitan.play.titanbox.islands.TitanIslands;
import com.firesoftitan.play.titanbox.libs.managers.SaveManager;
import org.bukkit.Location;

import java.util.*;

public class IslandManager {
    private static final Map<UUID, IslandManager> islands = new HashMap<UUID, IslandManager>();
    public static IslandManager getIsland(UUID uuid)
    {
        return islands.get(uuid);
    }
    public static IslandManager getIsland(Location location)
    {
        List<IslandManager> islandManagers = new ArrayList<>(IslandManager.islands.values());
        for (IslandManager islandManager : islandManagers) {
            if (islandManager.isInIsland(location)) {
                return islandManager;
            }
        }

        return null;
    }
    public static IslandManager getIsland(CubeManager cubeManager)
    {
        List<IslandManager> islandManagers = new ArrayList<>(IslandManager.islands.values());
        for (IslandManager islandManager : islandManagers) {
            if (islandManager.hasCube(cubeManager)) {
                return islandManager;
            }
        }

        return null;
    }
    private static final SaveManager islandSaves = new SaveManager(TitanIslands.instance.getName(), "islands");
    public static void loadAll()
    {
        for(String key: islandSaves.getKeys())
        {
            SaveManager saveManager = islandSaves.getSaveManager(key);
            IslandManager selectorTool = new IslandManager(saveManager);
        }
    }
    public static void saveAll()
    {
        for (IslandManager islandManager : islands.values())
        {
            SaveManager save = islandManager.save();
            islandSaves.set(islandManager.getId().toString(), save);
        }
        islandSaves.save();
    }

    private final UUID id;
    private Location location;
    private final Map<UUID, CubeManager> cubes = new HashMap<UUID, CubeManager>();
    public IslandManager() {
        id = generateID();
        IslandManager.islands.put(id, this);
    }
    public IslandManager(SaveManager saveManager) {
        id = saveManager.getUUID("id");
        IslandManager.islands.put(id, this);
    }
    public SaveManager save()
    {
        SaveManager saveManager = new SaveManager();
        saveManager.set("id", this.id);
        return saveManager;
    }
    public Location getLocation() {
        if (location == null) return null;
        return location.clone();
    }

    public UUID getId() {
        return id;
    }

    public void add(CubeManager cubeManager)
    {
        if (cubes.size() == 0) location = cubeManager.getCenter().clone();
        cubes.put(cubeManager.getId(), cubeManager);
    }
    public boolean hasCube(CubeManager cubeManager)
    {
        return this.cubes.containsKey(cubeManager.getId());
    }
    public UUID generateID()
    {
        UUID idtmp = UUID.randomUUID();
        while (islands.containsKey(idtmp))
        {
            idtmp = UUID.randomUUID();
        }
        return idtmp;
    }
    public boolean isInIsland(Location location)
    {
        List<CubeManager> cubes = new ArrayList<>(this.cubes.values());
        return cubes.stream().anyMatch(cube -> CubeManager.isInCube(location, cube));
    }
}
