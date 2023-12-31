package com.firesoftitan.play.titanbox.islands.managers;

import com.firesoftitan.play.titanbox.islands.TitanIslands;
import com.firesoftitan.play.titanbox.libs.managers.SaveManager;
import org.bukkit.ChatColor;

public class LangManager {
    private final SaveManager configFile;
    public static LangManager instants;

    public LangManager(String lang_file) {
        configFile = new SaveManager(TitanIslands.instance.getName(), "lang" , lang_file);
        instants = this;
        String version = configFile.getString("version");

    }
    public boolean contains(String key)
    {
        return this.configFile.contains(key);
    }

    public String getMessage(String key) {
        if (!contains(key)) return ChatColor.RED + "KEY NOT FOUND " + key;
        String string = this.configFile.getString(key);
        string = ChatColor.translateAlternateColorCodes('&',string);
        return string;
    }
}
