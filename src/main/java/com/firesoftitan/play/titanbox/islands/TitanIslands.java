package com.firesoftitan.play.titanbox.islands;

import com.firesoftitan.play.titanbox.islands.guis.CompassGui;
import com.firesoftitan.play.titanbox.islands.listeners.CompassGUIListener;
import com.firesoftitan.play.titanbox.islands.listeners.MainListener;
import com.firesoftitan.play.titanbox.islands.listeners.ProtectionListener;
import com.firesoftitan.play.titanbox.islands.listeners.TabCompleteListener;
import com.firesoftitan.play.titanbox.islands.managers.*;
import com.firesoftitan.play.titanbox.islands.runnables.CompassRunnable;
import com.firesoftitan.play.titanbox.islands.runnables.IslandSpawnerRunnable;
import com.firesoftitan.play.titanbox.islands.runnables.SaveRunnable;
import com.firesoftitan.play.titanbox.libs.tools.LibsMessageTool;
import com.firesoftitan.play.titanbox.libs.tools.Tools;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.util.*;


public class TitanIslands extends JavaPlugin {
    public static Tools tools;
    public static ConfigManager configManager;
    public static TitanIslands instance;
    public static MainListener mainListener;
    public static ProtectionListener protectionListener;
    public static LibsMessageTool messageTool;
    public static File tiFilePath;
    public static PlayerManager playerManager;
    public void onEnable() {
        instance = this;
        tools = new Tools(this, new SaveRunnable(), 99835);
        messageTool = tools.getMessageTool();
        mainListener = new MainListener();
        mainListener.registerEvents();
        protectionListener = new ProtectionListener();
        protectionListener.registerEvents();
        CompassGUIListener compassGUIListener = new CompassGUIListener();
        compassGUIListener.registerEvents();
        saveDefaultFiles();
        configManager = new ConfigManager();
        playerManager = new PlayerManager();
        new LangManager(configManager.getLanguage());
        tiFilePath = new File("plugins" + File.separator + this.getDescription().getName() + File.separator);
        Objects.requireNonNull(this.getCommand("tis")).setTabCompleter(new TabCompleteListener());
        Objects.requireNonNull(this.getCommand("island")).setTabCompleter(new TabCompleteListener());
        Objects.requireNonNull(this.getCommand("is")).setTabCompleter(new TabCompleteListener());

        File structuresFolder = new File(tiFilePath, File.separator + "structures" + File.separator);
        for (File file : Objects.requireNonNull(structuresFolder.listFiles())) {
            String name = file.getName();
            if (name.toLowerCase().endsWith(".yml")) {
                String newName = name.substring(0, name.length() - 4);
                new StructureManager(newName);
            }
        }

        IslandManager.loadAll();
        CubeManager.loadAll();

        new IslandSpawnerRunnable().runTaskTimer(this, configManager.getTime() * 20L, configManager.getTime() * 20L);
        new CompassRunnable().runTaskTimer(this, 10, 10);

    }

