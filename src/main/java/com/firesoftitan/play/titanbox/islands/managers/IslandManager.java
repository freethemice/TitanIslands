package com.firesoftitan.play.titanbox.islands.managers;

import com.firesoftitan.play.titanbox.islands.TitanIslands;
import com.firesoftitan.play.titanbox.islands.enums.StructureTypeEnum;
import com.firesoftitan.play.titanbox.islands.runnables.IslandMakerRunnable;
import com.firesoftitan.play.titanbox.islands.tools.IslandGeneratorInfo;
import com.firesoftitan.play.titanbox.libs.managers.SaveManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.*;

public class IslandManager {
    private static final Map<UUID, IslandManager> islands = new HashMap<UUID, IslandManager>();

    public static void removeIsland(IslandManager islandManager)
    {
        islands.remove(islandManager.getId());
        islandSaves.delete(islandManager.getId().toString());
    }
    private static void getIslandBounds()
    {
        List<IslandManager> islandManagers = new ArrayList<>(IslandManager.islands.values());
        for (IslandManager islandManager : islandManagers) {
            double minX = Double.MAX_VALUE;
            double minY = Double.MAX_VALUE;
            double minZ = Double.MAX_VALUE;

            double maxX = -Double.MAX_VALUE;
            double maxY = -Double.MAX_VALUE;
            double maxZ = -Double.MAX_VALUE;

            for (CubeManager cube : islandManager.getCubes()) {
                Location minLoc = cube.getFirstCorner();
                Location maxLoc = cube.getSecondCorner();

                minX = Math.min(minX, minLoc.getX());
                minY = Math.min(minY, minLoc.getY());
                minZ = Math.min(minZ, minLoc.getZ());

                maxX = Math.max(maxX, maxLoc.getX());
                maxY = Math.max(maxY, maxLoc.getY());
                maxZ = Math.max(maxZ, maxLoc.getZ());
            }
            Location firstCorner = new Location(islandManager.getLocation().getWorld(), minX, minY, minZ);
            Location secondCorner = new Location(islandManager.getLocation().getWorld(), maxX, maxY, maxZ);
        }
    }

    public static Boolean isSafeSpawnLocation(Location location)
    {
        List<IslandManager> islandManagers = new ArrayList<>(IslandManager.islands.values());
        for (IslandManager islandManager : islandManagers) {
            if (islandManager.getLocation().distance(location) < ConfigManager.getInstants().getDistance_min()) return false;
        }
        return true;
    }

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

    private static final Random random = new Random(System.currentTimeMillis());

