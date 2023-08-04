package com.firesoftitan.play.titanbox.islands.listeners;

import com.firesoftitan.play.titanbox.islands.TitanIslands;
import com.firesoftitan.play.titanbox.islands.enums.ProtectionEnum;
import com.firesoftitan.play.titanbox.islands.managers.ConfigManager;
import com.firesoftitan.play.titanbox.islands.managers.FragmentManager;
import com.firesoftitan.play.titanbox.islands.managers.IslandManager;
import com.firesoftitan.play.titanbox.islands.managers.PlayerManager;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;

import java.util.Objects;
import java.util.UUID;

import static com.firesoftitan.play.titanbox.islands.TitanIslands.configManager;
import static com.firesoftitan.play.titanbox.islands.TitanIslands.instance;

public class ProtectionListener  implements Listener {

    public ProtectionListener() {

    }

    public void registerEvents(){
        PluginManager pm = instance.getServer().getPluginManager();
        pm.registerEvents(this, instance);
    }
    @EventHandler(priority = EventPriority.LOWEST)  
    public void onBlockBreakEvent(BlockBreakEvent event) {
        //System.out.println("BREAK: " + canAccess(event.getPlayer(), event.getBlock(), ProtectionEnum.BREAK));
        if (!canAccess(event.getPlayer(), event.getBlock(),ProtectionEnum.BREAK))
        {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)  
    public void onBlockPlaceEvent(BlockPlaceEvent event) {
        //System.out.println("BUILD: " + canAccess(event.getPlayer(), event.getBlock(), ProtectionEnum.BUILD));
        if (!canAccess(event.getPlayer(), event.getBlock(), ProtectionEnum.BUILD))
        {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)  
    public void onPlayerInteractEvent(PlayerInteractEvent event) {
        Block clickedBlock = event.getClickedBlock();
        ItemStack itemStack = event.getItem();
        if (clickedBlock == null) return;
        //System.out.println("Interact: " + canAccess(event.getPlayer(), clickedBlock, ProtectionEnum.USE));
        if (!canAccess(event.getPlayer(), clickedBlock, ProtectionEnum.USE))
        {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)  
    public void onBlockIgniteEvent(BlockIgniteEvent event) {
        if (event.getIgnitingBlock() == null) return;
        if (event.getPlayer() == null) return;
        //System.out.println("IGNITE: " + canAccess(event.getPlayer(), event.getIgnitingBlock(), ProtectionEnum.IGNITE));
        if (!canAccess(event.getPlayer(), event.getIgnitingBlock(), ProtectionEnum.IGNITE))
        {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)  
    public void onBlockFromToEvent(BlockFromToEvent event) {
        if (!sameOwner(event.getToBlock(), event.getBlock()))
        {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)  
    public void onSpongeAbsorbEvent(SpongeAbsorbEvent event) {
        for(BlockState exBlock: event.getBlocks())
        {
            if (!sameOwner(exBlock.getBlock(), event.getBlock()))
            {
                event.setCancelled(true);
                return;
            }
        }
    }


    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockPistonExtendEvent(BlockPistonExtendEvent event) {
        for(Block exBlock: event.getBlocks())
        {
            if (!sameOwner(exBlock, event.getBlock()))
            {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)  
    public void onBlockPistonRetractEvent(BlockPistonRetractEvent event) {
        for(Block exBlock: event.getBlocks())
        {
            if (!sameOwner(exBlock, event.getBlock()))
            {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)  
    public void onBlockExplodeEvent(BlockExplodeEvent event) {

        for(Block exBlock: event.blockList())
        {
            if (!sameOwner(exBlock, event.getBlock()))
            {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)  
    public void onEntityExplodeEvent(EntityExplodeEvent event) {
        if (event.getEntity().getType() == EntityType.CREEPER || event.getEntity().getType() == EntityType.WITHER_SKULL || event.getEntity().getType() == EntityType.FIREBALL || event.getEntity().getType() == EntityType.LIGHTNING) {
            FragmentManager fragmentA = FragmentManager.getFragment(event.getLocation());
            if (fragmentA == null && !configManager.isProtection_wild_creepers()) {
                event.setCancelled(true);
                return;
            }
            if (fragmentA == null && configManager.isProtection_wild_creepers())
            {
                return;
            }
            UUID owner = PlayerManager.getOwner(fragmentA);
            if (owner == null && !configManager.isProtection_not_owned_creepers()) {
                event.setCancelled(true);
            } else {
                if (configManager.isProtection_owned_creepers()) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)  
    public void onEntityChangeBlockEvent(EntityChangeBlockEvent event) {
        if (event.getEntity() != null && event.getEntity().getType() == EntityType.PLAYER)
        {
            if (!canAccess((Player) event.getEntity(), event.getBlock(), ProtectionEnum.USE))
            {
                event.setCancelled(true);
            }
        }
        else {
            if (isProtected(event.getBlock())) {
                event.setCancelled(true);
            }
        }
    }

    
    private boolean sameOwner(Block blockA, Block blockB)
    {
        return sameOwner(blockA.getLocation(), blockB.getLocation());
    }
    private boolean sameOwner(Location locationA, Location locationB)
    {
        if (!Objects.requireNonNull(locationA.getWorld()).getName().equals(ConfigManager.getInstants().getWorld().getName())) return true;
        if (!Objects.requireNonNull(locationB.getWorld()).getName().equals(ConfigManager.getInstants().getWorld().getName())) return true;
        FragmentManager fragmentA = FragmentManager.getFragment(locationA);
        FragmentManager fragmentB = FragmentManager.getFragment(locationB);
        if (fragmentA == null && fragmentB != null) return false;
        if (fragmentA != null && fragmentB == null) return false;
        if (fragmentA == null && fragmentB == null) return configManager.isProtection_wild_break();
        UUID ownerA = PlayerManager.getOwner(fragmentA);
        UUID ownerB = PlayerManager.getOwner(fragmentB);
        if (ownerA == null && ownerB == null) return true;
        if (ownerA == null || ownerB == null) return false;
        return ownerA.equals(ownerB);
    }
    private boolean isProtected(Location location)
    {
        if (!Objects.requireNonNull(location.getWorld()).getName().equals(ConfigManager.getInstants().getWorld().getName())) return false;
        FragmentManager fragment = FragmentManager.getFragment(location);
        if (fragment == null) return configManager.isProtection_wild_break(); //stop player from building in the wild
        UUID owner = PlayerManager.getOwner(fragment);
        if (owner == null) return configManager.isProtection_not_owned_break();
        return true;
    }
    private boolean isProtected(Block block)
    {
        return isProtected(block.getLocation());
    }
    private boolean canAccess(Player player, Block block, ProtectionEnum action) {
        return canAccess(player, block.getLocation(), action);
    }
    private boolean canAccess(Player player, Location location, ProtectionEnum action) {
        if (!Objects.requireNonNull(location.getWorld()).getName().equals(ConfigManager.getInstants().getWorld().getName())) return true;
        if (TitanIslands.getAdminMode(player)) return true;

        FragmentManager fragment = FragmentManager.getFragment(location);

        if (fragment == null) {
            if (action == ProtectionEnum.BREAK) {
                return configManager.isProtection_wild_break();
            } else if (action == ProtectionEnum.USE) {
                return configManager.isProtection_wild_use();
            } else if (action == ProtectionEnum.BUILD) {
                return configManager.isProtection_wild_build();
            } else if (action == ProtectionEnum.IGNITE) {
                return configManager.isProtection_wild_ignite();
            }
        }

        UUID ownedByPlayer = PlayerManager.getOwner(fragment);

        if (ownedByPlayer == null) {
            if (action == ProtectionEnum.BREAK) {
                return configManager.isProtection_not_owned_break();
            } else if (action == ProtectionEnum.USE) {
                return configManager.isProtection_not_owned_use();
            } else if (action == ProtectionEnum.BUILD) {
                return configManager.isProtection_not_owned_build();
            } else if (action == ProtectionEnum.IGNITE) {
                return configManager.isProtection_not_owned_ignite();
            }
        }

        boolean equals = player.getUniqueId().equals(ownedByPlayer);
        if (!equals)
        {
            if (fragment != null) {
                IslandManager island = fragment.getIsland();
                if (island != null) return island.isFriend(player);
            }
        }

        return equals;
    }
}
