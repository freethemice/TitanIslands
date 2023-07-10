package com.firesoftitan.play.titanbox.islands.managers;

import com.firesoftitan.play.titanbox.libs.managers.SaveManager;
import com.firesoftitan.play.titanbox.islands.TitanIslands;
import com.firesoftitan.play.titanbox.libs.managers.SettingsManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ConfigManager {
    public static ConfigManager instants;
    private SettingsManager configFile;
    private String language, world;
    private List<String> starting;
    private int randomI,x,z;
    private boolean world_boarder, announce;
    private int distance_min, distance_max, time, count_min, count_max, closest;

    public ConfigManager() {
        reload();
        instants = this;
    }
    public void reload()
    {
        configFile = new SettingsManager(TitanIslands.instance.getName(), "config");


        if (!configFile.contains("settings.language"))
        {
            configFile.set("settings.language", "en_us");
        }
        if (!configFile.contains("settings.world"))
        {
            configFile.set("settings.world.name", "world");
            configFile.set("settings.world.world_boarder", true);
            configFile.set("settings.world.x", 20000);
            configFile.set("settings.world.z", 20000);
        }
        if (!configFile.contains("settings.starting.islands"))
        {
            List<String> starting = new ArrayList<String>();
            starting.add("starter");
            starting.add("oak");
            starting.add("oak");
            starting.add("stone");
            starting.add("stone");
            starting.add("sand");
            configFile.set("settings.starting.islands", starting);
        }
        if (!configFile.contains("settings.starting.random"))
        {
            configFile.set("settings.starting.random", 1);
        }
        if (!configFile.contains("settings.environment"))
        {
            configFile.set("settings.environment.distance.min", 50);
            configFile.set("settings.environment.distance.max", 300);
            configFile.set("settings.environment.distance.closest", 300);
            configFile.set("settings.environment.time", 1800);
            configFile.set("settings.environment.count.min", 1);
            configFile.set("settings.environment.count.max", 3);
            configFile.set("settings.environment.announce", true);
        }

        this.closest = configFile.getInt("settings.environment.distance.closest");
        this.distance_min = configFile.getInt("settings.environment.distance.min");
        this.distance_max = configFile.getInt("settings.environment.distance.max");
        if (closest < distance_max)
        {
            closest = distance_max;
            configFile.set("settings.environment.distance.max", distance_max);
        }
        this.time = configFile.getInt("settings.environment.time");
        this.announce = configFile.getBoolean("settings.environment.announce");
        this.count_min = configFile.getInt("settings.environment.count.min");
        this.count_max = configFile.getInt("settings.environment.count.max");
        this.world = configFile.getString("settings.world.name");
        this.world_boarder = configFile.getBoolean("settings.world.world_boarder");
        this.x = configFile.getInt("settings.world.x");
        this.z = configFile.getInt("settings.world.z");
        this.starting = configFile.getStringList("settings.starting.islands");
        this.randomI = configFile.getInt("settings.starting.random");
        this.language = configFile.getString("settings.language");
        configFile.save();

    }

    public int getClosest() {
        return closest;
    }

    public boolean isAnnounce() {
        return announce;
    }

    public int getDistance_min() {
        return distance_min;
    }

    public int getDistance_max() {
        return distance_max;
    }

    public int getTime() {
        return time;
    }

    public int getCount_min() {
        return count_min;
    }

    public int getCount_max() {
        return count_max;
    }

    public String getLanguage() {
        return language;
    }

    public int getX() {
        return x;
    }

    public int getZ() {
        return z;
    }

    public boolean isWorld_boarder() {
        return world_boarder;
    }

    public World getWorld() {
        return Bukkit.getWorld(world);
    }

    public List<String> getStarting() {
        List<String> clone = new ArrayList<>(starting);
        return clone;
    }

    public int getRandomI() {
        return randomI;
    }
}
