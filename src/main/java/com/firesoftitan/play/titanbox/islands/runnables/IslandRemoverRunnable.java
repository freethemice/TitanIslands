package com.firesoftitan.play.titanbox.islands.runnables;

import com.firesoftitan.play.titanbox.islands.TitanIslands;
import com.firesoftitan.play.titanbox.islands.managers.ConfigManager;
import com.firesoftitan.play.titanbox.islands.managers.CubeManager;
import com.firesoftitan.play.titanbox.islands.managers.IslandManager;
import com.firesoftitan.play.titanbox.islands.managers.LangManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class IslandRemoverRunnable extends BukkitRunnable {

    private List<CubeManager> cubes = new ArrayList<CubeManager>();
    private IslandManager oldest;
    public static IslandRemoverRunnable instance;

    public IslandRemoverRunnable() {
        instance = this;
    }


    public IslandRemoverRunnable(IslandManager manager) {
        this.oldest = manager;
        cubes = oldest.getCubes();
    }

    @Override
    public void run() {
        if (cubes.isEmpty()) {
            if (ConfigManager.getInstants().getDecay() < 1) return;
            oldest = IslandManager.getOldest(true);
            if (System.currentTimeMillis() - oldest.getCreatedTime() > ConfigManager.getInstants().getDecay() * 1000L) {
                cubes = oldest.getCubes();
            }
        }
        else
        {
            CubeManager cube = cubes.get(0);
            CubeManager.deleteCube(cube.getCenter());
            cubes.remove(0);
            if (cubes.isEmpty())
            {
                IslandManager.removeIsland(oldest);
                oldest = null;
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
}
