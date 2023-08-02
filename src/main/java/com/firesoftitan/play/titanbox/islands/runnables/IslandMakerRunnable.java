package com.firesoftitan.play.titanbox.islands.runnables;

import com.firesoftitan.play.titanbox.islands.enums.StructureTypeEnum;
import com.firesoftitan.play.titanbox.islands.managers.FragmentManager;
import com.firesoftitan.play.titanbox.islands.managers.IslandManager;
import com.firesoftitan.play.titanbox.islands.managers.StructureManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import static com.firesoftitan.play.titanbox.islands.TitanIslands.playerManager;

public class IslandMakerRunnable extends BukkitRunnable {
    private final Location location;
    private final String[][] island;

    private final Player player;
    private int width = 0;
    private int height = 0;
    private int row = 0, col = 0;
    private final IslandManager islandManager;
    public IslandMakerRunnable(Player player, Location location, String[][] island) {
        this.player = player;
        this.location = location.clone();
        this.island = island;
        this.islandManager = new IslandManager();
        if (player != null) playerManager.add(player, this.islandManager);
    }

    @Override
    public void run() {
        String iKey = island[row][col];

        Location updatedLocation = location.clone().add(col*width, 0, row*height);
        String[] split = iKey.split(":");
        StructureManager structure = StructureManager.getStructure(split[0], StructureTypeEnum.getType(split[1]), split[2]);
        Location check = FragmentManager.adjustLocation(structure, updatedLocation);

        FragmentManager build = structure.build(check, islandManager.getHeight());
        if (!FragmentManager.isOverlapping(build)) {
            if (row == 0) width = build.getWidth();
            height = build.getDepth();
            build.place(islandManager, structure);
            if (player != null) playerManager.add(player, build);
        }
        col++;
        if (col >= island[0].length)
        {
            row++;
            col = 0;
            if (row >= island.length)
            {
                this.cancel();
            }
        }
    }
}
