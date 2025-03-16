package bl.clemensyo.modAttackUtils.listeners;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class nonetherite implements Listener {
    private boolean isBlockedItem(Material material) {
        // Liste der blockierten Gegenstände
        return material == Material.ANCIENT_DEBRIS ||
                material == Material.NETHERITE_SCRAP ||
                material == Material.NETHERITE_INGOT ||
                material == Material.NETHERITE_UPGRADE_SMITHING_TEMPLATE;
    }

    @EventHandler
    public void onEntityPickUpItem(EntityPickupItemEvent event) {
        if (event.getEntity() instanceof Player) {
            if (isBlockedItem(event.getItem().getItemStack().getType())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        ItemStack currentItem = event.getCurrentItem();
        if (currentItem != null && isBlockedItem(currentItem.getType())) {
            event.setCancelled(true);
        }
    }
}
