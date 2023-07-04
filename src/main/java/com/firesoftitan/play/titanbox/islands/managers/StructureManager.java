package com.firesoftitan.play.titanbox.islands.managers;

import com.firesoftitan.play.titanbox.islands.TitanIslands;
import com.firesoftitan.play.titanbox.islands.tools.CubeSelectorTool;
import com.firesoftitan.play.titanbox.libs.TitanBoxLibs;
import com.firesoftitan.play.titanbox.libs.managers.SaveManager;
import com.firesoftitan.play.titanbox.libs.managers.SettingsManager;
import org.bukkit.Location;
import org.bukkit.block.structure.Mirror;
import org.bukkit.block.structure.StructureRotation;
import org.bukkit.structure.Structure;
import org.bukkit.util.BlockVector;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;

public class StructureManager {
    private static Structure load(File section)
    {
        try {
            org.bukkit.structure.StructureManager structureManager = TitanIslands.instance.getServer().getStructureManager();
            Structure structure = structureManager.loadStructure(section);
            return structure;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    private static Location getAdjustedPlacement(Location location, String name)
    {
        StructureManager structure1 = StructureManager.getStructure(name);
        Structure structure = load(structure1.nbtFile);
        BlockVector size = structure.getSize();
        Location location1 = new Location(location.getWorld(), location.getBlockX() + size.getBlockX(), location.getBlockY() + size.getBlockY(), location.getBlockZ() + size.getBlockZ());
        CubeSelectorTool selectorTool = new CubeSelectorTool(location, location1);
        Location center = selectorTool.getCenterOffset();
        Location subtract = location.clone().subtract(center);
        int highest = location.getWorld().getHighestBlockYAt(subtract);
        subtract.setY(highest - structure1.getSeaLevelOffset() );

        return subtract.clone();
    }
    public static CubeSelectorTool getPreBuild(Location location, File section)
    {
        Structure structure = load(section);
        BlockVector size = structure.getSize();
        Location location1 = new Location(location.getWorld(), location.getBlockX() + size.getBlockX(), location.getBlockY() + size.getBlockY(), location.getBlockZ() + size.getBlockZ());
        CubeSelectorTool selectorTool = new CubeSelectorTool(location, location1);
        return selectorTool;
    }
    public static CubeSelectorTool build(Location location, File section, StructureRotation rotation)
    {
        return build(location, section, rotation, 1);
    }
    private static CubeSelectorTool build(Location location, File section, StructureRotation rotation, float all)
    {
        Structure structure = load(section);
        return build(location,structure,rotation,all);
    }
    private static CubeSelectorTool build(Location location, Structure structure, StructureRotation rotation)
    {
        structure.place(location,true, rotation, Mirror.NONE, -1, 1, new Random());
        BlockVector size = structure.getSize();
        Location location1 = new Location(location.getWorld(), location.getBlockX() + size.getBlockX(), location.getBlockY() + size.getBlockY(), location.getBlockZ() + size.getBlockZ());
        CubeSelectorTool selectorTool = new CubeSelectorTool(location, location1);
        return selectorTool;
    }
    private static CubeSelectorTool build(Location location, Structure structure, StructureRotation rotation, float all)
    {
        structure.place(location,true, rotation, Mirror.NONE, -1, all, new Random());
        BlockVector size = structure.getSize();
        Location location1 = new Location(location.getWorld(), location.getBlockX() + size.getBlockX(), location.getBlockY() + size.getBlockY(), location.getBlockZ() + size.getBlockZ());
        CubeSelectorTool selectorTool = new CubeSelectorTool(location, location1);
        return selectorTool;
    }
    private static HashMap<String, StructureManager> allStructures = new HashMap<String, StructureManager>();

    public static StructureManager getStructure(String name) {
        return allStructures.get(name);
    }
    public static List<String> getStructures() {
        List<String> stringList = new ArrayList<String>(allStructures.keySet());
        return stringList;
    }
    private String name;
    private File nbtFile;
    private File ymlFile;
    private SettingsManager configManager;
    private String jarNbtFile;
    private String jarYmlFile;
    public StructureManager(String name)
    {
        this.name = name;
        this.nbtFile = new File(TitanIslands.tiFilePath,  File.separator + "structures" +  File.separator + name + ".nbt");
        this.ymlFile = new File(TitanIslands.tiFilePath,  File.separator + "structures" +  File.separator + name + ".yml");
        this.jarNbtFile = "/defaults/structures/" + name + ".nbt";
        this.jarYmlFile = "/defaults/structures/" + name + ".yml";
        boolean ymlExists = this.ymlFile.exists();
        configManager = new SettingsManager(ymlFile);
        allStructures.put(this.name, this);


        InputStream stream = getClass().getResourceAsStream(this.jarYmlFile);
        System.out.println(stream + "/" + ymlExists);
        if (stream == null && !ymlExists)
        {
            TitanIslands.tools.getMessageTool().sendMessageSystem("New File detected");
            stream = getClass().getResourceAsStream("/defaults/structures/sample.yml");
            SettingsManager settingsManager = new SettingsManager(stream);
            settingsManager.set("name", this.name);
            settingsManager.convertToFile(this.ymlFile);
            settingsManager.save();
            configManager.reload();
            TitanIslands.tools.getMessageTool().sendMessageSystem(this.name + " yml has been generated for new file.");
        }else if (stream != null)
        {
            if (this.isAutoUpdate()) {
                SettingsManager settingsManager = new SettingsManager(stream);
                String version = settingsManager.getString("version");
                if (!this.getVersion().equals(version)) {
                    try {
                        if (stream != null) {
                            stream = getClass().getResourceAsStream(this.jarYmlFile);
                            // Copy the file from the JAR to the plugin folder
                            Files.copy(stream, this.ymlFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                            stream.close();
                            stream = getClass().getResourceAsStream(this.jarNbtFile);
                            if (stream != null) {
                                Files.copy(stream, this.nbtFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                                stream.close();
                            }
                            TitanIslands.tools.getMessageTool().sendMessageSystem(this.name + " has been updated from the jar");
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

    }
    public CubeSelectorTool getPreBuild(Location location)
    {
        Location finalLocation = StructureManager.getAdjustedPlacement(location, name);
        CubeSelectorTool build = StructureManager.getPreBuild(finalLocation, this.nbtFile);
        return build;
    }
    public CubeSelectorTool build(Location location)
    {
        Location finalLocation = StructureManager.getAdjustedPlacement(location, name);
        CubeSelectorTool build = StructureManager.build(finalLocation, this.nbtFile, StructureRotation.NONE);
        return build;
    }
    public int getSeaLevelOffset()
    {
        return configManager.getInt("sealevel") - 1;
    }
    public String getTitle()
    {
        return configManager.getString("name");
    }
    public String getVersion()
    {
        String version = configManager.getString("version");
        if (version == null) version = "0";
        return version;
    }
    public Boolean isEnabled()
    {
        return configManager.getBoolean("enabled");
    }
    public Boolean isAutoUpdate()
    {
        return configManager.getBoolean("autoupdate");
    }
    public String getName() {
        return name;
    }

    public File getNbtFile() {
        return nbtFile;
    }

    public File getYmlFile() {
        return ymlFile;
    }
}
