package com.firesoftitan.play.titanbox.islands.managers;

import com.firesoftitan.play.titanbox.islands.TitanIslands;
import com.firesoftitan.play.titanbox.libs.managers.SaveManager;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import java.util.*;

public class CubeManager {
    private static final HashMap<UUID, CubeManager> cubes = new HashMap<UUID, CubeManager>();
    private static final int MAX_CHECK_OFFSET = 250;
    private static final int[] X_OFFSETS = {1, -1};
    private static final int[] Z_OFFSETS = {1, -1};
    private static final Random random = new Random(System.currentTimeMillis());
    /**
     * Checks if the given location overlaps with the structure for the given key.
     * If it overlaps, it finds the first valid non-overlapping location within
     * MAX_CHECK_OFFSET blocks.
     *
     * @param structureKey The key of the structure to check
     * @param locationToCheck The location to check for overlap
     * @return The first valid non-overlapping location, or the original location
     *         if none found within MAX_CHECK_OFFSET
     */
    public static Location adjustLocation(String structureKey, Location locationToCheck) {
        boolean useXOffset = random.nextBoolean();
        int offsetIndex = random.nextInt(2);  // Either 0 or 1
        int xOffset = useXOffset ? X_OFFSETS[offsetIndex] : 0;
        int zOffset = useXOffset ? 0 : Z_OFFSETS[offsetIndex];
        BlockFace blockFace = null;
        if (xOffset == -1) blockFace = BlockFace.WEST;
        else if (xOffset == 1) blockFace = BlockFace.EAST;
        else if (zOffset == -1) blockFace = BlockFace.NORTH;
        else if (zOffset == 1) blockFace = BlockFace.SOUTH;
        return adjustLocation(structureKey, locationToCheck, blockFace);
    }
    /**
     * Checks if the given location overlaps with the structure for the given key.
     * If it overlaps, it finds the first valid non-overlapping location within
     * MAX_CHECK_OFFSET blocks.
     *
     * @param structureKey The key of the structure to check
     * @param locationToCheck The location to check for overlap
     * @param direction The direction in which to search for a free place
     * @return The first valid non-overlapping location, or the original location
     *         if none found within MAX_CHECK_OFFSET
     */

