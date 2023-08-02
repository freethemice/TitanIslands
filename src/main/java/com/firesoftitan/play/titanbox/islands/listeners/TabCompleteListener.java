package com.firesoftitan.play.titanbox.islands.listeners;

import com.firesoftitan.play.titanbox.islands.TitanIslands;
import com.firesoftitan.play.titanbox.islands.enums.StructureTypeEnum;
import com.firesoftitan.play.titanbox.islands.managers.IslandManager;
import com.firesoftitan.play.titanbox.islands.managers.PlayerManager;
import com.firesoftitan.play.titanbox.islands.managers.StructureManager;
import org.bukkit.Bukkit;
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
    private static final String[] ADMIN_COMMANDS = {"spawn", "build", "admin", "delete", "count", "unlock"};
    private static final String[] NON_ADMIN_COMMANDS = {"home", "add", "sethome", "total", "info", "friends", "remove", "replace", "claim", "abandon"};
    private final List<String> pluginNames = new ArrayList<String>();
    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        List<String> commands = new ArrayList<String>();
        if (args.length == 1) {
            if (TitanIslands.isAdmin(commandSender)) {
                commands.addAll(List.of(ADMIN_COMMANDS));
            }
            commands.addAll(List.of(NON_ADMIN_COMMANDS));

        }
        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("count"))
            {
                commands.add("add");
                commands.add("remove");
                commands.add("get");
            }
            if (args[0].equalsIgnoreCase("friends"))
            {
                commands.add("add");
                commands.add("remove");
                commands.add("list");
            }
            if (args[0].equalsIgnoreCase("unlock"))
            {
                OfflinePlayer[] offlinePlayers = Bukkit.getOfflinePlayers();
                for (OfflinePlayer friend : offlinePlayers) {
                    commands.add(friend.getName());
                }
            }
            if (args[0].equalsIgnoreCase("build")) {
                commands.addAll(TitanIslands.instance.getNamespaceList());
            }

            if (args[0].equalsIgnoreCase("replace") || args[0].equalsIgnoreCase("add"))
            {
                //List<String> unlocked = TitanIslands.playerManager.getUnlocked((Player) commandSender);
                //commands.addAll(unlocked);
                for (String key : PlayerManager.instants.getUnlocked((Player) commandSender)) {
                    String nameKey = key.split(":")[0];
                    if (!commands.contains(nameKey)) commands.add(nameKey);
                }
            }
        }
        if (args.length == 3)
        {
            if (args[0].equalsIgnoreCase("replace") || args[0].equalsIgnoreCase("add") ) {
                for (String key : PlayerManager.instants.getUnlocked((Player) commandSender, args[1])) {
                    String nameKey = key.split(":")[1];
                    if (!commands.contains(nameKey)) commands.add(nameKey);
                }
            }
            if (args[0].equalsIgnoreCase("build"))
            {
                for(StructureTypeEnum structureTypeEnum: StructureTypeEnum.values()) {
                    commands.add(structureTypeEnum.getName());
                }
            }
            if (args[0].equalsIgnoreCase("unlock"))
            {
                commands.addAll(TitanIslands.instance.getNamespaceList());
            }
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
                        commands.add(friend.getName());
                    }
                }
            }
        }
        if (args.length == 4)
        {
            if (args[0].equalsIgnoreCase("unlock"))
            {
                for(StructureTypeEnum structureTypeEnum: StructureTypeEnum.values()) {
                    commands.add(structureTypeEnum.getName());
                }
            }

            if (args[0].equalsIgnoreCase("build"))
            {
                StructureTypeEnum type = StructureTypeEnum.getType(args[2]);
                for(StructureManager key: StructureManager.getStructures(args[1], type))
                {
                    commands.add(key.getName());
                }
            }
            if (args[0].equalsIgnoreCase("replace") || args[0].equalsIgnoreCase("add") )
            {
                StructureTypeEnum type = StructureTypeEnum.getType(args[2]);
                for(String key: PlayerManager.instants.getUnlocked((Player) commandSender, args[1], type))
                {
                    commands.add(key.split(":")[2]);
                }
            }

            if (args[0].equalsIgnoreCase("count")) {
                if (args[1].equalsIgnoreCase("set") || args[1].equalsIgnoreCase("add") || args[1].equalsIgnoreCase("remove") || args[1].equalsIgnoreCase("get")) {
                    List<StructureManager> countList = PlayerManager.instants.getCountList(((Player) commandSender));
                    for (StructureManager key: countList) {
                        commands.add(key.getNamespace());
                    }
                }
            }
        }
        if (args.length == 5)
        {
            if (args[0].equalsIgnoreCase("unlock"))
            {
                StructureTypeEnum type = StructureTypeEnum.getType(args[3]);
                List<StructureManager> unlocked = StructureManager.getStructures(args[2], type);
                List<StructureManager> countList = PlayerManager.instants.getCountList(((Player) commandSender));
                unlocked.removeAll(countList);

                for(StructureManager key: unlocked) {
                    commands.add(key.getName());
                }
            }
            if (args[0].equalsIgnoreCase("count")) {
                if (args[1].equalsIgnoreCase("set") || args[1].equalsIgnoreCase("add") || args[1].equalsIgnoreCase("remove") || args[1].equalsIgnoreCase("get")) {
                    List<StructureManager> countList = PlayerManager.instants.getCountList(((Player) commandSender));
                    for (StructureManager key: countList) {
                        if (key.getNamespace().equalsIgnoreCase(args[3])) {
                            commands.add(key.getType().getName());
                        }
                    }
                }
            }
        }
        if (args.length == 6) {
            if (args[0].equalsIgnoreCase("count")) {
                if (args[1].equalsIgnoreCase("set") || args[1].equalsIgnoreCase("add") || args[1].equalsIgnoreCase("remove") || args[1].equalsIgnoreCase("get")) {
                    List<StructureManager> countList = PlayerManager.instants.getCountList(((Player) commandSender));
                    for (StructureManager key : countList) {
                        if (key.getNamespace().equals(args[3]) && key.getType().getName().equals(args[4])) {
                            commands.add(key.getName());
                        }
                    }
                }
            }
        }
        if (args.length == 7) {
            if (args[0].equalsIgnoreCase("count")) {
                if (args[1].equalsIgnoreCase("set") || args[1].equalsIgnoreCase("add") || args[1].equalsIgnoreCase("remove")) {
                    commands.add("#");
                }
            }
        }
        //create a new array
        final List<String> completions = new ArrayList<>();
        //copy matches of first argument from list (ex: if first arg is 'm' will return just 'minecraft')
        StringUtil.copyPartialMatches(args[args.length - 1], commands, completions);
        //sort the list
        Collections.sort(completions);


        return completions;

    }
}
