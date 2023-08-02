package com.firesoftitan.play.titanbox.islands;

import com.firesoftitan.play.titanbox.islands.enums.StructureTypeEnum;
import com.firesoftitan.play.titanbox.islands.guis.CompassGui;
import com.firesoftitan.play.titanbox.islands.guis.ConfirmationDialog;
import com.firesoftitan.play.titanbox.islands.listeners.CompassGUIListener;
import com.firesoftitan.play.titanbox.islands.listeners.MainListener;
import com.firesoftitan.play.titanbox.islands.listeners.ProtectionListener;
import com.firesoftitan.play.titanbox.islands.listeners.TabCompleteListener;
import com.firesoftitan.play.titanbox.islands.managers.*;
import com.firesoftitan.play.titanbox.islands.runnables.CompassRunnable;
import com.firesoftitan.play.titanbox.islands.runnables.IslandRemoverRunnable;
import com.firesoftitan.play.titanbox.islands.runnables.IslandSpawnerRunnable;
import com.firesoftitan.play.titanbox.islands.runnables.SaveRunnable;
import com.firesoftitan.play.titanbox.libs.tools.LibsMessageTool;
import com.firesoftitan.play.titanbox.libs.tools.Tools;
import org.bukkit.*;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
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
    private final List<String> namespaceList = new ArrayList<String>();
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

        File namespacesFolder = new File(tiFilePath, File.separator + "structures" + File.separator);
        for (File folder : Objects.requireNonNull(namespacesFolder.listFiles())) {
            String name = folder.getName();
            if (folder.isDirectory())
            {
                namespaceList.add(name);
                messageTool.sendMessageSystem("Loading Structures from: " + name);
                File structuresFolder = new File(namespacesFolder, File.separator + name + File.separator);
                for (StructureTypeEnum typeEnum: StructureTypeEnum.values())
                {
                    messageTool.sendMessageSystem("--Loading Structures from: " + typeEnum.getName());
                    File typeFolder = new File(structuresFolder, File.separator + typeEnum.getName() + File.separator );
                    if (typeFolder.exists() && typeFolder.isDirectory())
                    {
                        for (File file : Objects.requireNonNull(typeFolder.listFiles())) {
                            String ymlName = file.getName();
                            if (ymlName.toLowerCase().endsWith(".yml")) {
                                messageTool.sendMessageSystem("----Loading Structure: " + ymlName);
                                String newName = ymlName.substring(0, ymlName.length() - 4);
                                try {
                                    new StructureManager(name, typeEnum, newName);
                                } catch (Exception ignore) {
                                    messageTool.sendMessageSystem("Couldn't load file: " + name + ":" +  typeEnum.getName() + ":" + ymlName);
                                }
                            }
                        }
                    }
                }
            }
        }

        IslandManager.loadAll();
        FragmentManager.loadAll();

        new IslandSpawnerRunnable().runTaskTimer(this, configManager.getTime() * 20L, configManager.getTime() * 20L);
        new CompassRunnable().runTaskTimer(this, 10, 10);

        new IslandRemoverRunnable().runTaskTimer(this, 10L, 10L);

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

    public List<String> getNamespaceList() {
        return namespaceList;
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
                    if (!Objects.requireNonNull(((Player) sender).getWorld()).getName().equals(ConfigManager.getInstants().getWorld().getName()))
                    {
                        messageTool.sendMessagePlayer((Player) sender, LangManager.instants.getMessage("error.world"));
                        return true;
                    }
                    CompassGui compassGUI = new CompassGui((Player) sender);
                    compassGUI.showGUI();
                    return true;
                }

            }
            if (label.equalsIgnoreCase("tis") || label.equalsIgnoreCase("island") || label.equalsIgnoreCase("is")) {
                if (args.length > 0) {
                    if (args[0].equalsIgnoreCase("sethome") && sender instanceof Player) {
                        if (!Objects.requireNonNull(((Player) sender).getWorld()).getName().equals(ConfigManager.getInstants().getWorld().getName()))
                        {
                            messageTool.sendMessagePlayer((Player) sender, LangManager.instants.getMessage("error.world"));
                            return true;
                        }
                        FragmentManager fragmentManager = FragmentManager.getFragment(((Player) sender).getLocation());
                        if (fragmentManager == null || !playerManager.isOwnedByPlayer(((Player) sender), fragmentManager)) {
                            messageTool.sendMessagePlayer((Player) sender, LangManager.instants.getMessage("error.must_be_inside"));
                            return true;
                        }
                        playerManager.setHome((Player) sender, ((Player) sender).getLocation().clone());
                        messageTool.sendMessagePlayer((Player) sender, LangManager.instants.getMessage("home_set"));
                        return true;
                    }

                    if (args[0].equalsIgnoreCase("abandon") && sender instanceof Player) {
                        IslandManager island = IslandManager.getIsland(((Player) sender).getLocation());
                        if (island == null) {
                            messageTool.sendMessagePlayer((Player) sender, LangManager.instants.getMessage("error.must_be_inside"));
                            return true;
                        }
                        if (!island.getOwner().equals(((Player) sender).getUniqueId())) {
                            messageTool.sendMessagePlayer((Player) sender, LangManager.instants.getMessage("error.must_be_inside"));
                            return true;
                        }
                        Location home = PlayerManager.instants.getHome((Player) sender);
                        IslandManager island1 = IslandManager.getIsland(home);
                        if (Objects.requireNonNull(island1).getId().equals(island.getId()))
                        {
                            messageTool.sendMessagePlayer((Player) sender, LangManager.instants.getMessage("error.home"));
                            return true;
                        }
                        ConfirmationDialog.show((Player) sender, LangManager.instants.getMessage("fragment_delete"), new BukkitRunnable() {
                            @Override
                            public void run() {
                                PlayerManager.instants.remove((Player) sender, island);
                                for(FragmentManager fragmentManager : island.getFragments())
                                {
                                    PlayerManager.instants.remove((Player) sender, fragmentManager);
                                }
                                messageTool.sendMessagePlayer((Player) sender, LangManager.instants.getMessage("done"));
                            }
                        });


                    }
                    if (args[0].equalsIgnoreCase("claim") && sender instanceof Player) {
                        IslandManager island = IslandManager.getIsland(((Player) sender).getLocation());
                        if (island == null)
                        {
                            messageTool.sendMessagePlayer((Player) sender, LangManager.instants.getMessage("error.must_be_inside2"));
                            return true;
                        }
                        if (island.getOwner() != null)
                        {
                            messageTool.sendMessagePlayer((Player) sender, LangManager.instants.getMessage("error.owner"));
                            return true;
                        }
                        PlayerManager.instants.add((Player) sender, island);
                        for(FragmentManager fragmentManager : island.getFragments())
                        {
                            StructureManager structure = StructureManager.getStructure(fragmentManager.getNamespace(), fragmentManager.getType(), fragmentManager.getName());
                            PlayerManager.instants.add((Player) sender, fragmentManager);
                            int max = structure.getPersonalLimit();
                            String maxNumber = String.valueOf(max);
                            if (max < 0) maxNumber = LangManager.instants.getMessage("unlimited");
                            int count = playerManager.getCount((Player) sender, fragmentManager);
                            messageTool.sendMessagePlayer((Player) sender, ChatColor.AQUA + fragmentManager.getName() + ChatColor.WHITE + " (" + count + "/" + maxNumber + ")");
                        }
                        messageTool.sendMessagePlayer((Player) sender, LangManager.instants.getMessage("done"));
                    }
                    if (args[0].equalsIgnoreCase("home") && sender instanceof Player) {
                        Location home = playerManager.getHome((Player) sender);
                        if (home != null) {
                            ((Player) sender).teleport(home.clone().add(0, 2, 0));
                            messageTool.sendMessagePlayer((Player) sender, LangManager.instants.getMessage("done"));
                        }
                        return true;
                    }
                    if (args[0].equalsIgnoreCase("replace") && sender instanceof Player) {
                        if (!Objects.requireNonNull(((Player) sender).getWorld()).getName().equals(ConfigManager.getInstants().getWorld().getName()))
                        {
                            messageTool.sendMessagePlayer((Player) sender, LangManager.instants.getMessage("error.world"));
                            return true;
                        }
                        FragmentManager fragmentManager = FragmentManager.getFragment(((Player) sender).getLocation());
                        if (fragmentManager == null || !playerManager.isOwnedByPlayer(((Player) sender), fragmentManager)) {
                            messageTool.sendMessagePlayer((Player) sender, LangManager.instants.getMessage("error.must_be_inside"));
                            return true;
                        }
                        Location check = fragmentManager.getCenter();
                        if (args.length > 1) {
                            String plugin = args[1];
                            StructureTypeEnum section = StructureTypeEnum.getType(args[2]);
                            String name = args[3];

                            StructureManager structure = StructureManager.getStructure(plugin, section, name);
                            if (structure == null) {
                                messageTool.sendMessagePlayer((Player) sender, LangManager.instants.getMessage("error.no_structure"));
                                return true;
                            }
                            if (!playerManager.isUnlocked((Player) sender, structure)) {
                                messageTool.sendMessagePlayer((Player) sender, LangManager.instants.getMessage("error.no_unlock"));
                                return true;
                            }
                            int cost = structure.getCost();
                            int level = ((Player) sender).getLevel();
                            if (cost > level) {
                                messageTool.sendMessagePlayer((Player) sender, LangManager.instants.getMessage("error.not_enough") + cost + LangManager.instants.getMessage("levels"));
                                return true;
                            }
                            int max = structure.getPersonalLimit();
                            int count = playerManager.getCount((Player) sender, structure);
                            if (max > -1) {
                                if (count > max) {
                                    messageTool.sendMessagePlayer((Player) sender, LangManager.instants.getMessage("error.limit") + " (" + count + "/" + max + ")");
                                    return true;
                                }
                            }
                            ConfirmationDialog.show((Player) sender, LangManager.instants.getMessage("fragment_replace"),  new BukkitRunnable() {
                                @Override
                                public void run() {
                                    int i = level - cost;
                                    if (i < 0) i = 0;
                                    ((Player) sender).setLevel(i);
                                    UUID owner = PlayerManager.instants.getOwner(fragmentManager);
                                    Location firstCorner = fragmentManager.getCenter();
                                    FragmentManager build = structure.build(firstCorner.clone(), fragmentManager.getIsland().getHeight());
                                    build.place(fragmentManager.getIsland(), structure);
                                    playerManager.add((Player) sender, build);
                                    String maxNumber = String.valueOf(max);
                                    if (max < 0) maxNumber = LangManager.instants.getMessage("unlimited");
                                    messageTool.sendMessagePlayer((Player) sender, LangManager.instants.getMessage("done") + ChatColor.WHITE + " (" + count + "/" + maxNumber + ")");
                                }

                            });
                            return true;
                        }


                    }
                    if (args[0].equalsIgnoreCase("remove") && sender instanceof Player) {
                        if (!Objects.requireNonNull(((Player) sender).getWorld()).getName().equals(ConfigManager.getInstants().getWorld().getName()))
                        {
                            messageTool.sendMessagePlayer((Player) sender, LangManager.instants.getMessage("error.world"));
                            return true;
                        }
                        FragmentManager fragmentManager = FragmentManager.getFragment(((Player) sender).getLocation());
                        if (fragmentManager == null || !playerManager.isOwnedByPlayer(((Player) sender), fragmentManager)) {
                            messageTool.sendMessagePlayer((Player) sender, LangManager.instants.getMessage("error.must_be_inside"));
                            return true;
                        }
                        ConfirmationDialog.show((Player) sender, LangManager.instants.getMessage("fragment_delete"),  new BukkitRunnable() {
                            @Override
                            public void run() {
                                UUID owner = PlayerManager.instants.getOwner(fragmentManager);
                                Location firstCorner = fragmentManager.getCenter();
                                String emptyType = "water";
                                if (ConfigManager.getInstants().getType().equalsIgnoreCase("air")) emptyType = "air";
                                StructureManager structure = StructureManager.getStructure("primary", StructureTypeEnum.INLAND, emptyType);
                                FragmentManager build = structure.build(firstCorner.clone(), fragmentManager.getIsland().getHeight());
                                build.place(fragmentManager.getIsland(), structure);
                                playerManager.add((Player) sender, build);
                                messageTool.sendMessagePlayer((Player) sender, LangManager.instants.getMessage("done"));
                            }
                        });
                        return true;
                    }
                    if (args[0].equalsIgnoreCase("add") && sender instanceof Player) {
                        if (!Objects.requireNonNull(((Player) sender).getWorld()).getName().equals(ConfigManager.getInstants().getWorld().getName()))
                        {
                            messageTool.sendMessagePlayer((Player) sender, LangManager.instants.getMessage("error.world"));
                            return true;
                        }
                        BlockFace facing = ((Player) sender).getFacing();
                        FragmentManager fragmentManager = FragmentManager.getFragment(((Player) sender).getLocation());
                        if (fragmentManager == null || !playerManager.isOwnedByPlayer(((Player) sender), fragmentManager)) {
                            messageTool.sendMessagePlayer((Player) sender, LangManager.instants.getMessage("error.must_be_inside"));
                            return true;
                        }
                        Location check = fragmentManager.getCenter();
                        if (args.length > 1) {
                            String plugin = args[1];
                            StructureTypeEnum section = StructureTypeEnum.getType(args[2]);
                            String name = args[3];
                            StructureManager structure = StructureManager.getStructure(plugin, section, name);
                            if (structure == null) {
                                messageTool.sendMessagePlayer((Player) sender, LangManager.instants.getMessage("error.no_structure"));
                                return true;
                            }
                            if (!playerManager.isUnlocked((Player) sender, structure)) {
                                messageTool.sendMessagePlayer((Player) sender, LangManager.instants.getMessage("error.no_unlock"));
                                return true;
                            }
                            if (facing != BlockFace.EAST && facing != BlockFace.WEST && facing != BlockFace.SOUTH && facing != BlockFace.NORTH) {
                                messageTool.sendMessagePlayer((Player) sender, LangManager.instants.getMessage("error.direction"));
                                return true;
                            }
                            int cost = structure.getCost();
                            int level = ((Player) sender).getLevel();
                            if (cost > level) {
                                messageTool.sendMessagePlayer((Player) sender, LangManager.instants.getMessage("error.not_enough") + cost + LangManager.instants.getMessage("levels"));
                                return true;
                            }

                            int max = structure.getPersonalLimit();
                            int count = playerManager.getCount((Player) sender, structure);
                            if (max > -1) {
                                if (count > max) {
                                    messageTool.sendMessagePlayer((Player) sender, LangManager.instants.getMessage("error.limit") + " (" + count + "/" + max + ")");
                                    return true;
                                }
                            }
                            int i = level - cost;
                            if (i < 0) i = 0;
                            ((Player) sender).setLevel(i);
                            check = FragmentManager.adjustLocation(structure, check, facing);
                            FragmentManager build = structure.build(check, fragmentManager.getIsland().getHeight());
                            if (!FragmentManager.isOverlapping(build)) {
                                IslandManager islandManager = IslandManager.getIsland(fragmentManager);
                                build.place(islandManager, structure);
                                playerManager.add((Player) sender, build);
                                String maxNumber = String.valueOf(max);
                                if (max < 0) maxNumber = LangManager.instants.getMessage("unlimited");
                                messageTool.sendMessagePlayer((Player) sender, LangManager.instants.getMessage("done") + ChatColor.WHITE + " (" + count + "/" + maxNumber + ")");
                            }
                            return true;
                        }
                    }
                    if (args[0].equalsIgnoreCase("friends")) {
                        if (args[1].equalsIgnoreCase("list")) {
                            IslandManager islandManager = IslandManager.getIsland(PlayerManager.instants.getHome((Player) sender));
                            if (islandManager != null) {
                                List<UUID> friends = islandManager.getFriends();
                                List<String> names = new ArrayList<String>();
                                for (UUID friend: friends)
                                {
                                    OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(friend);
                                    if (offlinePlayer != null) names.add(offlinePlayer.getName());
                                }
                                for(String name: names)
                                {
                                    messageTool.sendMessagePlayer((Player) sender, ChatColor.AQUA + name);
                                }
                                messageTool.sendMessagePlayer((Player) sender, LangManager.instants.getMessage("done"));
                                return true;
                            }
                        }
                        if (args[1].equalsIgnoreCase("add")) {
                            if (args.length > 2) {
                                Player player = Bukkit.getPlayer(args[2]);
                                if (player == null) {
                                    messageTool.sendMessagePlayer((Player) sender, LangManager.instants.getMessage("friends.not_online"));
                                    return true;
                                }
                                IslandManager islandManager = IslandManager.getIsland(PlayerManager.instants.getHome((Player) sender));
                                if (islandManager != null) {
                                    islandManager.addFriend(player);
                                    messageTool.sendMessagePlayer((Player) sender, LangManager.instants.getMessage("friends.added"));
                                    return true;
                                }
                            }
                        }
                        if (args[1].equalsIgnoreCase("remove")) {
                            Player player = Bukkit.getPlayer(args[2]);
                            if (args.length > 2) {
                                if (player == null) {
                                    messageTool.sendMessagePlayer((Player) sender, LangManager.instants.getMessage("friends.not_online"));
                                    return true;
                                }
                                IslandManager islandManager = IslandManager.getIsland(PlayerManager.instants.getHome((Player) sender));
                                if (islandManager != null) {
                                    islandManager.removeFriend(player);
                                    messageTool.sendMessagePlayer((Player) sender, LangManager.instants.getMessage("friends.removed"));
                                    return true;
                                }
                            }
                        }
                    }
                    if (args[0].equalsIgnoreCase("total")) {
                        messageTool.sendMessagePlayer((Player) sender, LangManager.instants.getMessage("count") + IslandManager.getCount());
                        return true;
                    }
                    if (args[0].equalsIgnoreCase("info")) {
                        if (!Objects.requireNonNull(((Player) sender).getWorld()).getName().equals(ConfigManager.getInstants().getWorld().getName()))
                        {
                            messageTool.sendMessagePlayer((Player) sender, LangManager.instants.getMessage("error.world"));
                            return true;
                        }
                        FragmentManager fragmentManager = FragmentManager.getFragment(((Player) sender).getLocation());

                        if (fragmentManager == null) {
                            messageTool.sendMessagePlayer((Player) sender,"&aIs Island: &fwild");
                            messageTool.sendMessagePlayer((Player) sender,"&aOwner: &fwild");
                        } else
                        {
                            IslandManager islandManager = IslandManager.getIsland(fragmentManager);
                            messageTool.sendMessagePlayer((Player) sender,"&aIs Island: &f" + (islandManager != null));

                            UUID owner = PlayerManager.instants.getOwner(fragmentManager);
                            if (owner != null)
                            {
                                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(owner);
                                messageTool.sendMessagePlayer((Player) sender,"&aOwner: &f" + offlinePlayer.getName());
                            }
                            else {
                                messageTool.sendMessagePlayer((Player) sender,"&aOwner: &fNot owned");
                            }
                        }

                        return true;
                    }
                }
                if (isAdmin(sender)) {
                    if (args.length > 0) {
                        if (args[0].equalsIgnoreCase("unlock")) {
                            String playerName = args[1];
                            String plugin = args[2];
                            StructureTypeEnum section = StructureTypeEnum.getType(args[3]);
                            String name = args[4];
                            //noinspection deprecation
                            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);
                            StructureManager structure = StructureManager.getStructure(plugin, section, name);
                            if (structure == null)
                            {
                                messageTool.sendMessagePlayer((Player) sender, LangManager.instants.getMessage("error.no_structure"));
                                return true;
                            }
                            //noinspection deprecation
                            if (name == null || Bukkit.getOfflinePlayer(playerName) == null)
                            {
                                messageTool.sendMessagePlayer((Player) sender, LangManager.instants.getMessage("error.player"));
                                return true;
                            }
                            boolean unlocked = playerManager.isUnlocked(offlinePlayer.getUniqueId(), structure);
                            if (!unlocked && !name.equalsIgnoreCase("water")&& !name.equalsIgnoreCase("air")) {
                                playerManager.unlock(offlinePlayer.getUniqueId(), structure);

                                if (offlinePlayer.isOnline()) {
                                    Player player = offlinePlayer.getPlayer();
                                    if (player != null) {
                                        messageTool.sendMessagePlayer(player, LangManager.instants.getMessage("unlocked") + structure.getTitle());
                                        int personalLimit = structure.getPersonalLimit();
                                        String txtAmount = String.valueOf(personalLimit);
                                        if (personalLimit == -1)
                                            txtAmount = LangManager.instants.getMessage("unlimited");
                                        if (personalLimit == 0) txtAmount = LangManager.instants.getMessage("none");
                                        txtAmount = txtAmount + LangManager.instants.getMessage("these");
                                        messageTool.sendMessagePlayer(player, LangManager.instants.getMessage("build") + txtAmount);
                                        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
                                    }
                                }
                                messageTool.sendMessagePlayer((Player) sender, LangManager.instants.getMessage("done"));
                                return true;
                            }
                            else {
                                messageTool.sendMessagePlayer((Player) sender, LangManager.instants.getMessage("error.unlocked"));
                                return true;
                            }
                        }
                        if (args[0].equalsIgnoreCase("count")) {
                            String playerName = args[2];
                            String plugin = args[3];
                            StructureTypeEnum section = StructureTypeEnum.getType(args[4]);
                            String name = args[5];
                            StructureManager structure = StructureManager.getStructure(plugin, section, name);
                            if (structure == null)
                            {
                                messageTool.sendMessagePlayer((Player) sender, LangManager.instants.getMessage("error.no_structure"));
                                return true;
                            }
                            //noinspection deprecation
                            if (name == null || Bukkit.getOfflinePlayer(playerName) == null)
                            {
                                messageTool.sendMessagePlayer((Player) sender, LangManager.instants.getMessage("error.player"));
                                return true;
                            }
                            //noinspection deprecation
                            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);
                            int count = playerManager.getCount(offlinePlayer.getUniqueId(), structure);
                            int amount = 1;
                            if (args[1].equalsIgnoreCase("remove")) {
                                if (args.length == 7) amount = Integer.parseInt(args[6]);
                                int amount1 = count - amount;
                                if (amount1 < 0) amount1 = 0;
                                playerManager.setCount(offlinePlayer.getUniqueId(), structure, amount1);
                            }
                            if (args[1].equalsIgnoreCase("add")) {
                                if (args.length == 7) amount = Integer.parseInt(args[6]);
                                playerManager.setCount(offlinePlayer.getUniqueId(), structure,count + amount);
                            }
                            if (args[1].equalsIgnoreCase("set")) {
                                if (args.length == 7) amount = Integer.parseInt(args[6]);
                                if (amount < 0) amount = 0;
                                playerManager.setCount(offlinePlayer.getUniqueId(), structure,amount);
                            }
                            count = playerManager.getCount(offlinePlayer.getUniqueId(), structure);
                            int max = structure.getPersonalLimit();
                            String maxNumber = String.valueOf(max);
                            if (max < 0) maxNumber = LangManager.instants.getMessage("unlimited");
                            messageTool.sendMessagePlayer((Player) sender, ChatColor.AQUA + playerName + ChatColor.WHITE + " (" + count + "/" + maxNumber + ")");

                            return true;
                        }
                        if (args[0].equalsIgnoreCase("delete")) {
                            IslandManager island = IslandManager.getIsland(((Player) sender).getLocation());
                            if (island == null)
                            {
                                messageTool.sendMessagePlayer((Player) sender, LangManager.instants.getMessage("error.must_be_inside2"));
                                return true;
                            }
                            ConfirmationDialog.show((Player) sender, LangManager.instants.getMessage("island_delete"), new BukkitRunnable() {
                                @Override
                                public void run() {
                                    IslandRemoverRunnable islandRemoverRunnable = new IslandRemoverRunnable(island);
                                    islandRemoverRunnable.runTaskTimer(TitanIslands.instance, 10, 10);
                                }
                            });
                            return true;
                        }
                        if (args[0].equalsIgnoreCase("admin")) {
                            boolean b = TitanIslands.toggleAdminMode((Player) sender);
                            if (b) {
                                messageTool.sendMessagePlayer((Player) sender, LangManager.instants.getMessage("admin_mode_on"));
                            } else {
                                messageTool.sendMessagePlayer((Player) sender, LangManager.instants.getMessage("admin_mode_off"));
                            }
                            return true;
                        }
                        if (args[0].equalsIgnoreCase("spawn")) {
                            IslandSpawnerRunnable.instance.run();
                            return true;
                        }
                        if (args[0].equalsIgnoreCase("build") && sender instanceof Player) {
                            if (!Objects.requireNonNull(((Player) sender).getWorld()).getName().equals(ConfigManager.getInstants().getWorld().getName()))
                            {
                                messageTool.sendMessagePlayer((Player) sender, LangManager.instants.getMessage("error.world"));
                                return true;
                            }
                            String plugin = args[1];
                            StructureTypeEnum section = StructureTypeEnum.getType(args[2]);
                            String name = args[3];

                            IslandManager islandManager = new IslandManager();
                            StructureManager structure = StructureManager.getStructure(plugin, section, name);
                            Location location = ((Player) sender).getLocation().clone();
                            FragmentManager preBuild = structure.getPreBuild(location, islandManager.getHeight());
                            boolean anyFragments = FragmentManager.isOverlapping(preBuild);
                            if (anyFragments) {
                                messageTool.sendMessagePlayer((Player) sender, LangManager.instants.getMessage("error.inside"));
                                return true;
                            }
                            //make sure nothing got mess-up when we checked it
                            structure = StructureManager.getStructure(plugin, section, name);
                            location = ((Player) sender).getLocation().clone();
                            FragmentManager build = structure.build(location, islandManager.getHeight());
                            build.place(new IslandManager(), structure);
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
        FragmentManager.saveAll();
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
