package com.firesoftitan.play.titanbox.islands;

import com.firesoftitan.play.titanbox.islands.tools.CubeSelectorTool;
import com.firesoftitan.play.titanbox.libs.managers.SaveManager;
import com.firesoftitan.play.titanbox.libs.managers.SettingsManager;
import com.firesoftitan.play.titanbox.libs.tools.LibsMessageTool;
import com.firesoftitan.play.titanbox.libs.tools.Tools;
import com.firesoftitan.play.titanbox.islands.listeners.MainListener;
import com.firesoftitan.play.titanbox.islands.listeners.TabCompleteListener;
import com.firesoftitan.play.titanbox.islands.managers.*;
import com.firesoftitan.play.titanbox.islands.runnables.SaveRunnable;
import org.bukkit.Location;
import org.bukkit.block.structure.StructureRotation;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class TitanIslands extends JavaPlugin {
    public static Tools tools;
    public static ConfigManager configManager;
    public static TitanIslands instance;
    public static MainListener mainListener;
    public static LibsMessageTool messageTool;
    public static File tiFilePath;
    public void onEnable() {
        instance = this;
        tools = new Tools(this, new SaveRunnable(), 99835);
        messageTool = tools.getMessageTool();
        mainListener = new MainListener();
        mainListener.registerEvents();
        saveDefaultFiles();
        configManager = new ConfigManager();
        new LangManager(configManager.getLanguage());
        tiFilePath = new File("plugins" + File.separator + this.getDescription().getName() + File.separator);
        this.getCommand("tis").setTabCompleter(new TabCompleteListener());
        this.getCommand("island").setTabCompleter(new TabCompleteListener());
        this.getCommand("is").setTabCompleter(new TabCompleteListener());

        System.out.println("Loading Structures...");
        File structuresFolder = new File(tiFilePath, File.separator + "structures" + File.separator);
        for (File file : structuresFolder.listFiles()) {
            String name = file.getName();
            if (name.toLowerCase().endsWith(".nbt")) {
                String newName = name.substring(0, name.length() - 4);
                new StructureManager(newName);
                System.out.println("Loaded: " + newName);
            }
        }
        System.out.println("Done Structures");
    }

    private void saveDefaultFiles() {
        List<String> jarFileList = getJarFiles();
        for(String s: jarFileList)
        {
            if (!s.endsWith("sample.yml")) {
                System.out.println(s);
                saveDefaultFile(s);
            }
        }
    }

    private void saveDefaultFile(String fileName) {
        String jarFileName = fileName;
        fileName = fileName.substring(9);
        File file = new File(getDataFolder(), fileName);

        if (!file.exists()) {
            // Create the parent directories if they don't exist
            file.getParentFile().mkdirs();

            try (InputStream inputStream = getClass().getResourceAsStream( jarFileName)) {
                if (inputStream != null) {
                    // Copy the file from the JAR to the plugin folder
                    if (!file.exists()) Files.copy(inputStream, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
                } else {
                    getLogger().warning(LangManager.instants.getMessage("error.default_file") + fileName);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    private List<String> getJarFiles() {
        String directoryName = "defaults";
        List<String> jarFiles = new ArrayList<>();

        try {
            // Get the JAR file path
            Path jarPath = Paths.get(getClass().getProtectionDomain().getCodeSource().getLocation().toURI());

            if (jarPath != null && Files.isRegularFile(jarPath)) {
                // Open the JAR file system
                try (FileSystem jarFileSystem = FileSystems.newFileSystem(jarPath)) {
                    // Specify the path of the directory within the JAR
                    Path directoryPath = jarFileSystem.getPath("/" + directoryName);

                    // Iterate over all files in the specified directory in the JAR
                    Files.walk(directoryPath)
                            .filter(Files::isRegularFile)
                            .forEach(filePath -> jarFiles.add(filePath.toString()));
                }
            }
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }

        return jarFiles;
    }

    public void onDisable()
    {
        this.saveALL();
    }
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, String label, String[] args) {
        try {
            if (isAdmin(sender)) {
                if (label.equalsIgnoreCase("tis") || label.equalsIgnoreCase("island") || label.equalsIgnoreCase("is")) {
                    if (args.length > 0) {
                        StructureManager structure = StructureManager.getStructure(args[0]);
                        Location location = ((Player) sender).getLocation().clone();
                        CubeSelectorTool preBuild = structure.getPreBuild(location);
                        boolean anyCubes = CubeSelectorTool.inAnyCubes(preBuild);
                        if (anyCubes)
                        {
                            if (sender instanceof Player)
                            {
                                messageTool.sendMessagePlayer((Player) sender,LangManager.instants.getMessage("error.inside"));
                            }
                            else
                            {
                                messageTool.sendMessageSystem(LangManager.instants.getMessage("error.inside"));
                            }
                            return true;
                        }
                        //make sure nothing got mess-up when we checked it
                        structure = StructureManager.getStructure(args[0]);
                        location = ((Player) sender).getLocation().clone();
                        CubeSelectorTool build = structure.build(location);
                        build.place();

                    }
                }
            }
        } catch (Exception e) {
            if (sender instanceof Player)
            {
                messageTool.sendMessagePlayer((Player) sender,LangManager.instants.getMessage("error.understand"));
            }
            else
            {
                messageTool.sendMessageSystem(LangManager.instants.getMessage("error.understand"));
            }
        }

        return true;
    }

    public void saveALL()
    {

    }
    public static boolean isAdmin(CommandSender sender)
    {
        if (sender.isOp() || sender.hasPermission("titanbox.admin")) return true;
        return false;
    }
}
