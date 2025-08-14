package bl.clemensyo.modAttackUtils.listeners;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

public class HeadDrop implements Listener {
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event){
        ItemStack playerhead = new ItemStack(Material.PLAYER_HEAD, 1);
        SkullMeta skullMeta = (SkullMeta) playerhead.getItemMeta();
        skullMeta.setOwningPlayer(event.getEntity());
        playerhead.setItemMeta(skullMeta);
        event.getDrops().add(playerhead);
     }
}
