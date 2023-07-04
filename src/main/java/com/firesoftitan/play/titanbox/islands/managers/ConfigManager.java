package com.firesoftitan.play.titanbox.islands.managers;

import com.firesoftitan.play.titanbox.libs.managers.SaveManager;
import com.firesoftitan.play.titanbox.islands.TitanIslands;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ConfigManager {
    private SaveManager configFile;
    private String language;
    public ConfigManager() {
        reload();
    }
    public void reload()
    {
        configFile = new SaveManager(TitanIslands.instance.getName(), "config");


        if (!configFile.contains("settings.language"))
        {
            configFile.set("settings.language", "en_us");
        }

        this.language = configFile.getString("settings.language");
        configFile.save();

    }

    public String getLanguage() {
        return language;
    }

}