    private void saveDefaultFiles() {
        List<String> jarFileList = getJarFiles();
        for(String s: jarFileList)
        {
            if (!s.endsWith("sample.yml")) {
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
                getLogger().severe("Error saving " + fileName +" to "+ file.getPath());
                getLogger().severe(e.getMessage());
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
        CompassRunnable.instance.shutdown();
    }
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        try {
            if (label.equalsIgnoreCase("com") || label.equalsIgnoreCase("cp") || label.equalsIgnoreCase("compass"))
            {
                if (sender instanceof Player) {
                    CompassGui compassGUI = new CompassGui((Player) sender);
                    compassGUI.showGUI();
                    return true;
                }

            }
            if (label.equalsIgnoreCase("tis") || label.equalsIgnoreCase("island") || label.equalsIgnoreCase("is")) {
                if (args.length > 0) {
                    if (args[0].equalsIgnoreCase("sethome") && sender instanceof Player) {
                        CubeManager cubeManager = CubeManager.getCube(((Player) sender).getLocation());
                        if (cubeManager == null || !playerManager.isOwnedByPlayer(((Player) sender), cubeManager)) {
                            messageTool.sendMessagePlayer((Player) sender, LangManager.instants.getMessage("error.mustbeinside"));
                            return true;
                        }
                        playerManager.setHome((Player) sender, ((Player) sender).getLocation().clone());
                        messageTool.sendMessagePlayer((Player) sender, LangManager.instants.getMessage("homeset"));
                        return true;
                    }
                    if (args[0].equalsIgnoreCase("home") && sender instanceof Player) {
                        Location home = playerManager.getHome((Player) sender);
                        if (home != null) {
                            ((Player) sender).teleport(home.clone().add(0, 2, 0));
                            messageTool.sendMessagePlayer((Player) sender, LangManager.instants.getMessage("done"));
                        }
                        return true;
                    }
                    if (args[0].equalsIgnoreCase("add") && sender instanceof Player) {
                        BlockFace facing = ((Player) sender).getFacing();
                        CubeManager cubeManager = CubeManager.getCube(((Player) sender).getLocation());
                        if (cubeManager == null || !playerManager.isOwnedByPlayer(((Player) sender), cubeManager)) {
                            messageTool.sendMessagePlayer((Player) sender, LangManager.instants.getMessage("error.mustbeinside"));
                            return true;
                        }
                        Location check = cubeManager.getCenter();
                        String name = args[1].toLowerCase();
                        StructureManager structure = StructureManager.getStructure(name);
                        if (structure == null) {
                            messageTool.sendMessagePlayer((Player) sender, LangManager.instants.getMessage("error.nostructer"));
                            return true;
                        }
                        if (!playerManager.isUnlocked((Player) sender, name)) {
                            messageTool.sendMessagePlayer((Player) sender, LangManager.instants.getMessage("error.nounlock"));
                            return true;
                        }
                        if (facing != BlockFace.EAST && facing != BlockFace.WEST && facing != BlockFace.SOUTH && facing != BlockFace.NORTH) {
                            messageTool.sendMessagePlayer((Player) sender, LangManager.instants.getMessage("error.direction"));
                            return true;
                        }
                        int cost = structure.getCost();
                        int level = ((Player) sender).getLevel();
                        if (cost > level) {
                            messageTool.sendMessagePlayer((Player) sender, LangManager.instants.getMessage("error.notenough") + cost + LangManager.instants.getMessage("levels"));
                            return true;
                        }

                        int max = structure.getPersonalLimit();
                        int count = playerManager.getCount((Player) sender, name);
                        if (max > -1) {
                            if (count >= max) {
                                messageTool.sendMessagePlayer((Player) sender, LangManager.instants.getMessage("error.limit") + " (" + count + "/" + max + ")");
                                return true;
                            }
                        }
                        int i = level - cost;
                        if (i < 0) i = 0;
                        ((Player) sender).setLevel(i);
                        check = CubeManager.adjustLocation(name, check, facing);
                        CubeManager build = structure.build(check);
                        if (!CubeManager.isOverlapping(build)) {
                            IslandManager islandManager = IslandManager.getIsland(cubeManager);
                            playerManager.add((Player) sender, build);
                            build.place(islandManager, structure.getName());
                            messageTool.sendMessagePlayer((Player) sender, LangManager.instants.getMessage("done"));
                        }
                        return true;
                    }
                    if (args[0].equalsIgnoreCase("count")) {

                        messageTool.sendMessagePlayer((Player) sender, LangManager.instants.getMessage("count") + IslandManager.getCount());
                    }
                    if (args[0].equalsIgnoreCase("info")) {
                        CubeManager cubeManager = CubeManager.getCube(((Player) sender).getLocation());

                        if (cubeManager == null) {
                            sender.sendMessage("IM: wild");
                            sender.sendMessage("OWNED: wild");
                        } else
                        {
                            IslandManager islandManager = IslandManager.getIsland(cubeManager);
                            sender.sendMessage("IM: " + (islandManager != null));
                            sender.sendMessage("OWNED: " + PlayerManager.instants.isOwnedByPlayer((Player) sender, cubeManager));
                        }

                        return true;
                    }
                }
                if (isAdmin(sender)) {
                    if (args.length > 0) {
                        if (args[0].equalsIgnoreCase("admin")) {
                            boolean b = TitanIslands.toggleAdminMode((Player) sender);
                            if (b) {
                                messageTool.sendMessagePlayer((Player) sender, LangManager.instants.getMessage("adminmodeon"));
                            } else {
                                messageTool.sendMessagePlayer((Player) sender, LangManager.instants.getMessage("adminmodeoff"));
                            }
                            return true;
                        }
                        if (args[0].equalsIgnoreCase("spawn")) {
                            IslandSpawnerRunnable.instance.run();
                            return true;
                        }
                        if (args[0].equalsIgnoreCase("build") && sender instanceof Player) {
                            StructureManager structure = StructureManager.getStructure(args[1]);
                            Location location = ((Player) sender).getLocation().clone();
                            CubeManager preBuild = structure.getPreBuild(location);
                            boolean anyCubes = CubeManager.isOverlapping(preBuild);
                            if (anyCubes) {
                                messageTool.sendMessagePlayer((Player) sender, LangManager.instants.getMessage("error.inside"));
                                return true;
                            }
                            //make sure nothing got mess-up when we checked it
                            structure = StructureManager.getStructure(args[1]);
                            location = ((Player) sender).getLocation().clone();
                            CubeManager build = structure.build(location);
                            build.place(new IslandManager(), structure.getName());
                        }


                    }

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
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
        StructureManager.save();
        IslandManager.saveAll();
        CubeManager.saveAll();
        playerManager.save();
    }
    private static final Map<UUID, Boolean> adminMode = new HashMap<UUID, Boolean>();
    public static boolean getAdminMode(Player player)
    {
        UUID uniqueId = player.getUniqueId();
        if (!adminMode.containsKey(uniqueId)) adminMode.put(uniqueId, false);
        return adminMode.get(uniqueId);
    }
    public static boolean toggleAdminMode(Player player)
    {
        UUID uniqueId = player.getUniqueId();
        if (!adminMode.containsKey(uniqueId)) adminMode.put(uniqueId, false);
        boolean adMode = !adminMode.get(uniqueId);
        adminMode.put(uniqueId, adMode);
        return adMode;
    }
    public static boolean isAdmin(CommandSender sender)
    {
        return sender.isOp() || sender.hasPermission("titanbox.admin");
    }
}
