package com.firesoftitan.play.titanbox.islands.tools;

import java.util.HashMap;
import java.util.Map;

public class IslandGeneratorInfo
{
    private final int islandWidth;
    private final int islandHeight;
    private int animalCount;
    private int buildingCount;
    private int currentCol, currentRow;

    private final Map<String, Integer> structureCount = new HashMap<String, Integer>();
    public IslandGeneratorInfo(int islandWidth, int islandHeight) {
        this.islandWidth = islandWidth;
        this.islandHeight = islandHeight;
    }

    private int getMaxAnimalCount() {

        if (islandWidth < 5 || islandHeight < 5) {
            return 0;
        }

        int area = islandWidth * islandHeight;

        if (area <= 25) {
            return 1;
        } else if (area <= 50) {
            return 2;
        } else if (area <= 75) {
            return 3;
        } else {
            return 4;
        }
    }
    public int getStructureCount(String name)
    {
        Integer count = structureCount.get(name);
        if(count == null) {
            return 0;
        }
        return count;
    }
    public void setStructureCount(String name, int value)
    {
        structureCount.put(name, value);
    }


    private int getMaxBuildingCount() {

        if (islandWidth < 5 || islandHeight < 5) {
            return 0;
        }

        int area = islandWidth * islandHeight;

        if (area <= 25) {
            return 1;
        } else if (area <= 50) {
            return 2;
        } else if (area <= 75) {
            return 3;
        } else {
            return 4;
        }
    }
    public int getBuildingCount() {
        return buildingCount;
    }

    public void setBuildingCount(int buildingCount) {
        this.buildingCount = buildingCount;
    }
    public boolean canAddMoreBuilding()
    {
        return getBuildingCount() < getMaxBuildingCount();
    }
    public boolean canAddMoreAnimals()
    {
        return getAnimalCount() < getMaxAnimalCount();
    }
    public int getIslandWidth() {
        return islandWidth;
    }

    public int getIslandHeight() {
        return islandHeight;
    }

    public int getAnimalCount() {
        return animalCount;
    }

    public void setAnimalCount(int animalCount) {
        this.animalCount = animalCount;
    }

    public int getCurrentCol() {
        return currentCol;
    }

    public void setCurrentCol(int currentCol) {
        this.currentCol = currentCol;
    }

    public int getCurrentRow() {
        return currentRow;
    }

    public void setCurrentRow(int currentRow) {
        this.currentRow = currentRow;
    }
}
