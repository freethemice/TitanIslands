package com.firesoftitan.play.titanbox.islands.guis;

import com.firesoftitan.play.titanbox.islands.TitanIslands;
import com.firesoftitan.play.titanbox.islands.managers.*;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.util.*;

public class IslandTopGui {
    private static final HashMap<UUID, IslandTopGui> activeGuis = new HashMap<UUID, IslandTopGui>();
    private final Inventory myGui;
    private final int size = 9*6;//54
    private Player viewer;
    public static String guiName = LangManager.instants.getMessage("gui.island.title");
    public static IslandTopGui getGui(Player player)
    {
        if (activeGuis.containsKey(player.getUniqueId())) {
            return activeGuis.get(player.getUniqueId());
        }
        return null;
    }
    public IslandTopGui(Player player) {
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
        int fragmentlistTotal = 0;
        List<UUID> uuids = PlayerManager.getPlayers();
        List<NameAmount> nameAmounts = new ArrayList<NameAmount>();
        int total = uuids.size();
        for (UUID uuid: uuids)
        {
            int count = 0;
            Location home = PlayerManager.getHome(uuid);
            List<FragmentManager> fragments = Objects.requireNonNull(IslandManager.getIsland(home)).getFragments();
            for (FragmentManager manager: fragments)
            {
                StructureManager structure = manager.getStructure();
                int odds = 100;
                if (structure != null) odds = structure.getOdds();
                count = count + manager.getType().getValue() * (int)((100D- odds)/4D);
            }
            fragmentlistTotal = Math.max(fragmentlistTotal, count);
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
                button = TitanIslands.tools.getItemStackTool().addLore(button, LangManager.instants.getMessage("gui.island.line_a") + ChatColor.WHITE + TitanIslands.tools.getFormattingTool().formatCommas(amount.amount) + "/" + TitanIslands.tools.getFormattingTool().formatCommas(fragmentlistTotal), LangManager.instants.getMessage("gui.island.line_b"));
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
