package com.firesoftitan.play.titanbox.islands.listeners;

import com.firesoftitan.play.titanbox.islands.TitanIslands;
import com.firesoftitan.play.titanbox.islands.managers.CubeManager;
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
import org.bukkit.plugin.PluginManager;

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
        if (!canAccess(event.getPlayer(), event.getBlock()))
        {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)  
    public void onBlockPlaceEvent(BlockPlaceEvent event) {
        if (!canAccess(event.getPlayer(), event.getBlock()))
        {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)  
    public void onPlayerInteractEvent(PlayerInteractEvent event) {
        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null) return;
        if (clickedBlock.getType().toString().toLowerCase().contains("door")) return;
        if (!canAccess(event.getPlayer(), clickedBlock))
        {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)  
    public void onBlockIgniteEvent(BlockIgniteEvent event) {
        if (event.getIgnitingBlock() == null) return;
        if (!canAccess(event.getPlayer(), event.getIgnitingBlock()))
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
            CubeManager cubeA = CubeManager.getCube(event.getLocation());
            if (cubeA == null && !configManager.isProtection_creepers()) {
                event.setCancelled(true);
                return;
            }
            UUID owner = PlayerManager.instants.getOwner(cubeA);
            if (owner == null && !configManager.isProtection_creepers_notowned()) {
                event.setCancelled(true);
            } else {
                if (configManager.isProtection_creepers_owned()) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)  
    public void onEntityChangeBlockEvent(EntityChangeBlockEvent event) {
        if (isProtected(event.getBlock()))
        {
            event.setCancelled(true);
        }
    }

    
    private boolean sameOwner(Block blockA, Block blockB)
    {
        return sameOwner(blockA.getLocation(), blockB.getLocation());
    }
    private boolean sameOwner(Location locationA, Location locationB)
    {
        CubeManager cubeA = CubeManager.getCube(locationA);
        CubeManager cubeB = CubeManager.getCube(locationB);
        if (cubeA == null && cubeB != null) return false;
        if (cubeA != null && cubeB == null) return false;
        if (cubeA == null && cubeB == null) return configManager.isProtection_griefing();
        UUID ownerA = PlayerManager.instants.getOwner(cubeA);
        UUID ownerB = PlayerManager.instants.getOwner(cubeB);
        return ownerA.equals(ownerB);
    }
    private boolean isProtected(Location location)
    {
        CubeManager cube = CubeManager.getCube(location);
        if (cube == null) return configManager.isProtection_griefing(); //stop player from building in the wild
        UUID owner = PlayerManager.instants.getOwner(cube);
        if (owner == null) return configManager.isProtection_griefing_notowned();
        return true;
    }
    private boolean isProtected(Block block)
    {
        return isProtected(block.getLocation());
    }
    private boolean canAccess(Player player, Block block)
    {
        return canAccess(player, block.getLocation());
    }
    private boolean canAccess(Player player, Location location)
    {
        if (TitanIslands.getAdminMode(player)) return true;
        CubeManager cube = CubeManager.getCube(location);
        if (cube == null) return configManager.isProtection_griefing(); //stop player from building in the wild
        UUID ownedByPlayer = PlayerManager.instants.getOwner(cube);
        if (ownedByPlayer == null) return configManager.isProtection_griefing_notowned(); //
        return player.getUniqueId().equals(ownedByPlayer);
    }
    
}
