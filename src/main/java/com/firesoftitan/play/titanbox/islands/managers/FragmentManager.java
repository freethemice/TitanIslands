package com.firesoftitan.play.titanbox.islands.managers;

import com.firesoftitan.play.titanbox.islands.TitanIslands;
import com.firesoftitan.play.titanbox.islands.enums.StructureTypeEnum;
import com.firesoftitan.play.titanbox.libs.managers.SaveManager;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.*;

public class FragmentManager {

    private static final SaveManager fragmentsSaves = new SaveManager(TitanIslands.instance.getName(), "fragments");
    private static final HashMap<UUID, FragmentManager> fragments = new HashMap<UUID, FragmentManager>();
    private static final int MAX_CHECK_OFFSET = 250;
    private static final int[] X_OFFSETS = {1, -1};
    private static final int[] Z_OFFSETS = {1, -1};
    private static final Random random = new Random(System.currentTimeMillis());

    public static Location adjustLocation(StructureManager structure, Location locationToCheck) {
        boolean useXOffset = random.nextBoolean();
        int offsetIndex = random.nextInt(2);  // Either 0 or 1
        int xOffset = useXOffset ? X_OFFSETS[offsetIndex] : 0;
        int zOffset = useXOffset ? 0 : Z_OFFSETS[offsetIndex];
        BlockFace blockFace = null;
        if (xOffset == -1) blockFace = BlockFace.WEST;
        else if (xOffset == 1) blockFace = BlockFace.EAST;
        else if (zOffset == -1) blockFace = BlockFace.NORTH;
        else if (zOffset == 1) blockFace = BlockFace.SOUTH;
        return adjustLocation(structure, locationToCheck, blockFace);
    }

    public static void deleteFragment(Location location)
    {
        if (!Objects.requireNonNull(location.getWorld()).getName().equals(ConfigManager.getInstants().getWorld().getName()))
        {
            return;
        }
        FragmentManager fragmentManager = FragmentManager.getFragment(location);
        if (fragmentManager == null) {
            return;
        }
        UUID owner = PlayerManager.getOwner(fragmentManager);
        Location firstCorner = fragmentManager.getCenter();
        String emptyType = "water";
        if (ConfigManager.getInstants().getType().equalsIgnoreCase("air")) emptyType = "air";
        //noinspection SpellCheckingInspection
        StructureManager structure = StructureManager.getStructure("primary", StructureTypeEnum.INLAND, emptyType);
        FragmentManager build = structure.build(firstCorner.clone(), fragmentManager.getIsland().getHeight());

        fragments.remove(fragmentManager.getId());
        fragmentsSaves.delete(fragmentManager.getId().toString());
        fragmentManager.getIsland().removeFragment(fragmentManager);


        double diagonal = Math.sqrt(fragmentManager.getWidth() * fragmentManager.getWidth() + fragmentManager.getDepth() * fragmentManager.getDepth());
        diagonal = diagonal / 2;

        List<Entity> entities = TitanIslands.tools.getEntityTool().findEntities(fragmentManager.getCenter(), (int) diagonal, "");
        for (Entity e: entities)
        {
            if (e.getType() != EntityType.PLAYER) e.remove();
        }

    }