    public static Location adjustLocation(String structureKey, Location locationToCheck, BlockFace direction)
    {
        int xOffset = direction.getModX();
        int zOffset = direction.getModZ();
        StructureManager structure = StructureManager.getStructure(structureKey);
        if (structure == null) return null;
        for (int i = 0; i < MAX_CHECK_OFFSET; i++) {
            Location newLocation = locationToCheck.clone().add(i*xOffset, 0, i*zOffset);
            CubeManager build = structure.getPreBuild(newLocation);
            if (!CubeManager.isOverlapping(build)) {
                return newLocation;
            }
            //useXOffset = !useXOffset;  // Alternate between x and z
        }
        return locationToCheck;
    }
    public static void placeCube(CubeManager cubeManager)
    {
        cubes.put(cubeManager.getId(), cubeManager);
    }
    /**
     * Checks if two cubes represented by CubeSelectorManager objects intersect.
     *
     * @param cube1 The first cube to check
     * @param cube2 The second cube to check
     * @return True if the cubes intersect, false otherwise
     */
    public static boolean isInCube(CubeManager cube1, CubeManager cube2) {
        // Get the coordinates of the two points defining the first cube
        int x1 = cube1.getFirstCornerX();
        int z1 = cube1.getFirstCornerZ();
        int x2 = cube1.getSecondCornerX();
        int z2 = cube1.getSecondCornerZ();

        // Get the coordinates of the two points defining the second cube
        int x3 = cube2.getFirstCornerX();
        int z3 = cube2.getFirstCornerZ();
        int x4 = cube2.getSecondCornerX();
        int z4 = cube2.getSecondCornerZ();
        // Check if the cubes intersect on the x-axis
        boolean xOverlap = (x1 > x3 && x1 < x4) || (x2 > x3 && x2 < x4) ||
                (x3 > x1 && x3 < x2) || (x4 > x1 && x4 < x2) || (x1 == x3 && x2 == x4);

        // Check if the cubes intersect on the z-axis
        boolean zOverlap = (z1 > z3 && z1 < z4) || (z2 > z3 && z2 < z4) ||
                (z3 > z1 && z3 < z2) || (z4 > z1 && z4 < z2) || (z1 == z3 && z2 == z4);

        // Return true if there is overlap on both the x and z axes
        return xOverlap && zOverlap;
    }
    public static boolean isOverlapping(CubeManager cube1)
    {
        List<CubeManager> cubes = new ArrayList<>(CubeManager.cubes.values());
        return cubes.stream().anyMatch(cube -> isInCube(cube1, cube));
    }
    public static CubeManager getRandomExcluding(Player player)
    {
        List<CubeManager> cubes = new ArrayList<>(CubeManager.cubes.values());

        cubes.removeIf(selector -> PlayerManager.instants.isOwnedByPlayer(player, selector));

        if(cubes.isEmpty()) return null;

        int randomIndex = random.nextInt(cubes.size());

        return cubes.get(randomIndex);

    }
    public static CubeManager getClosestExcluding(Location location, Player player)
    {
        List<CubeManager> cubes = new ArrayList<>(CubeManager.cubes.values());
        cubes.removeIf(selector -> PlayerManager.instants.isOwnedByPlayer(player, selector));
        if(cubes.isEmpty()) return null;
        return cubes.stream().min(Comparator.comparingDouble(cube -> cube.getCenter().distance(location))).orElse(null);
    }
    public static CubeManager getClosest(Location location)
    {
        List<CubeManager> cubes = new ArrayList<>();

        for (CubeManager cube : CubeManager.cubes.values()) {
            Location cubeCenter = cube.getCenter();
            if (location.distance(cubeCenter) <= ConfigManager.instants.getDistance_max()) {
                cubes.add(cube);
            }
        }
        return cubes.stream().min(Comparator.comparingDouble(cube -> cube.getCenter().distance(location))).orElse(null);
    }
    public static CubeManager getNewest(Location location) {

        List<CubeManager> cubes = new ArrayList<>(CubeManager.cubes.values());
        return cubes.stream()
                .min(Comparator.comparingLong(cube -> Math.abs(cube.getCreatedTime() - System.currentTimeMillis())))
                .orElse(null);

    }
    public static CubeManager getOverlapping(CubeManager cube1)
    {
        List<CubeManager> cubes = new ArrayList<>(CubeManager.cubes.values());
        return cubes.stream()
                .filter(cube -> isInCube(cube1, cube))
                .findFirst()
                .orElse(null);
    }
    public static CubeManager getCube(Location location)
    {
        List<CubeManager> cubes = new ArrayList<>(CubeManager.cubes.values());
        return cubes.stream()
                .filter(cube -> isInCube(location, cube))
                .findFirst()
                .orElse(null);
    }
    public static boolean isOverlapping(Location location)
    {
        List<CubeManager> cubes = new ArrayList<>(CubeManager.cubes.values());
        return cubes.stream().anyMatch(cube -> isInCube(location, cube));
    }

    public static boolean isInCube(Location location, CubeManager cube) {
        if (location == null || location.getWorld() == null || cube == null || cube.getWorld() == null) return false;
        return location.getWorld().equals(cube.getWorld())
                && between(location.getBlockX(), cube.getFirstCornerX(), cube.getSecondCornerX())
                && between(location.getBlockZ(), cube.getFirstCornerZ(), cube.getSecondCornerZ());
    }

    private static boolean between(int val, int start, int end) {
        return val >= start && val < end;
    }
    private static final SaveManager cubesSaves = new SaveManager(TitanIslands.instance.getName(), "cubes");
    public static void loadAll()
    {
        for(String key: cubesSaves.getKeys())
        {
            SaveManager saveManager = cubesSaves.getSaveManager(key);
            CubeManager selectorTool = new CubeManager(saveManager);
            selectorTool.place();
        }
    }
    public static void saveAll()
    {
        for (CubeManager cubeManager : cubes.values())
        {
            SaveManager save = cubeManager.save();
            cubesSaves.set(cubeManager.getId().toString(), save);
        }
        cubesSaves.save();
    }
    private final Location firstCorner;
    private final Location secondCorner;
    private String name;
    private final UUID id;
    private final World world;
    private final long createdTime;

