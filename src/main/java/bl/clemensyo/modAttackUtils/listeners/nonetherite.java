package bl.clemensyo.modAttackUtils.listeners;

import bl.clemensyo.modAttackUtils.config;
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
        return material == Material.NETHERITE_BOOTS ||
                material == Material.NETHERITE_CHESTPLATE ||
                material == Material.NETHERITE_HELMET ||
                material == Material.NETHERITE_LEGGINGS ||
                material == Material.NETHERITE_SWORD ||
                material == Material.NETHERITE_AXE;
    }

    @EventHandler
    public void onEntityPickUpItem(EntityPickupItemEvent event) {
        if (event.getEntity() instanceof Player) {
            if (config.isevent){
                return;
            }
            if (isBlockedItem(event.getItem().getItemStack().getType())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (config.isevent){
            return;
        }
        ItemStack currentItem = event.getCurrentItem();
        if (currentItem != null && isBlockedItem(currentItem.getType())) {
            event.setCancelled(true);
        }
    }
}
