package com.firesoftitan.play.titanbox.islands.guis;

import com.firesoftitan.play.titanbox.islands.TitanIslands;
import com.firesoftitan.play.titanbox.islands.managers.LangManager;
import com.firesoftitan.play.titanbox.islands.managers.PlayerManager;
import com.firesoftitan.play.titanbox.islands.managers.StructureManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.util.*;

public class FragmentsTopGui {
    private static final HashMap<UUID, FragmentsTopGui> activeGuis = new HashMap<UUID, FragmentsTopGui>();
    private final Inventory myGui;
    private final int size = 9*6;//54
    private Player viewer;
    public static String guiName = LangManager.instants.getMessage("gui.fragments.title");
    public static FragmentsTopGui getGui(Player player)
    {
        if (activeGuis.containsKey(player.getUniqueId())) {
            return activeGuis.get(player.getUniqueId());
        }
        return null;
    }
    public FragmentsTopGui(Player player) {
        myGui = Bukkit.createInventory(null, size, guiName);
        this.viewer = player;
    }

    private void startDraw() {

        ItemStack button = getCustomButton();
        for (int i = 0; i < size; i++) {
            myGui.setItem(i, button.clone());
        }
        drawInterface();


    }
    private ItemStack getCustomButton() {
        return getCustomButton((Material)null);
    }
    private ItemStack getCustomButton(String skullTextureID) {

        ItemStack button = TitanIslands.tools.getSkullTool().getSkull(skullTextureID);
        return getCustomButton(button);
    }
    private ItemStack getCustomButton(Material material) {
        if (material == null) material = Material.GRAY_STAINED_GLASS_PANE;
        ItemStack button = new ItemStack(material);
        button = TitanIslands.tools.getItemStackTool().changeName(button, " ");

        return button.clone();
    }
    private ItemStack getCustomButton(ItemStack itemStack) {
        if (itemStack == null) itemStack = getCustomButton();
        ItemStack button = itemStack.clone();
        button = TitanIslands.tools.getItemStackTool().changeName(button, " ");

        return button.clone();
    }
    static class NameAmount {
        OfflinePlayer offlinePlayer;
        int amount;

        public NameAmount(OfflinePlayer offlinePlayer, int amount) {
            this.offlinePlayer = offlinePlayer;
            this.amount = amount;
        }
    }
    public void drawInterface() {
        ItemStack button;
        int slot = 0;
        int fragmentlistTotal = StructureManager.getAllStructureAsList().size();
        List<UUID> uuids = PlayerManager.getPlayers();
        List<NameAmount> nameAmounts = new ArrayList<NameAmount>();
        int total = uuids.size();
        for (UUID uuid: uuids)
        {
            int count = PlayerManager.getUnlocked(uuid).size();
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
            nameAmounts.add(new NameAmount(offlinePlayer, count));
        }
        nameAmounts.sort((a, b) -> b.amount - a.amount);
        int place = 0;
        for(NameAmount amount: nameAmounts)
        {

            try {
                place++;
                String playersTexture = TitanIslands.tools.getPlayerTool().getPlayersTexture(amount.offlinePlayer);
                button = getCustomButton(playersTexture);
                button = TitanIslands.tools.getItemStackTool().changeName(button, ChatColor.GREEN + "#" + place + "/" + total + ": " +ChatColor.WHITE + amount.offlinePlayer.getName() );
                button = TitanIslands.tools.getItemStackTool().addLore(button, LangManager.instants.getMessage("gui.fragments.line_a") + ChatColor.WHITE + amount.amount+ "/" + fragmentlistTotal, LangManager.instants.getMessage("gui.fragments.line_b"));
                button = TitanIslands.tools.getNBTTool().set(button, "buttonaction", "location");
                button = TitanIslands.tools.getNBTTool().set(button, "location", PlayerManager.getHome(amount.offlinePlayer.getUniqueId()));

                myGui.setItem(slot, button.clone());
                slot++;
                if (slot >= size) break;
            } catch (IOException ignored) {

            }
        }

    }

    public boolean isGuiOpen()
    {
        if (viewer != null) {
            return viewer.getOpenInventory().getTitle().equals(guiName);
        }
        return false;
    }
    public Player getViewer()
    {
        if (viewer != null) {
            if (viewer.getOpenInventory().getTitle().equals(guiName)) {
                return viewer;
            }
        }
        viewer = null;
        return null;
    }
    public Inventory getMyGui() {
        return myGui;
    }

    public int getSize() {
        return size;
    }
    public void showGUI()
    {
        activeGuis.put(viewer.getUniqueId(), this);
        startDraw();
        viewer.openInventory(myGui);
    }

}
