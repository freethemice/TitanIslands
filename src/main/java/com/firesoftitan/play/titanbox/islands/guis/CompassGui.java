package com.firesoftitan.play.titanbox.islands.guis;

import com.firesoftitan.play.titanbox.islands.TitanIslands;
import com.firesoftitan.play.titanbox.islands.managers.LangManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.UUID;

public class CompassGui {
    private static final HashMap<UUID, CompassGui> activeGuis = new HashMap<UUID, CompassGui>();
    private final Inventory myGui;
    private final int size;
    private Player viewer;
    public static String guiName = LangManager.instants.getMessage("gui.compass.title");
    public static CompassGui getGui(Player player)
    {
        if (activeGuis.containsKey(player.getUniqueId())) {
            return activeGuis.get(player.getUniqueId());
        }
        return null;
    }
    public CompassGui(Player player) {
        this.size = 9;
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

    public void drawInterface() {
        ItemStack button;
        int slot = 0;

        button = getCustomButton(Material.BARRIER);
        button = TitanIslands.tools.getItemStackTool().changeName(button, LangManager.instants.getMessage("gui.compass.none"));
        button = TitanIslands.tools.getNBTTool().set(button, "buttonaction", "none");
        myGui.setItem(slot, button.clone());
        slot++;

        button = getCustomButton("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjg1NDA2MGFhNTc3NmI3MzY2OGM4OTg2NTkwOWQxMmQwNjIyNDgzZTYwMGI2NDZmOTBjMTg2YzY1Yjc1ZmY0NSJ9fX0=");
        button = TitanIslands.tools.getItemStackTool().changeName(button, LangManager.instants.getMessage("gui.compass.home"));
        button = TitanIslands.tools.getNBTTool().set(button, "buttonaction", "home");
        myGui.setItem(slot, button.clone());
        slot++;

        button = getCustomButton("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDUyOGVkNDU4MDI0MDBmNDY1YjVjNGUzYTZiN2E5ZjJiNmE1YjNkNDc4YjZmZDg0OTI1Y2M1ZDk4ODM5MWM3ZCJ9fX0=");
        button = TitanIslands.tools.getItemStackTool().changeName(button, LangManager.instants.getMessage("gui.compass.closest"));
        button = TitanIslands.tools.getNBTTool().set(button, "buttonaction", "closest");
        myGui.setItem(slot, button.clone());
        slot++;

        button = getCustomButton("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMmU1ZDEwZjlmMzI0NjU5OTY1OGUwYzZkMDQ0NGU4NzRmZmFjMDE0MTA0NDBjNWNmZWM2ZjE5ZDNhYTg4Zjk0NSJ9fX0=");
        button = TitanIslands.tools.getItemStackTool().changeName(button, LangManager.instants.getMessage("gui.compass.newest"));
        button = TitanIslands.tools.getNBTTool().set(button, "buttonaction", "newest");
        myGui.setItem(slot, button.clone());
        slot++;

        button = getCustomButton("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzg4MWNjMjc0N2JhNzJjYmNiMDZjM2NjMzMxNzQyY2Q5ZGUyNzFhNWJiZmZkMGVjYjE0ZjFjNmE4YjY5YmM5ZSJ9fX0=");
        button = TitanIslands.tools.getItemStackTool().changeName(button, LangManager.instants.getMessage("gui.compass.random"));
        button = TitanIslands.tools.getNBTTool().set(button, "buttonaction", "random");
        myGui.setItem(slot, button.clone());
        //slot++;

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
