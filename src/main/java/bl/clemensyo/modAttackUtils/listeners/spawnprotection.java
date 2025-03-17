package bl.clemensyo.modAttackUtils.listeners;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageEvent;

public class spawnprotection implements Listener {

    private final Location center = new Location(Bukkit.getWorld("world"), -424, 124, 569);
    private final int radius = 20;
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event){
        Player player = event.getPlayer();
        Location loc = event.getBlock().getLocation();
        if (loc.getWorld().equals(center.getWorld()) && loc.distance(center) <= radius) {
            if (player.getGameMode() != GameMode.CREATIVE && !player.isOp()) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Location loc = event.getBlock().getLocation();
        if (loc.getWorld().equals(center.getWorld()) && loc.distance(center) <= radius) {
            if (player.getGameMode() != GameMode.CREATIVE && !player.isOp()) {
                event.setCancelled(true);
            }
        }
    }
    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        Location loc = event.getLocation();
        if (loc.getWorld().equals(center.getWorld()) && loc.distance(center) <= radius) {
            event.setCancelled(true);
        }
    }
    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            Location loc = player.getLocation();
            if (loc.getWorld().equals(center.getWorld()) && loc.distance(center) <= radius) {
                event.setCancelled(true);
            }
        }
    }
}
