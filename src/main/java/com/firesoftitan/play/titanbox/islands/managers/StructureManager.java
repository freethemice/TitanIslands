package com.firesoftitan.play.titanbox.islands.managers;

import com.firesoftitan.play.titanbox.islands.TitanIslands;
import com.firesoftitan.play.titanbox.islands.enums.StructureTypeEnum;
import com.firesoftitan.play.titanbox.libs.managers.SaveManager;
import com.firesoftitan.play.titanbox.libs.managers.SettingsManager;
import org.bukkit.Location;
import org.bukkit.block.structure.Mirror;
import org.bukkit.block.structure.StructureRotation;
import org.bukkit.structure.Structure;
import org.bukkit.util.BlockVector;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;

public class StructureManager {

    private static final SaveManager structureData = new SaveManager(TitanIslands.instance.getName(), "structureData");

    public static void save()
    {
        structureData.save();
    }
    public static int getStructureCount(String name)
    {
        return structureData.getInt(name + ".count");
    }
    public static void setStructureCount(String name, int value)
    {
        structureData.set(name + ".count", value);
    }

    public static String[] getAllStructureAsArray() {
        return allStructures.keySet().toArray(new String[0]);
    }
    public static List<String> getAllStructureAsList() {
        return new ArrayList<String>(allStructures.keySet());
    }
    public static StructureManager getRandomStructure() {
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
                String[] split = name.split(":");
                StructureManager structure = StructureManager.getStructure(split[0], StructureTypeEnum.getType(split[1]), split[2]);
                if (structure != null) {
                    return structure;
                }
            }
        }
        // Pick random structure if running product approach does not work
        String randomKey = (String) nameToOdds.keySet().toArray()[random.nextInt(nameToOdds.size())];
        String[] split = randomKey.split(":");
        return StructureManager.getStructure(split[0], StructureTypeEnum.getType(split[1]), split[2]);
    }

    private static Structure load(File section)
    {
        try {
            org.bukkit.structure.StructureManager structureManager = TitanIslands.instance.getServer().getStructureManager();
            return structureManager.loadStructure(section);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    private static Location getAdjustedPlacement(Location location, StructureManager structureManager, int height)
    {
        Structure structure = load(structureManager.nbtFile);
        assert structure != null;
        BlockVector size = structure.getSize();
        Location location1 = new Location(location.getWorld(), location.getBlockX() + size.getBlockX(), location.getBlockY() + size.getBlockY(), location.getBlockZ() + size.getBlockZ());
        CubeManager selectorTool = new CubeManager(location, location1);
        Location center = selectorTool.getCenterOffset();
        Location subtract = location.clone().subtract(center);
        subtract.setY(height - structureManager.getSeaLevelOffset() );

        return subtract.clone();
    }
    public static CubeManager getPreBuild(Location location, File section)
    {
        Structure structure = load(section);
        assert structure != null;
        BlockVector size = structure.getSize();
        Location location1 = new Location(location.getWorld(), location.getBlockX() + size.getBlockX(), location.getBlockY() + size.getBlockY(), location.getBlockZ() + size.getBlockZ());
        return new CubeManager(location, location1);
    }
    public static CubeManager build(Location location, File section, StructureRotation rotation)
    {
        return build(location, section, rotation, 1);
    }
    private static CubeManager build(Location location, File section, StructureRotation rotation, float all)
    {
        Structure structure = load(section);
        assert structure != null;
        return build(location,structure,rotation,all);
    }
    private static CubeManager build(Location location, Structure structure, StructureRotation rotation)
    {
        structure.place(location,true, rotation, Mirror.NONE, -1, 1, new Random());
        BlockVector size = structure.getSize();
        Location location1 = new Location(location.getWorld(), location.getBlockX() + size.getBlockX(), location.getBlockY() + size.getBlockY(), location.getBlockZ() + size.getBlockZ());
        return new CubeManager(location, location1);
    }
    private static CubeManager build(Location location, Structure structure, StructureRotation rotation, float all)
    {
        structure.place(location,true, rotation, Mirror.NONE, -1, all, new Random());
        BlockVector size = structure.getSize();
        Location location1 = new Location(location.getWorld(), location.getBlockX() + size.getBlockX(), location.getBlockY() + size.getBlockY(), location.getBlockZ() + size.getBlockZ());
        return new CubeManager(location, location1);
    }

    @Deprecated
    public static final HashMap<String, StructureManager> oldNamingStructures = new HashMap<String, StructureManager>();
    private static final HashMap<String, StructureManager> allStructures = new HashMap<String, StructureManager>();
    private static final HashMap<String, StructureManager> shoreStructures = new HashMap<String, StructureManager>();
    private static final HashMap<String, StructureManager> inlandStructures = new HashMap<String, StructureManager>();
    private static final HashMap<String, StructureManager> mineralStructures = new HashMap<String, StructureManager>();
    private static final HashMap<String, StructureManager> structureStructures = new HashMap<String, StructureManager>();
    private static final HashMap<String, StructureManager> animalStructures = new HashMap<String, StructureManager>();
    private static final HashMap<String, StructureManager> woodStructures = new HashMap<String, StructureManager>();

    public static StructureManager getStructure(String key) {
        return allStructures.get(key);
    }
    public static StructureManager getStructure(String namespace, String section, String name) {
        return getStructure(namespace, StructureTypeEnum.getType(section), name);
    }
    public static StructureManager getStructure(String namespace, StructureTypeEnum section, String name) {
        String key = namespace + ":" + section.getName() + ":" + name;
        return getStructure(key);
    }
    public static List<StructureManager> getStructures(String namespace, StructureTypeEnum structureTypeEnum) {
        List<StructureManager> filteredList;
        switch (structureTypeEnum)
        {
            case WOOD -> filteredList = getWoodStructures();
            case ANIMAL -> filteredList = getAnimalStructures();
            case SHORE -> filteredList = getShoreStructures();
            case BUILDING -> filteredList = getBuildingStructures();
            case INLAND -> filteredList = getInlandStructures();
            case MINERAL -> filteredList = getMineralStructures();
            default -> filteredList =  new ArrayList<StructureManager>(allStructures.values());
        }
        return filterNameSpace(namespace, filteredList);
    }

    @NotNull
    private static List<StructureManager> filterNameSpace(String namespace, List<StructureManager> filteredList) {
        List<StructureManager> out = new ArrayList<StructureManager>();
        for (StructureManager key: filteredList)
        {
            if (key.getNamespace().equals(namespace)) out.add(key);
        }
        return out;
    }

    public static List<StructureManager> getStructures() {
        return new ArrayList<StructureManager>(allStructures.values());
    }
    public static List<StructureManager> getShoreStructures() {
        return new ArrayList<StructureManager>(shoreStructures.values());
    }
    public static List<StructureManager> getInlandStructures() {
        return new ArrayList<StructureManager>(inlandStructures.values());
    }

    public static List<StructureManager> getWoodStructures() {
        return new ArrayList<StructureManager>(woodStructures.values());
    }
    public static List<StructureManager> getMineralStructures() {
        return new ArrayList<StructureManager>(mineralStructures.values());
    }
    public static List<StructureManager> getBuildingStructures() {
        return new ArrayList<StructureManager>(structureStructures.values());
    }
    public static List<StructureManager> getAnimalStructures() {
        return new ArrayList<StructureManager>(animalStructures.values());
    }

    private final File ymlFile;

    private final File nbtFile;
    private final SettingsManager configManager;

    private final String name;
    private final String namespace;

    public StructureManager(String namespace, StructureTypeEnum section, String ymlName)
    {

        this.ymlFile = new File(TitanIslands.tiFilePath,  File.separator + "structures" +  File.separator + namespace +  File.separator + section.getName() +  File.separator + ymlName + ".yml");
        String jarYmlFile = "/defaults/structures/" + namespace + "/" + section.getName() + "/" + ymlName + ".yml";

        configManager = new SettingsManager(ymlFile);
        String nbtName = configManager.getString("filename");
        this.nbtFile = new File(TitanIslands.tiFilePath, File.separator + "structures" +  File.separator + namespace +  File.separator + section.getName() +  File.separator + nbtName + ".nbt");
        String jarNbtFile = "/defaults/structures/"  + namespace + "/" + section.getName() + "/"  + nbtName + ".nbt";

        name = ymlName.toLowerCase().replace(" ", "");
        this.namespace = namespace;
        String key = namespace + ":" + section.getName() + ":" + this.getName();
        oldNamingStructures.put(this.getName(), this);
        allStructures.put(key, this);
        if (!structureData.contains(key))
        {
            structureData.set(key + ".count", 0);
        }

        String type = section.getName();
        if (section == StructureTypeEnum.SHORE) shoreStructures.put(key, this);
        if (section == StructureTypeEnum.INLAND) inlandStructures.put(key, this);
        if (section == StructureTypeEnum.MINERAL) mineralStructures.put(key, this);
        if (section == StructureTypeEnum.BUILDING) structureStructures.put(key, this);
        if (section == StructureTypeEnum.ANIMAL) animalStructures.put(key, this);
        if (section == StructureTypeEnum.WOOD) woodStructures.put(key, this);

        configManager.save();
        InputStream stream = getClass().getResourceAsStream(jarYmlFile);
        if (namespace.equalsIgnoreCase("titanislands") && stream != null)
        {
            if (this.isAutoUpdate()) {
                SettingsManager settingsManager = new SettingsManager(stream);
                String version = settingsManager.getString("version");
                if (!this.getVersion().equals(version)) {
                    try {
                        if (stream != null) {
                            stream = getClass().getResourceAsStream(jarYmlFile);
                            // Copy the file from the JAR to the plugin folder
                            assert stream != null;
                            Files.copy(stream, this.ymlFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                            stream.close();
                            stream = getClass().getResourceAsStream(jarNbtFile);
                            if (stream != null) {
                                Files.copy(stream, this.nbtFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                                stream.close();
                            }
                            TitanIslands.tools.getMessageTool().sendMessageSystem(this.getName() + " has been updated from the jar");
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

    }

    public String getNamespace() {
        return namespace;
    }
    public CubeManager getPreBuild(Location location, int height)
    {
        Location finalLocation = StructureManager.getAdjustedPlacement(location, this, height);
        return StructureManager.getPreBuild(finalLocation, this.nbtFile);
    }

    public CubeManager build(Location location, int height)
    {
        Location finalLocation = StructureManager.getAdjustedPlacement(location, this, height);
        return StructureManager.build(finalLocation, this.nbtFile, StructureRotation.NONE);
    }
    public int getCost()
    {
        return configManager.getInt("cost");
    }
    public int getSeaLevelOffset()
    {
        return configManager.getInt("sealevel") - 1;
    }
    public double getHeightMap()
    {
        return configManager.getDouble("heightmap");
    }

    public StructureTypeEnum getType()
    {
        String type = configManager.getString("type").toLowerCase();
        return StructureTypeEnum.getType(type);
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

    public int getIslandLimit()
    {
        return configManager.getInt("limits.island");
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
    public Boolean isAutoUpdate()
    {
        return configManager.getBoolean("autoupdate");
    }
    public String getName() {
        return this.name;
    }
    public File getYmlFile() {
        return ymlFile;
    }
}
