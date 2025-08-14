package bl.clemensyo.modAttackUtils.listeners;

import bl.clemensyo.modAttackUtils.ModAttackUtils;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.HashMap;
import java.util.UUID;

public class CombatLog implements Listener {
    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player attacker && event.getEntity() instanceof Player victim) {
            long currentTime = System.currentTimeMillis();
            ModAttackUtils plugin = ModAttackUtils.getInstance();
            HashMap<UUID, Long> combatLog = plugin.getCombatLog();
            combatLog.put(attacker.getUniqueId(), currentTime);
            combatLog.put(victim.getUniqueId(), currentTime);
        }
    }
}
