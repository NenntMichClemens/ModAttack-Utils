package bl.clemensyo.modAttackUtils.listeners;

import bl.clemensyo.modAttackUtils.config;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class pvplistener implements Listener {
    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
            if (!config.pvp){
                event.setCancelled(true);
            }
        }
    }
}