    public static Location adjustLocation(StructureManager structure, Location locationToCheck, BlockFace direction)
    {
        int xOffset = direction.getModX();
        int zOffset = direction.getModZ();
        if (structure == null) return null;
        for (int i = 0; i < MAX_CHECK_OFFSET; i++) {
            Location newLocation = locationToCheck.clone().add(i*xOffset, 0, i*zOffset);
            FragmentManager build = structure.getPreBuild(newLocation, 0);
            if (!FragmentManager.isOverlapping(build)) {
                return newLocation;
            }
            //useXOffset = !useXOffset;  // Alternate between x and z
        }
        return locationToCheck;
    }
    public static void placeFragment(FragmentManager fragmentManager)
    {
        fragments.put(fragmentManager.getId(), fragmentManager);
    }
    /**
     * Checks if two fragments represented by FragmentSelectorManager objects intersect.
     *
     * @param fragment1 The first fragment to check
     * @param fragment2 The second fragment to check
     * @return True if the fragments intersect, false otherwise
     */
    public static boolean isInFragment(FragmentManager fragment1, FragmentManager fragment2) {
        // Get the coordinates of the two points defining the first Fragment
        int x1 = fragment1.getFirstCornerX();
        int z1 = fragment1.getFirstCornerZ();
        int x2 = fragment1.getSecondCornerX();
        int z2 = fragment1.getSecondCornerZ();

        // Get the coordinates of the two points defining the second Fragment
        int x3 = fragment2.getFirstCornerX();
        int z3 = fragment2.getFirstCornerZ();
        int x4 = fragment2.getSecondCornerX();
        int z4 = fragment2.getSecondCornerZ();
        // Check if the fragments intersect on the x-axis
        boolean xOverlap = (x1 > x3 && x1 < x4) || (x2 > x3 && x2 < x4) ||
                (x3 > x1 && x3 < x2) || (x4 > x1 && x4 < x2) || (x1 == x3 && x2 == x4);

        // Check if the fragments intersect on the z-axis
        boolean zOverlap = (z1 > z3 && z1 < z4) || (z2 > z3 && z2 < z4) ||
                (z3 > z1 && z3 < z2) || (z4 > z1 && z4 < z2) || (z1 == z3 && z2 == z4);

        // Return true if there is overlap on both the x and z axes
        return xOverlap && zOverlap;
    }
    public static boolean isOverlapping(FragmentManager fragment1)
    {
        List<FragmentManager> fragments = new ArrayList<>(FragmentManager.fragments.values());
        return fragments.stream().anyMatch(fragment -> isInFragment(fragment1, fragment));
    }
    public static FragmentManager getRandomExcluding(Player player)
    {
        List<FragmentManager> fragments = new ArrayList<>(FragmentManager.fragments.values());

        fragments.removeIf(selector -> PlayerManager.isOwnedByPlayer(player, selector));

        if(fragments.isEmpty()) return null;

        int randomIndex = random.nextInt(fragments.size());

        return fragments.get(randomIndex);

    }
    public static FragmentManager getClosestExcluding(Location location, Player player)
    {
        List<FragmentManager> fragments = new ArrayList<>(FragmentManager.fragments.values());
        fragments.removeIf(selector -> PlayerManager.isOwnedByPlayer(player, selector));
        if(fragments.isEmpty()) return null;
        return fragments.stream().min(Comparator.comparingDouble(fragment -> fragment.getCenter().distance(location))).orElse(null);
    }
    public static FragmentManager getClosest(Location location)
    {
        List<FragmentManager> fragments = new ArrayList<>();

        for (FragmentManager fragment : FragmentManager.fragments.values()) {
            Location FragmentCenter = fragment.getCenter();
            if (location.distance(FragmentCenter) <= ConfigManager.instants.getDistance_max()) {
                fragments.add(fragment);
            }
        }
        return fragments.stream().min(Comparator.comparingDouble(fragment -> fragment.getCenter().distance(location))).orElse(null);
    }
    public static FragmentManager getNewest(Location location) {

        List<FragmentManager> fragments = new ArrayList<>(FragmentManager.fragments.values());
        return fragments.stream()
                .min(Comparator.comparingLong(fragment -> Math.abs(fragment.getCreatedTime() - System.currentTimeMillis())))
                .orElse(null);

    }
    public static FragmentManager getOverlapping(FragmentManager fragment1)
    {
        List<FragmentManager> fragments = new ArrayList<>(FragmentManager.fragments.values());
        return fragments.stream()
                .filter(fragment -> isInFragment(fragment1, fragment))
                .findFirst()
                .orElse(null);
    }
    public static FragmentManager getFragment(Location location)
    {
        List<FragmentManager> fragments = new ArrayList<>(FragmentManager.fragments.values());
        return fragments.stream()
                .filter(fragment -> isInFragment(location, fragment))
                .findFirst()
                .orElse(null);
    }
    public static boolean isOverlapping(Location location)
    {
        List<FragmentManager> fragments = new ArrayList<>(FragmentManager.fragments.values());
        return fragments.stream().anyMatch(fragment -> isInFragment(location, fragment));
    }

