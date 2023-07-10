package com.firesoftitan.play.titanbox.islands.managers;

import com.firesoftitan.play.titanbox.islands.TitanIslands;
import com.firesoftitan.play.titanbox.libs.managers.SaveManager;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockFace;

import java.util.*;

public class CubeSelectorManager {
    private static HashMap<String, CubeSelectorManager> cubes = new HashMap<String, CubeSelectorManager>();
    private static final int MAX_CHECK_OFFSET = 250;
    private static final int[] X_OFFSETS = {1, -1};
    private static final int[] Z_OFFSETS = {1, -1};
    private static Random random = new Random(System.currentTimeMillis());
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
            CubeSelectorManager build = structure.getPreBuild(newLocation);
            if (!CubeSelectorManager.isOverlapping(build)) {
                return newLocation;
            }
            //useXOffset = !useXOffset;  // Alternate between x and z
        }
        return locationToCheck;
    }
    public static void placeCube(CubeSelectorManager cubeSelectorManager)
    {
        cubes.put(cubeSelectorManager.getKey(), cubeSelectorManager);
    }
    public static boolean isInCube(CubeSelectorManager cube1, CubeSelectorManager cube2) {
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
    public static boolean isOverlapping(CubeSelectorManager cube1)
    {
        List<CubeSelectorManager> cubes = new ArrayList<>(CubeSelectorManager.cubes.values());
        return cubes.stream().anyMatch(cube -> isInCube(cube1, cube));
    }
    public static CubeSelectorManager getClosest(Location location)
    {
        List<CubeSelectorManager> cubes = new ArrayList<>(CubeSelectorManager.cubes.values());
        Optional<CubeSelectorManager> min = cubes.stream().min(Comparator.comparing(cube -> cube.getCenter().distance(location)));
        return min.get();
    }
    public static CubeSelectorManager getOverlapping(CubeSelectorManager cube1)
    {
        List<CubeSelectorManager> cubes = new ArrayList<>(CubeSelectorManager.cubes.values());
        return cubes.stream()
                .filter(cube -> isInCube(cube1, cube))
                .findFirst()
                .orElse(null);
    }
    public static CubeSelectorManager getCube(Location location)
    {
        List<CubeSelectorManager> cubes = new ArrayList<>(CubeSelectorManager.cubes.values());
        return cubes.stream()
                .filter(cube -> isInCube(location, cube))
                .findFirst()
                .orElse(null);
    }
    public static boolean isOverlapping(Location location)
    {
        List<CubeSelectorManager> cubes = new ArrayList<>(CubeSelectorManager.cubes.values());
        return cubes.stream().anyMatch(cube -> isInCube(location, cube));
    }

    public static boolean isInCube(Location location, CubeSelectorManager cube) {
        return location.getWorld().equals(cube.getWorld())
                && between(location.getBlockX(), cube.getFirstCornerX(), cube.getSecondCornerX())
                //&& between(location.getBlockY(), cube.getFirstCornerY(), cube.getSecondCornerY())
                && between(location.getBlockZ(), cube.getFirstCornerZ(), cube.getSecondCornerZ());
    }

    private static boolean between(int val, int start, int end) {
        return val >= start && val <= end;
    }
    private static SaveManager cubesSaves = new SaveManager(TitanIslands.instance.getName(), "cubes");
    public static void loadAll()
    {
        for(String key: cubesSaves.getKeys())
        {
            SaveManager saveManager = cubesSaves.getSaveManager(key);
            CubeSelectorManager selectorTool = new CubeSelectorManager(saveManager);
            selectorTool.place();
        }
    }
    public static void saveAll()
    {
        for (CubeSelectorManager cubeSelectorManager : cubes.values())
        {
            cubeSelectorManager.save();
        }
        cubesSaves.save();
    }
    private Location firstCorner;
    private Location secondCorner;
    private String name;
    private World world;

    public CubeSelectorManager(Location firstCorner, Location secondCorner) {
        this.world = firstCorner.getWorld();
        this.firstCorner = firstCorner.clone();
        this.secondCorner = secondCorner.clone();
    }
    public CubeSelectorManager(SaveManager saveManager)
    {
        this.firstCorner = saveManager.getLocation("first_corner");
        this.secondCorner = saveManager.getLocation("second_corner");
        this.name = saveManager.getString("name");
        this.world = firstCorner.getWorld();
    }
    public void save()
    {
        SaveManager saveManager = new SaveManager();
        saveManager.set("first_corner", this.firstCorner.clone());
        saveManager.set("second_corner", this.secondCorner.clone());
        saveManager.set("name", this.name);
        cubesSaves.set(this.getKey(), saveManager);
    }
    public String getKey()
    {
        String keyID = TitanIslands.tools.getSerializeTool().serializeLocation(this.firstCorner) +
                "~" + TitanIslands.tools.getSerializeTool().serializeLocation(this.secondCorner);
        return keyID;
    }

    public String getName() {
        return name;
    }
    private void place()
    {
        CubeSelectorManager.placeCube(this);
    }
    public void place(String name)
    {
        this.name = name;
        CubeSelectorManager.placeCube(this);
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


}