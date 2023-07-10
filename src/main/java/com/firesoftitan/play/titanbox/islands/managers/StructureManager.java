package com.firesoftitan.play.titanbox.islands.managers;

import com.firesoftitan.play.titanbox.islands.TitanIslands;
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

    public static StructureManager getRandomIsland() {
        Map<String, Integer> nameToOdds = new HashMap<>();
        for (String key : allStructures.keySet()) {
            StructureManager structure = allStructures.get(key);
            if (structure.getOdds() > 0) nameToOdds.put(key, structure.getOdds());
        }
        if (nameToOdds.isEmpty()) {
            return null;
        }
        Map<Integer, Integer> oddsCounts = new HashMap<>();
        for (int odd : nameToOdds.values()) {
            oddsCounts.put(odd, oddsCounts.getOrDefault(odd, 0) + 1);
        }
        List<String> keys = new ArrayList<>(nameToOdds.keySet());
        Collections.shuffle(keys);  // Fix to shuffle keys

        Random random = new Random();
        int totalOdds = 0;
        for (int odd : oddsCounts.keySet()) {
            totalOdds += odd * oddsCounts.get(odd);
        }

        float runningProduct = 1;

        for (String name : keys) {
            float randomFloat = random.nextFloat();
            int odd = nameToOdds.get(name);

            float oddProbability = (float) odd / totalOdds;
            runningProduct *= 1 - oddProbability;
            if (randomFloat < runningProduct) {
                StructureManager structure = StructureManager.getStructure(name);
                if (structure != null) {
                    return structure;
                }
            }
        }
        // Pick random structure if running product approach does not work
        String randomKey = (String) nameToOdds.keySet().toArray()[random.nextInt(nameToOdds.size())];
        return StructureManager.getStructure(randomKey);
    }

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
        CubeSelectorManager selectorTool = new CubeSelectorManager(location, location1);
        Location center = selectorTool.getCenterOffset();
        Location subtract = location.clone().subtract(center);
        int highest = location.getWorld().getHighestBlockYAt(subtract);
        subtract.setY(highest - structure1.getSeaLevelOffset() );

        return subtract.clone();
    }
    public static CubeSelectorManager getPreBuild(Location location, File section)
    {
        Structure structure = load(section);
        BlockVector size = structure.getSize();
        Location location1 = new Location(location.getWorld(), location.getBlockX() + size.getBlockX(), location.getBlockY() + size.getBlockY(), location.getBlockZ() + size.getBlockZ());
        CubeSelectorManager selectorTool = new CubeSelectorManager(location, location1);
        return selectorTool;
    }
    public static CubeSelectorManager build(Location location, File section, StructureRotation rotation)
    {
        return build(location, section, rotation, 1);
    }
    private static CubeSelectorManager build(Location location, File section, StructureRotation rotation, float all)
    {
        Structure structure = load(section);
        return build(location,structure,rotation,all);
    }
    private static CubeSelectorManager build(Location location, Structure structure, StructureRotation rotation)
    {
        structure.place(location,true, rotation, Mirror.NONE, -1, 1, new Random());
        BlockVector size = structure.getSize();
        Location location1 = new Location(location.getWorld(), location.getBlockX() + size.getBlockX(), location.getBlockY() + size.getBlockY(), location.getBlockZ() + size.getBlockZ());
        CubeSelectorManager selectorTool = new CubeSelectorManager(location, location1);
        return selectorTool;
    }
    private static CubeSelectorManager build(Location location, Structure structure, StructureRotation rotation, float all)
    {
        structure.place(location,true, rotation, Mirror.NONE, -1, all, new Random());
        BlockVector size = structure.getSize();
        Location location1 = new Location(location.getWorld(), location.getBlockX() + size.getBlockX(), location.getBlockY() + size.getBlockY(), location.getBlockZ() + size.getBlockZ());
        CubeSelectorManager selectorTool = new CubeSelectorManager(location, location1);
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


    private String filename;
    private File nbtFile;
    private File ymlFile;
    private SettingsManager configManager;
    private String jarNbtFile;
    private String jarYmlFile;
    public StructureManager(String filename)
    {
        this.filename = filename;
        this.nbtFile = new File(TitanIslands.tiFilePath,  File.separator + "structures" +  File.separator + filename + ".nbt");
        this.ymlFile = new File(TitanIslands.tiFilePath,  File.separator + "structures" +  File.separator + filename + ".yml");
        this.jarNbtFile = "/defaults/structures/" + filename + ".nbt";
        this.jarYmlFile = "/defaults/structures/" + filename + ".yml";
        boolean ymlExists = this.ymlFile.exists();
        configManager = new SettingsManager(ymlFile);
        allStructures.put(configManager.getString("name").toLowerCase(), this);


        InputStream stream = getClass().getResourceAsStream(this.jarYmlFile);
        if (stream == null && !ymlExists)
        {
            TitanIslands.tools.getMessageTool().sendMessageSystem("New File detected");
            stream = getClass().getResourceAsStream("/defaults/structures/sample.yml");
            SettingsManager settingsManager = new SettingsManager(stream);
            settingsManager.set("name", this.filename);
            settingsManager.set("title", TitanIslands.tools.getFormattingTool().fixCapitalization(this.filename));
            settingsManager.convertToFile(this.ymlFile);
            settingsManager.save();
            configManager.reload();
            TitanIslands.tools.getMessageTool().sendMessageSystem(this.filename + " yml has been generated for new file.");
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
                            TitanIslands.tools.getMessageTool().sendMessageSystem(this.filename + " has been updated from the jar");
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

    }
    public CubeSelectorManager getPreBuild(Location location)
    {
        Location finalLocation = StructureManager.getAdjustedPlacement(location, filename);
        CubeSelectorManager build = StructureManager.getPreBuild(finalLocation, this.nbtFile);
        return build;
    }
    public CubeSelectorManager build(Location location)
    {
        Location finalLocation = StructureManager.getAdjustedPlacement(location, filename);
        CubeSelectorManager build = StructureManager.build(finalLocation, this.nbtFile, StructureRotation.NONE);
        return build;
    }
    public int getSeaLevelOffset()
    {
        return configManager.getInt("sealevel") - 1;
    }
    public int getOdds()
    {
        int odds = configManager.getInt("odds");
        if (odds < 0) odds = 0;
        if (odds > 100) odds = 100;
        if (odds != configManager.getInt("odds")) {
            configManager.set("odds", odds);
            configManager.save();
        }
        return odds;
    }
    public String getTitle()
    {
        return configManager.getString("title");
    }
    public int getSpawnLimit()
    {
        return configManager.getInt("limits.spawn");
    }
    public int getPersonalLimit()
    {
        return configManager.getInt("limits.personal");
    }
    public String getVersion()
    {
        String version = configManager.getString("version");
        if (version == null) version = "0";
        return version;
    }
    public Boolean isUnlockable()
    {
        return configManager.getBoolean("unlockable");
    }
    public Boolean isAutoUpdate()
    {
        return configManager.getBoolean("autoupdate");
    }
    public String getName() {
        return configManager.getString("name").toLowerCase().replace(" ", "");
    }

    public File getNbtFile() {
        return nbtFile;
    }

    public File getYmlFile() {
        return ymlFile;
    }
}