    public static int getCount()
    {
        return islands.size();
    }
    public static void generateIsland(Location location)
    {
        generateIsland(null, location);
    }
    public static void generateIsland(Player player, Location location)
    {
        String[][] island;
        if (player == null) island = generateRandomIsland();
        else {
            List<String> starting = ConfigManager.instants.getStarting();
            int size_col = ConfigManager.instants.getSize_col();
            int size_row = ConfigManager.instants.getSize_row();
            island= new String[size_col][size_row];
            for (int row = 0; row < size_row; row++) {
                for (int col = 0; col < size_col; col++) {
                    island[row][col] = ConfigManager.instants.getDefault_starting_shore();
                    if ((row != 0 && row != size_row - 1) & (col != 0 && col != size_col - 1)) {
                        if (!starting.isEmpty()) {
                            island[row][col] = starting.get(0);
                            starting.remove(0);
                        }
                        else
                        {
                            island[row][col] = ConfigManager.instants.getDefault_starting_inland();
                        }
                    }
                }
            }
        }

        IslandMakerRunnable islandMakerRunnable = new IslandMakerRunnable(player, location, island);
        islandMakerRunnable.runTaskTimer(TitanIslands.instance, 1, 7);

    }
    private static String[][] generateRandomIsland() {
        int islandWidth = random.nextInt(ConfigManager.instants.getCount_max()) + ConfigManager.instants.getCount_min(); // Random width from 1 to 10
        int islandHeight = random.nextInt(ConfigManager.instants.getCount_max()) + ConfigManager.instants.getCount_min(); // Random height from 1 to 10
        return generateRandomIsland(islandWidth, islandHeight);
    }
    private static String[][] generateRandomIsland(int islandWidth, int islandHeight) {

        double frequency = 0.05; // Adjust the frequency for the Perlin noise
        IslandGeneratorInfo info = new IslandGeneratorInfo(islandWidth, islandHeight);
        // Generate the Perlin noise for the island
        double[][] islandNoise = generatePerlinNoise(islandWidth, islandHeight, frequency);

        String[][] island = new String[islandWidth][islandHeight];

        List<String> inlandWords = convertStructureToWords(StructureManager.getInlandStructures());
        List<String> shoreWords =convertStructureToWords(StructureManager.getShoreStructures());
        List<String> mineralWords =convertStructureToWords(StructureManager.getMineralStructures());
        List<String> animalWords = convertStructureToWords(StructureManager.getAnimalStructures());
        List<String> buildinglWords = convertStructureToWords(StructureManager.getBuildingStructures());


        boolean animals = false;
        boolean structure = false;
        if (islandWidth > 4 && islandHeight > 4) inlandWords.addAll(mineralWords);
        if (islandWidth > 5 && islandHeight > 5) animals = true;
        if (islandWidth > 5 && islandHeight > 5) structure = true;
        String woodType = getWoodTypeKey();


        for (int row = 0; row < islandWidth; row++) {
            for (int col = 0; col < islandHeight; col++) {
                double randomValue = islandNoise[row][col];
                info.setCurrentCol(col);
                info.setCurrentRow(row);
                String selectedWord;

                if ((row == 0 || row == islandWidth - 1) || (col == 0 || col == islandHeight - 1))
                {
                    selectedWord = getRandomWordFromList(shoreWords, randomValue, info);
                }
                else {
                    List<String> correctedList = new ArrayList<String>(inlandWords);
                    if (animals && random.nextDouble() > 0.4D) correctedList.addAll(animalWords);
                    if (structure && random.nextDouble() > 0.2D) correctedList.addAll(buildinglWords);
                    selectedWord = getRandomWordFromList(correctedList, randomValue, info);
                }
                if (selectedWord != null) {
                    String[] split = selectedWord.split(":");
                    if (StructureManager.getStructure(split[0], split[1], split[2]).getType() == StructureTypeEnum.ANIMAL) info.setAnimalCount(info.getAnimalCount() + 1);
                    if (StructureManager.getStructure(split[0], split[1], split[2]).getType() == StructureTypeEnum.BUILDING) info.setBuildingCount(info.getBuildingCount() + 1);
                    animals = info.canAddMoreAnimals();
                    structure = info.canAddMoreBuilding();
                    if (StructureManager.getStructure(split[0], split[1], split[2]).getType() == StructureTypeEnum.WOOD) selectedWord = woodType;

                    int structureCount = StructureManager.getStructureCount(selectedWord);
                    int max = StructureManager.getStructure(split[0], split[1], split[2]).getSpawnLimit();
                    if (structureCount >= max && max > -1)
                    {
                        col--;
                        continue;
                    }
                    else
                    {
                        int structureCount2 = info.getStructureCount(selectedWord);
                        int max2 = StructureManager.getStructure(split[0], split[1], split[2]).getIslandLimit();
                        if (structureCount2 >= max2 && max2 > -1)
                        {
                            col--;
                            continue;
                        }
                        else {
                            StructureManager.setStructureCount(selectedWord, structureCount + 1);//global count
                            info.setStructureCount(selectedWord, structureCount2 + 1);//temp island generator count
                        }
                    }

                    island[row][col] = selectedWord;
                } else {
                    // Place default element (e.g., water, sand, etc.) on the island
                    island[row][col] = ConfigManager.instants.getDefaultStructure();
                }
            }
        }

        return island;
    }
    private static List<String> convertStructureToWords(List<StructureManager> structureManagers)
    {
        List<String> inlandWords = new ArrayList<String>();
        for (StructureManager ssw: structureManagers)
        {
            inlandWords.add(ssw.getNamespace() + ":" + ssw.getType().getName() + ":" + ssw.getName());
        }
        return inlandWords;
    }
    private static String getWoodTypeKey() {

        List<String> structures = convertStructureToWords(StructureManager.getWoodStructures());

        // Get total odds
        int totalOdds = 0;
        for (String s : structures) {
            String[] split = s.split(":");

            totalOdds += StructureManager.getStructure(split[0], split[1], split[2]).getOdds();
        }

        // Pick random number between 0 and total odds
        int random = new Random().nextInt(totalOdds);

        // Loop through structures until random falls into odds range
        int odds = 0;
        for (String s : structures) {
            String[] split = s.split(":");
            odds += StructureManager.getStructure(split[0], split[1], split[2]).getOdds();
            if (random < odds) {
                return s;
            }
        }

        return "oak";
    }
    // Helper method to get a random word from a list based on spawn odds and height
    private static String getRandomWordFromList(List<String> words, double heightMapKey, IslandGeneratorInfo info) {
        List<String> validWords = new ArrayList<>();
        String defaultWord = null;
        double defaultOdds = 0;
        for (String word : words) {
            String[] split = word.split(":");
            StructureManager structure = StructureManager.getStructure(split[0], split[1], split[2]);
            double height = structure.getHeightMap();
            double spawnOddsValue = structure.getOdds();
            if (defaultWord == null || spawnOddsValue > defaultOdds) {
                defaultWord = word;
                defaultOdds = spawnOddsValue;
            }
            if (Math.random() < spawnOddsValue / 100.0) {
                if (structure.getType() == StructureTypeEnum.MINERAL
                        || structure.getType() == StructureTypeEnum.BUILDING
                        || structure.getType() == StructureTypeEnum.ANIMAL) {
                    // Ensure the selected word doesn't touch shore words for mineral and structure types
                    if (!touchesShore(info)) {
                        validWords.add(word);
                    }
                } else validWords.add(word);
            }
        }

        // Return a random word from the valid words list
        if (!validWords.isEmpty()) {
            // Use heightMapKey to influence random selection
            double weight = 1.0 - Math.abs(heightMapKey - 0.5) * 2;
            int randomIndex = new Random().nextInt(validWords.size());
            randomIndex = (int)Math.floor(randomIndex * weight);
            return validWords.get(randomIndex);
        }
        return defaultWord;
    }
    // Helper method to check if a word touches the shore (checks 8 neighboring positions)
    private static boolean touchesShore(IslandGeneratorInfo info) {


        int[] dx = {-1, -1, -1, 0, 0, 1, 1, 1};
        int[] dy = {-1, 0, 1, -1, 1, -1, 0, 1};

        for (int i = 0; i < dx.length; i++) {
            int newX = info.getCurrentCol() + dx[i];
            int newY = info.getCurrentRow() + dy[i];

            if (newX >= 0 && newX < info.getIslandWidth() && newY >= 0 && newY < info.getIslandHeight()) {
                if ((newX == 0 || newX == info.getIslandWidth() - 1) || (newY == 0 || newY == info.getIslandHeight() - 1))
                {
                    return true;
                }
            }
        }

        return false;
    }



