package com.firesoftitan.play.titanbox.islands.runnables;

import com.firesoftitan.play.titanbox.islands.TitanIslands;
import com.firesoftitan.play.titanbox.islands.managers.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class IslandRemoverRunnable extends BukkitRunnable {

    private List<FragmentManager> fragments = new ArrayList<FragmentManager>();
    private Location[] islandBounds;
    private Location islandCenter;
    private IslandManager oldest;
    public static IslandRemoverRunnable instance;

    public IslandRemoverRunnable() {
        instance = this;
    }


    public IslandRemoverRunnable(IslandManager manager) {
        this.oldest = manager;
        fragments = oldest.getFragments();
        islandBounds = oldest.getBounds();
        islandCenter = oldest.getCenter();
    }

    @Override
    public void run() {
        if (fragments.isEmpty()) {
            if (ConfigManager.getInstants().getDecay() < 1) return;
            oldest = IslandManager.getOldest(true);
            if (System.currentTimeMillis() - oldest.getCreatedTime() > ConfigManager.getInstants().getDecay() * 1000L) {
                fragments = oldest.getFragments();
                islandBounds = oldest.getBounds();
                islandCenter = oldest.getCenter();
            }
        }
        else
        {
            FragmentManager fragment = fragments.get(0);
            FragmentManager.deleteFragment(fragment.getCenter());
            int structureCount = StructureManager.getStructureCount(fragment.getName());
            StructureManager.setStructureCount(fragment.getName(), structureCount - 1);//global count
            fragments.remove(0);
            if (fragments.isEmpty())
            {
                IslandManager.removeIsland(oldest);

                clearIslandMobs();

                oldest = null;
                fragments = null;
                islandBounds = null;
                if (TitanIslands.configManager.isAnnounce()) {
                    List<Player> playerList = new ArrayList<Player>(Bukkit.getOnlinePlayers());
                    if (!playerList.isEmpty()) {
                        for (Player playerA : playerList) {
                            playerA.sendMessage(LangManager.instants.getMessage("removed"));
                        }
                    }
                }
                if (IslandRemoverRunnable.instance != this) this.cancel();
            }
        }

    }

    private void clearIslandMobs() {
        double x1 = islandBounds[0].getX();
        double z1 = islandBounds[0].getZ();


        double x2 = islandBounds[1].getX();
        double z2 = islandBounds[1].getZ();

        double width = Math.abs(x2 - x1);
        double depth = Math.abs(z2 - z1);

        double diagonal = Math.sqrt(width * width + depth * depth);
        diagonal = diagonal / 2;

        List<Entity> entities = TitanIslands.tools.getEntityTool().findEntities(islandCenter, (int) diagonal, "");
        for (Entity e: entities)
        {
            if (e.getType() != EntityType.PLAYER) e.remove();
        }
    }
}
