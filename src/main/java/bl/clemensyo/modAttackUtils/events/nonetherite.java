package bl.clemensyo.modAttackUtils.events;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

import static org.bukkit.Material.*;

public class nonetherite implements Listener {
    @EventHandler
    public void onEntityPickUpItem(EntityPickupItemEvent event){
        if (event.getEntity() instanceof Player){
            if (event.getItem().getItemStack().getType() == ANCIENT_DEBRIS){
                event.setCancelled(true);
            }
        }
    }
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event){
        ItemStack currentItem = event.getCurrentItem();
        if ((currentItem != null && currentItem.getType() == ANCIENT_DEBRIS) || (currentItem != null && currentItem.getType() == NETHERITE_SCRAP) || (currentItem != null && currentItem.getType() == NETHERITE_INGOT)){
            event.setCancelled(true);
        }
    }
}
