package com.firesoftitan.play.titanbox.islands.tools;

import com.firesoftitan.play.titanbox.islands.TitanIslands;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class CubeSelectorTool {
    private static HashMap<String, CubeSelectorTool> cubes = new HashMap<String, CubeSelectorTool>();

    public static void placeCube(CubeSelectorTool cubeSelectorTool)
    {
        String keyID = TitanIslands.tools.getSerializeTool().serializeLocation(cubeSelectorTool.firstCorner) +
                "~" + TitanIslands.tools.getSerializeTool().serializeLocation(cubeSelectorTool.secondCorner);
        cubes.put(keyID, cubeSelectorTool);
    }
    public static boolean isInCube(CubeSelectorTool cube1, CubeSelectorTool cube2) {
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
        cube1.getFirstCorner().getBlock().setType(Material.DIAMOND_BLOCK);
        cube1.getSecondCorner().getBlock().setType(Material.GOLD_BLOCK);
        cube2.getFirstCorner().getBlock().setType(Material.COAL_BLOCK);
        cube2.getSecondCorner().getBlock().setType(Material.NETHERITE_BLOCK);
        // Check if the cubes intersect on the x-axis
        boolean xOverlap = (x1 > x3 && x1 < x4) || (x2 > x3 && x2 < x4) ||
                (x3 > x1 && x3 < x2) || (x4 > x1 && x4 < x2);

        // Check if the cubes intersect on the z-axis
        boolean zOverlap = (z1 > z3 && z1 < z4) || (z2 > z3 && z2 < z4) ||
                (z3 > z1 && z3 < z2) || (z4 > z1 && z4 < z2);

        // Return true if there is overlap on both the x and z axes
        return xOverlap && zOverlap;
    }
    public static boolean inAnyCubes(CubeSelectorTool cube1)
    {
        List<CubeSelectorTool> cubes = new ArrayList<>(CubeSelectorTool.cubes.values());
        return cubes.stream().anyMatch(cube -> isInCube(cube1, cube));
    }
    public static boolean inAnyCubes(Location location)
    {
        List<CubeSelectorTool> cubes = new ArrayList<>(CubeSelectorTool.cubes.values());
        return cubes.stream().anyMatch(cube -> isInCube(location, cube));
    }

    public static boolean isInCube(Location location, CubeSelectorTool cube) {
        return location.getWorld().equals(cube.getWorld())
                && between(location.getBlockX(), cube.getFirstCornerX(), cube.getSecondCornerX())
                //&& between(location.getBlockY(), cube.getFirstCornerY(), cube.getSecondCornerY())
                && between(location.getBlockZ(), cube.getFirstCornerZ(), cube.getSecondCornerZ());
    }

    private static boolean between(int val, int start, int end) {
        return val >= start && val <= end;
    }


    private Location firstCorner;
    private Location secondCorner;
    private World world;

    public CubeSelectorTool(Location firstCorner, Location secondCorner) {
        this.world = firstCorner.getWorld();
        this.firstCorner = firstCorner.clone();
        this.secondCorner = secondCorner.clone();
    }
    public void place()
    {
        CubeSelectorTool.placeCube(this);
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
        Location subtract = center.subtract(getFirstCorner());
        return subtract;
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