    public static boolean isInFragment(Location location, FragmentManager fragments) {
        if (location == null || location.getWorld() == null || fragments == null || fragments.getWorld() == null) return false;
        return location.getWorld().equals(fragments.getWorld())
                && between(location.getBlockX(), fragments.getFirstCornerX(), fragments.getSecondCornerX())
                && between(location.getBlockZ(), fragments.getFirstCornerZ(), fragments.getSecondCornerZ());
    }

    private static boolean between(int val, int start, int end) {
        return val >= start && val < end;
    }
    public static void loadAll()
    {
        for(String key: fragmentsSaves.getKeys())
        {
            SaveManager saveManager = fragmentsSaves.getSaveManager(key);
            FragmentManager selectorTool = new FragmentManager(saveManager);
            selectorTool.place();
        }
    }
    public static void saveAll()
    {
        for (FragmentManager fragmentManager : fragments.values())
        {
            SaveManager save = fragmentManager.save();
            fragmentsSaves.set(fragmentManager.getId().toString(), save);
        }
        fragmentsSaves.save();
    }
    private final Location firstCorner;
    private final Location secondCorner;
    private String name;
    private StructureTypeEnum section;
    private String namespace;

    private final UUID id;
    private final World world;
    private final long createdTime;

    private IslandManager islandManager;
    public FragmentManager(Location firstCorner, Location secondCorner) {
        this.world = firstCorner.getWorld();
        this.firstCorner = firstCorner.clone();
        this.secondCorner = secondCorner.clone();
        this.createdTime = System.currentTimeMillis();
        this.id = this.generateID();
    }
    public FragmentManager(SaveManager saveManager)
    {
        this.firstCorner = saveManager.getLocation("first_corner");
        this.secondCorner = saveManager.getLocation("second_corner");
        this.name = saveManager.getString("name");
        this.namespace = saveManager.getString("namespace");
        this.section = StructureTypeEnum.getType(saveManager.getString("type"));
        this.id = saveManager.getUUID("id");
        this.createdTime = saveManager.getLong("created_time");
        this.world = firstCorner.getWorld();
        this.islandManager = IslandManager.getIsland(saveManager.getUUID("island.id"));
        this.islandManager.add(this);



    }

    public UUID getOwner()
    {
        return PlayerManager.getOwner(this);
    }
    public UUID getId() {
        return id;
    }

    public UUID generateID()
    {
        UUID idtmp = UUID.randomUUID();
        while (fragments.containsKey(idtmp))
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
        saveManager.set("namespace", this.namespace);
        saveManager.set("type", this.section.getName());
        if (this.islandManager != null) saveManager.set("island.id", this.islandManager.getId());
        return saveManager;
    }
    public StructureManager getStructure()
    {
        return StructureManager.getStructure(this.namespace, this.section, this.name);
    }
    public StructureTypeEnum getType() {
        return section;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getName() {
        return name;
    }
    private void place()
    {
        FragmentManager.placeFragment(this);
    }
    public void place(IslandManager islandManager, StructureManager structureManager)
    {
        this.islandManager = islandManager;
        this.name = structureManager.getName();
        this.namespace = structureManager.getNamespace();
        this.section = structureManager.getType();
        FragmentManager.placeFragment(this);
        this.islandManager.add(this);
    }
    public void place(IslandManager islandManager, String namespace, StructureTypeEnum section, String name)
    {
        this.islandManager = islandManager;
        this.name = name;
        this.namespace = namespace;
        this.section = section;
        FragmentManager.placeFragment(this);
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