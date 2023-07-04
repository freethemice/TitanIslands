package com.firesoftitan.play.titanbox.islands.listeners;

import com.firesoftitan.play.titanbox.islands.TitanIslands;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class TabCompleteListener implements TabCompleter {
    private static final String[] ADMIN_COMMANDS = { };
    private static final String[] NON_ADMIN_COMMANDS = { };
    private List<String> pluginNames = new ArrayList<String>();
    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        List<String> Commands = new ArrayList<String>();
        if (args.length == 1) {
            if (TitanIslands.isAdmin(commandSender)) {
                Commands.addAll(List.of(ADMIN_COMMANDS));
            }
            else {
                Commands.addAll(List.of(NON_ADMIN_COMMANDS));
            }
        }
        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("give"))
            {
                return null;
            }
        }
        if (args.length == 3) {

        }
        if (args.length == 4) {

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
