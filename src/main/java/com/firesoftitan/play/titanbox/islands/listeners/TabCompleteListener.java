package com.firesoftitan.play.titanbox.islands.listeners;

import com.firesoftitan.play.titanbox.islands.TitanIslands;
import com.firesoftitan.play.titanbox.islands.managers.StructureManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TabCompleteListener implements TabCompleter {
    private static final String[] ADMIN_COMMANDS = { "spawn", "build", "admin"};
    private static final String[] NON_ADMIN_COMMANDS = {"home", "add", "sethome"};
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
            if (args[0].equalsIgnoreCase("give"))
            {
                return null;
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
        //create new array
        final List<String> completions = new ArrayList<>();
        //copy matches of first argument from list (ex: if first arg is 'm' will return just 'minecraft')
        StringUtil.copyPartialMatches(args[args.length - 1], Commands, completions);
        //sort the list
        Collections.sort(completions);

        return completions;

    }
}
