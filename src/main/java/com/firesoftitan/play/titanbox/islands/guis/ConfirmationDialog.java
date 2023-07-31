package com.firesoftitan.play.titanbox.islands.guis;

import com.firesoftitan.play.titanbox.islands.TitanIslands;
import com.firesoftitan.play.titanbox.islands.managers.LangManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.scheduler.BukkitRunnable;

import static com.firesoftitan.play.titanbox.islands.TitanIslands.messageTool;

public class ConfirmationDialog {

    public static void show(Player player, String message, BukkitRunnable onAccept) {

        new BukkitRunnable() {

            @Override
            public void run() {

                if(player.isOnline()) {

                    // show a confirmation message
                    player.sendMessage(message);
                    player.sendMessage(LangManager.instants.getMessage("confirmation"));

                    // register accept/decline handlers
                    Bukkit.getPluginManager().registerEvents(new ConfirmHandler(onAccept), TitanIslands.instance);

                }

            }

        }.runTask(TitanIslands.instance);

    }

    private record ConfirmHandler(BukkitRunnable onAccept) implements Listener {
        @EventHandler
            public void onChat(AsyncPlayerChatEvent evt) {
                Player player = evt.getPlayer();
                String msg = evt.getMessage();
                if (msg.equalsIgnoreCase(LangManager.instants.getMessage("accept").toLowerCase())) {
                    onAccept.runTask(TitanIslands.instance);
                } else {
                    messageTool.sendMessagePlayer(player, LangManager.instants.getMessage("canceled"));
                }
                evt.setCancelled(true);
                evt.getHandlers().unregister(this);
            }

        }

}
