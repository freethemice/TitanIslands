package com.firesoftitan.play.titanbox.islands.managers;

import com.firesoftitan.play.titanbox.islands.TitanIslands;
import com.firesoftitan.play.titanbox.libs.managers.SettingsManager;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ConfigManager {
    public static ConfigManager instants;
    private String language, world, default_structure;
    private List<String> starting;
    private long decay;
    private int x,z;
    private boolean world_boarder, announce, protection_wild_creepers, protection_wild_break, protection_not_owned_creepers,
            protection_not_owned_break, protection_owned_creepers;
    private int distance_min, distance_max, time, count_min, count_max, closest;
    private int size_col, size_row, max_islands, major, placement;
    private String default_starting_shore, default_starting_inland, type;
    private boolean protection_wild_build;
    private boolean protection_not_owned_build;
    private boolean protection_wild_use;
    private boolean protection_not_owned_use;
    private boolean protection_wild_ignite;
    private boolean protection_not_owned_ignite;


    public ConfigManager() {
        reload();
        instants = this;
    }
    public void reload()
    {
        SettingsManager configFile = new SettingsManager(TitanIslands.instance.getName(), "config");

        if (!configFile.contains("settings.server"))
        {
            configFile.set("settings.server.type", "water");
            configFile.set("settings.server.placement", "auto");
        }
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
        if (!configFile.contains("settings.starting.structures"))
        {
            configFile.set("settings.starting.default.shore", "sand");
            configFile.set("settings.starting.default.inland", "grass");
            configFile.set("settings.starting.size.col", 5);
            configFile.set("settings.starting.size.row", 5);
            List<String> starting = new ArrayList<String>();
            starting.add("oak");
            starting.add("oak");
            starting.add("starter");
            starting.add("oak");
            starting.add("oak");
            starting.add("stone");
            starting.add("stone");
            starting.add("coal");
            configFile.set("settings.starting.structures", starting);
        }
        if (!configFile.contains("settings.environment"))
        {
            configFile.set("settings.environment.default.structure", "sand");
            configFile.set("settings.environment.distance.min", 200);
            configFile.set("settings.environment.distance.max", 500);
            configFile.set("settings.environment.distance.closest", 500);
            configFile.set("settings.environment.time", 1800);
            configFile.set("settings.environment.decay", 10800);
            configFile.set("settings.environment.max", 25);
            configFile.set("settings.environment.major", 4);
            configFile.set("settings.environment.count.min", 1);
            configFile.set("settings.environment.count.max", 10);
            configFile.set("settings.environment.announce", true);
        }
        if (!configFile.contains("settings.protection"))
        {
            configFile.set("settings.protection.wild.use", true);
            configFile.set("settings.protection.wild.break", true);
            configFile.set("settings.protection.wild.build", false);
            configFile.set("settings.protection.wild.ignite", true);
            configFile.set("settings.protection.wild.creepers", true);
            configFile.set("settings.protection.islands.not_owned.use", true);
            configFile.set("settings.protection.islands.not_owned.break", true);
            configFile.set("settings.protection.islands.not_owned.build", true);
            configFile.set("settings.protection.islands.not_owned.ignite", false);
            configFile.set("settings.protection.islands.not_owned.creepers", false);
            configFile.set("settings.protection.islands.owned.creepers", true);
        }

        try {
            String test = configFile.getString("settings.server.placement");
            if (test.equalsIgnoreCase("auto")) this.placement = -1;
            else if (test.equalsIgnoreCase("random")) this.placement = -2;
            else this.placement = Integer.parseInt(test);
        } catch (NumberFormatException ignore) {
            this.placement = -1;
            configFile.set("settings.server.placement", "auto");
        }

        this.type = configFile.getString("settings.server.type");
        this.closest = configFile.getInt("settings.environment.distance.closest");
        this.distance_min = configFile.getInt("settings.environment.distance.min");
        this.distance_max = configFile.getInt("settings.environment.distance.max");
        if (closest < distance_max)
        {
            closest = distance_max;
            configFile.set("settings.environment.distance.max", distance_max);
        }

        this.protection_wild_use = configFile.getBoolean("settings.protection.wild.use");
        this.protection_wild_build = configFile.getBoolean("settings.protection.wild.build");
        this.protection_wild_break = configFile.getBoolean("settings.protection.wild.break");
        this.protection_wild_creepers = configFile.getBoolean("settings.protection.wild.creepers");
        this.protection_wild_ignite = configFile.getBoolean("settings.protection.wild.ignite");
        this.protection_not_owned_use = configFile.getBoolean("settings.protection.islands.not_owned.use");
        this.protection_not_owned_build = configFile.getBoolean("settings.protection.islands.not_owned.build");
        this.protection_not_owned_break = configFile.getBoolean("settings.protection.islands.not_owned.break");
        this.protection_not_owned_creepers = configFile.getBoolean("settings.protection.islands.not_owned.creepers");
        this.protection_not_owned_ignite = configFile.getBoolean("settings.protection.islands.not_owned.ignite");
        this.protection_owned_creepers = configFile.getBoolean("settings.protection.islands.owned.creepers");
        this.default_structure = configFile.getString("settings.environment.default.structure");
        this.time = configFile.getInt("settings.environment.time");
        this.decay = configFile.getLong("settings.environment.decay");
        this.announce = configFile.getBoolean("settings.environment.announce");
        this.max_islands = configFile.getInt("settings.environment.max");
        this.major = configFile.getInt("settings.environment.major");
        this.count_min = configFile.getInt("settings.environment.count.min");
        this.count_max = configFile.getInt("settings.environment.count.max");
        this.world = configFile.getString("settings.world.name");
        this.world_boarder = configFile.getBoolean("settings.world.world_boarder");
        this.x = configFile.getInt("settings.world.x");
        this.z = configFile.getInt("settings.world.z");
        this.starting = configFile.getStringList("settings.starting.structures");
        this.language = configFile.getString("settings.language");
        this.size_col = configFile.getInt("settings.starting.size.col");
        this.size_row = configFile.getInt("settings.starting.size.row");
        this.default_starting_shore = configFile.getString("settings.starting.default.shore");
        this.default_starting_inland = configFile.getString("settings.starting.default.inland");
        configFile.save();

    }

    public void setPlacement(int placement) {
        this.placement = placement;
        SettingsManager configFile = new SettingsManager(TitanIslands.instance.getName(), "config");
        if (placement > -1) configFile.set("settings.server.placement", this.placement);
        if (placement == -1) configFile.set("settings.server.placement", "auto");
        if (placement == -2) configFile.set("settings.server.placement", "random");
        configFile.save();
    }

    public long getDecay() {
        return decay;
    }

    public int getMajor() {
        return major;
    }

    public int getPlacement() {
        return placement;
    }

    public String getType() {
        return type;
    }

    public static ConfigManager getInstants() {
        return instants;
    }

    public boolean isProtection_wild_ignite() {
        return protection_wild_ignite;
    }

    public boolean isProtection_not_owned_ignite() {
        return protection_not_owned_ignite;
    }

    public boolean isProtection_wild_build() {
        return protection_wild_build;
    }

    public boolean isProtection_not_owned_build() {
        return protection_not_owned_build;
    }

    public boolean isProtection_wild_use() {
        return protection_wild_use;
    }

    public boolean isProtection_not_owned_use() {
        return protection_not_owned_use;
    }

    public int getMax_islands() {
        return max_islands;
    }

    public String getDefault_starting_shore() {
        String[] split = default_starting_shore.split(":");
        if (StructureManager.getStructure(split[0], split[1], split[2]) == null) return Objects.requireNonNull(StructureManager.getRandomStructure()).getName();
        return default_starting_shore;
    }

    public String getDefault_starting_inland() {
        String[] split = default_starting_inland.split(":");
        if (StructureManager.getStructure(split[0], split[1], split[2]) == null) return Objects.requireNonNull(StructureManager.getRandomStructure()).getName();
        return default_starting_inland;
    }

    public int getSize_col() {
        return Math.max(size_col, 1);
    }

    public int getSize_row() {
        return Math.max(size_row, 1);
    }

    public String getDefaultStructure()
    {
        return this.default_structure;
    }

    public boolean isProtection_owned_creepers() {
        return protection_owned_creepers;
    }

    public boolean isProtection_not_owned_creepers() {
        return protection_not_owned_creepers;
    }

    public boolean isProtection_not_owned_break() {
        return protection_not_owned_break;
    }

    public boolean isProtection_wild_creepers() {
        return protection_wild_creepers;
    }

    public boolean isProtection_wild_break() {
        return protection_wild_break;
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
        return new ArrayList<>(starting);
    }

}