    private static int customMod(int a, int b) {
        return (a % b + b) % b;
    }

    private static double[][] generatePerlinNoise(int width, int height, double frequency) {
        double[][] noise = new double[width][height];
        Random random = new Random();

        int octaves = 8; // Increase the number of octaves for more detail
        double persistence = 0.6; // Adjust the persistence for smoother transitions

        // Generate random gradients for the Perlin noise
        double[][][] gradients = new double[width][height][2];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                double angle = random.nextDouble() * 2 * Math.PI;
                gradients[i][j][0] = Math.cos(angle);
                gradients[i][j][1] = Math.sin(angle);
            }
        }

        // Calculate the center of the island
        int centerX = width / 2;
        int centerY = height / 2;

        // Generate Perlin noise
        for (int octave = 0; octave < octaves; octave++) {
            double amplitude = Math.pow(persistence, octave);
            double wavelength = Math.pow(frequency, octave);

            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
                    double x = i * wavelength;
                    double y = j * wavelength;

                    int x0 = (int) Math.floor(x);
                    int y0 = (int) Math.floor(y);
                    int x1 = x0 + 1;
                    int y1 = y0 + 1;

                    double dx0 = x - x0;
                    double dy0 = y - y0;
                    double dx1 = x - x1;
                    double dy1 = y - y1;

                    double dot00 = gradients[customMod(x0, width)][customMod(y0, height)][0] * dx0 + gradients[customMod(x0, width)][customMod(y0, height)][1] * dy0;
                    double dot01 = gradients[customMod(x0, width)][customMod(y1, height)][0] * dx0 + gradients[customMod(x0, width)][customMod(y1, height)][1] * dy1;
                    double dot10 = gradients[customMod(x1, width)][customMod(y0, height)][0] * dx1 + gradients[customMod(x1, width)][customMod(y0, height)][1] * dy0;
                    double dot11 = gradients[customMod(x1, width)][customMod(y1, height)][0] * dx1 + gradients[customMod(x1, width)][customMod(y1, height)][1] * dy1;

                    double wx = (3 - 2 * dx0) * dx0 * dx0;
                    double wy = (3 - 2 * dy0) * dy0 * dy0;

                    // Calculate distance from the center
                    double distanceFromCenter = Math.sqrt((i - centerX) * (i - centerX) + (j - centerY) * (j - centerY));
                    double normalizedDistance = distanceFromCenter / Math.max(centerX, centerY); // Normalize distance to [0, 1]

                    // Adjust the noise value using distance from the center and amplitude
                    double adjustedNoise = (wx * dot00 + (1 - wx) * dot10) * (1 - wy) + (wx * dot01 + (1 - wx) * dot11) * wy;
                    noise[i][j] += adjustedNoise * amplitude * normalizedDistance; // Use distance as a factor
                }
            }
        }

        // Normalize the noise values
        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (noise[i][j] < min) {
                    min = noise[i][j];
                }
                if (noise[i][j] > max) {
                    max = noise[i][j];
                }
            }
        }

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                // Normalize the values between 0 and 1
                noise[i][j] = (noise[i][j] - min) / (max - min);
            }
        }

        return noise;
    }

    public static IslandManager getRandomExcluding(Player player)
    {
        List<IslandManager> islandManagers = new ArrayList<>(IslandManager.islands.values());

        islandManagers.removeIf(selector -> PlayerManager.instants.isOwnedByPlayer(player, selector));

        if(islandManagers.isEmpty()) return null;

        int randomIndex = random.nextInt(islandManagers.size());

        return islandManagers.get(randomIndex);

    }
    public static IslandManager getClosestExcluding(Location location, Player player)
    {
        List<IslandManager> islandManagers = new ArrayList<>(IslandManager.islands.values());
        islandManagers.removeIf(selector -> PlayerManager.instants.isOwnedByPlayer(player, selector));
        if(islandManagers.isEmpty()) return null;
        return islandManagers.stream().min(Comparator.comparingDouble(island -> island.getLocation().distance(location))).orElse(null);
    }
    public static List<IslandManager> getSurrounding(Location location)
    {
        List<IslandManager> cubes = new ArrayList<>();

        for (IslandManager island : IslandManager.islands.values()) {
            Location cubeCenter = island.getLocation();
            if (cubeCenter == null) continue;
            if (location.distance(cubeCenter) <= ConfigManager.instants.getDistance_max()) {
                cubes.add(island);
            }
        }
        return cubes;
    }
    public static IslandManager getClosest(Location location)
    {
        List<IslandManager> cubes = new ArrayList<>();

        for (IslandManager island : IslandManager.islands.values()) {
            Location cubeCenter = island.getLocation();
            if (cubeCenter == null) continue;
            if (location.distance(cubeCenter) <= ConfigManager.instants.getDistance_max()) {
                cubes.add(island);
            }
        }
        return cubes.stream().min(Comparator.comparingDouble(island -> island.getLocation().distance(location))).orElse(null);
    }
    public static IslandManager getNewest() {
        return getNewest(false);
    }
    public static IslandManager getNewest(boolean UnownedOnly) {

        List<IslandManager> islands = new ArrayList<>();
        if (UnownedOnly) {
            for (IslandManager island : IslandManager.islands.values()) {
                if (island.getOwner() == null) {
                    islands.add(island);
                }
            }
        }
        else {
            islands = new ArrayList<>(IslandManager.islands.values());
        }
        return islands.stream()
                .min(Comparator.comparingLong(islandM -> Math.abs(islandM.getCreatedTime() - System.currentTimeMillis())))
                .orElse(null);

    }
    public static IslandManager getOldest()
    {
        return getOldest(false);
    }

    public static IslandManager getOldest(boolean UnownedOnly) {

        List<IslandManager> islands = new ArrayList<>();
        if (UnownedOnly) {
            for (IslandManager island : IslandManager.islands.values()) {
                if (island.getOwner() == null) {
                    islands.add(island);
                }
            }
        }
        else {
            islands = new ArrayList<>(IslandManager.islands.values());
        }
        return islands.stream()
                .max(Comparator.comparingLong(islandM -> Math.abs(islandM.getCreatedTime() - System.currentTimeMillis())))
                .orElse(null);

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
    private final Long created_time;
    private final Map<UUID, CubeManager> cubes = new HashMap<UUID, CubeManager>();
    private final List<UUID> friends;
    private final int height;
    public IslandManager() {

        this.id = generateID();
        this.created_time = System.currentTimeMillis();
        IslandManager.islands.put(this.id, this);
        this.friends = new ArrayList<UUID>();
        this.height = getPlacementHeight();
    }

    private static int getPlacementHeight() {
        Random random = new Random(System.currentTimeMillis());
        int placement = ConfigManager.getInstants().getPlacement();

        if (placement == -1) {
            World world = Objects.requireNonNull(ConfigManager.getInstants().getWorld());

            while (ConfigManager.getInstants().getPlacement() == -1) {

                int x = random.nextInt(2000) - 1000;
                int z = random.nextInt(2000) - 1000;

                int h = world.getHighestBlockYAt(x, z);
                Location randomLocation = world.getSpawnLocation().clone().add(x, 0, z);
                randomLocation.setY(h);
                if (world.getBlockAt(randomLocation).getType() == Material.WATER) {
                    ConfigManager.getInstants().setPlacement(world.getHighestBlockYAt(randomLocation));
                }
                if (world.getBlockAt(randomLocation).getType() == Material.AIR)
                {
                    ConfigManager.getInstants().setPlacement(-2);
                }
            }
        }

        int height = ConfigManager.getInstants().getPlacement();

        if (placement == -2) height = random.nextInt(201);
        return height;
    }
    public UUID getOwner()
    {
        return PlayerManager.instants.getOwner(this);
    }
    public IslandManager(SaveManager saveManager) {
        this.id = saveManager.getUUID("id");
        this.created_time = saveManager.getLong("time");
        this.friends = saveManager.getUUIDList("friends");
        if (saveManager.contains("height")) this.height = saveManager.getInt("height");
        else this.height = getPlacementHeight();

        IslandManager.islands.put(this.id, this);
    }
    public SaveManager save()
    {
        SaveManager saveManager = new SaveManager();
        saveManager.set("id", this.id);
        saveManager.set("time", this.created_time);
        saveManager.set("friends", this.friends);
        saveManager.set("height", this.height);
        return saveManager;
    }

    public List<UUID> getFriends()
    {
        return new ArrayList<UUID>(friends);
    }
    public boolean isFriend(Player player) {
        return friends.contains(player.getUniqueId());
    }
    public void addFriend(Player player)
    {
        friends.add(player.getUniqueId());
    }
    public void removeFriend(Player player)
    {
        friends.remove(player.getUniqueId());
    }
    public Location getLocation() {
        if (location == null) return null;
        return location.clone();
    }

    public UUID getId() {
        return id;
    }
    public long getCreatedTime() {
        return created_time;
    }

    public void add(CubeManager cubeManager)
    {
        if (cubes.isEmpty()) location = cubeManager.getCenter().clone();
        cubes.put(cubeManager.getId(), cubeManager);
    }

    public int getHeight() {
        return height;
    }

    public List<CubeManager> getCubes()
    {
        return new ArrayList<CubeManager>(this.cubes.values());
    }
    public void removeCube(CubeManager cubeManager)
    {
        this.cubes.remove(cubeManager.getId());
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
