package com.firesoftitan.play.titanbox.islands.listeners;

import com.firesoftitan.play.titanbox.islands.TitanIslands;
import com.firesoftitan.play.titanbox.islands.managers.IslandManager;
import com.firesoftitan.play.titanbox.islands.managers.PlayerManager;
import com.firesoftitan.play.titanbox.islands.managers.StructureManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class TabCompleteListener implements TabCompleter {
    private static final String[] ADMIN_COMMANDS = {"spawn", "build", "admin", "delete", "count"};
    private static final String[] NON_ADMIN_COMMANDS = {"home", "add", "sethome", "total", "info", "friends", "remove", "replace", "claim", "abandon"};
    private final List<String> pluginNames = new ArrayList<String>();
    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        List<String> Commands = new ArrayList<String>();
        if (args.length == 1) {
            if (TitanIslands.isAdmin(commandSender)) {
                Commands.addAll(List.of(ADMIN_COMMANDS));
            }
            Commands.addAll(List.of(NON_ADMIN_COMMANDS));

        }
        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("count"))
            {
                Commands.add("add");
                Commands.add("remove");
                Commands.add("get");
            }
            if (args[0].equalsIgnoreCase("friends"))
            {
                Commands.add("add");
                Commands.add("remove");
                Commands.add("list");
            }
            if (args[0].equalsIgnoreCase("give"))
            {
                return null;
            }
            if (args[0].equalsIgnoreCase("replace"))
            {
                List<String> unlocked = TitanIslands.playerManager.getUnlocked((Player) commandSender);
                Commands.addAll(unlocked);
            }
            if (args[0].equalsIgnoreCase("add"))
            {
                List<String> unlocked = TitanIslands.playerManager.getUnlocked((Player) commandSender);
                Commands.addAll(unlocked);
            }
            if (args[0].equalsIgnoreCase("build"))
            {
                List<String> unlocked = StructureManager.getStructures();
                Commands.addAll(unlocked);
            }
        }
        if (args.length == 3)
        {
            if (args[0].equalsIgnoreCase("friends")) {
                if (args[1].equalsIgnoreCase("add")) {
                    return null;
                }
                if (args[1].equalsIgnoreCase("remove")) {
                    Location home = PlayerManager.instants.getHome((Player) commandSender);
                    if (home != null) {
                        IslandManager islandManager = IslandManager.getIsland(home);
                        if (islandManager != null) {
                            List<UUID> friends = islandManager.getFriends();
                            List<String> names = new ArrayList<String>();
                            for (UUID friend : friends) {
                                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(friend);
                                if (offlinePlayer != null) names.add(offlinePlayer.getName());
                            }
                            return names;
                        }
                    }
                }
            }
            if (args[0].equalsIgnoreCase("count")) {
                if (args[1].equalsIgnoreCase("set") || args[1].equalsIgnoreCase("add") || args[1].equalsIgnoreCase("remove") || args[1].equalsIgnoreCase("get")) {
                    OfflinePlayer[] offlinePlayers = Bukkit.getOfflinePlayers();
                    for (OfflinePlayer friend : offlinePlayers) {
                        Commands.add(friend.getName());
                    }
                }
            }
        }
        if (args.length == 4)
        {
            if (args[0].equalsIgnoreCase("count")) {
                if (args[1].equalsIgnoreCase("set") || args[1].equalsIgnoreCase("add") || args[1].equalsIgnoreCase("remove") || args[1].equalsIgnoreCase("get")) {
                    Commands.addAll(PlayerManager.instants.getCountList(((Player)commandSender)));
                }
            }
        }
        if (args.length == 5)
        {
            if (args[0].equalsIgnoreCase("count")) {
                if (args[1].equalsIgnoreCase("set") || args[1].equalsIgnoreCase("add") || args[1].equalsIgnoreCase("remove")) {
                    Commands.add("#");
                }
            }
        }
        //create new array
        final List<String> completions = new ArrayList<>();
        //copy matches of first argument from list (ex: if first arg is 'm' will return just 'minecraft')
        StringUtil.copyPartialMatches(args[args.length - 1], Commands, completions);
        //sort the list
        Collections.sort(completions);


        return completions;

    }
}