    private IslandManager islandManager;
    public CubeManager(Location firstCorner, Location secondCorner) {
        this.world = firstCorner.getWorld();
        this.firstCorner = firstCorner.clone();
        this.secondCorner = secondCorner.clone();
        this.createdTime = System.currentTimeMillis();
        this.id = this.generateID();
    }
    public CubeManager(SaveManager saveManager)
    {
        this.firstCorner = saveManager.getLocation("first_corner");
        this.secondCorner = saveManager.getLocation("second_corner");
        this.name = saveManager.getString("name");
        this.id = saveManager.getUUID("id");
        this.createdTime = saveManager.getLong("created_time");
        this.world = firstCorner.getWorld();
        this.islandManager = IslandManager.getIsland(saveManager.getUUID("island.id"));
        this.islandManager.add(this);
    }

    public UUID getId() {
        return id;
    }

    public UUID generateID()
    {
        UUID idtmp = UUID.randomUUID();
        while (cubes.containsKey(idtmp))
        {
            idtmp = UUID.randomUUID();
        }
        return idtmp;
    }
    public long getCreatedTime() {
        return createdTime;
    }

    public SaveManager save()
    {
        SaveManager saveManager = new SaveManager();
        saveManager.set("first_corner", this.firstCorner.clone());
        saveManager.set("second_corner", this.secondCorner.clone());
        saveManager.set("created_time", this.createdTime);
        saveManager.set("id", this.id);
        saveManager.set("name", this.name);
        if (this.islandManager != null) saveManager.set("island.id", this.islandManager.getId());
        return saveManager;
    }

    public String getName() {
        return name;
    }
    private void place()
    {
        CubeManager.placeCube(this);
    }
    public void place(IslandManager islandManager, String name)
    {
        this.islandManager = islandManager;
        this.name = name;
        CubeManager.placeCube(this);
        this.islandManager.add(this);
    }
    public Location getFirstCorner() {
        return firstCorner.clone();
    }

    public Location getSecondCorner() {
        return secondCorner.clone();
    }
    public int getWidth() {
        int i =  firstCorner.getBlockX() - secondCorner.getBlockX();
        return Math.abs(i);
    }

    public int getDepth() {
        int i =  firstCorner.getBlockZ() - secondCorner.getBlockZ();
        return Math.abs(i);
    }
    public int getHeight()
    {
        int i = firstCorner.getBlockY() - secondCorner.getBlockY();
        return Math.abs(i);
    }
    public Location getCenterOffset() {
        Location center = getCenter();
        Location subtract = center.clone().subtract(getFirstCorner());
        return subtract.clone();
    }

    public World getWorld() {
        return world;
    }

    public Location getCenter() {
        Location firstCorner = getFirstCorner();
        Location secondCorner = getSecondCorner();
        int x1 = firstCorner.getBlockX();
        int x2 = secondCorner.getBlockX();
        int y1 = firstCorner.getBlockY();
        int y2 = secondCorner.getBlockY();
        int z1 = firstCorner.getBlockZ();
        int z2 = secondCorner.getBlockZ();

        int xCenter = (x1 + x2) / 2;
        int yCenter = (y1 + y2) / 2;
        int zCenter = (z1 + z2) / 2;

        return new Location(this.world, xCenter, yCenter, zCenter);
    }
    public int getHighest()
    {
        return Math.max(getFirstCorner().getBlockY(), getSecondCorner().getBlockY());
    }

    private int getSecondCornerZ() {
        return this.secondCorner.getBlockZ();
    }

    private int getSecondCornerY() {
        return this.secondCorner.getBlockY();
    }

    private int getSecondCornerX() {
        return this.secondCorner.getBlockX();
    }

    private int getFirstCornerZ() {

        return this.firstCorner.getBlockZ();
    }

    private int getFirstCornerY() {
        return this.firstCorner.getBlockY();
    }

    private int getFirstCornerX() {
        return this.firstCorner.getBlockX();
    }


    public IslandManager getIsland() {
        return IslandManager.getIsland(this);
    }
}