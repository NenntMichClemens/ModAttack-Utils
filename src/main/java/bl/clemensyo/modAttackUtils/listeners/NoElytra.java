package bl.clemensyo.modAttackUtils.listeners;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class NoElytra implements Listener {
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();

        // Überprüfen, ob der Slot ein Rüstungsslot ist
        if (event.getSlotType() != InventoryType.SlotType.ARMOR) {
            return;
        }

        // Überprüfen, ob der Spieler eine Elytra anzieht
        ItemStack clickedItem = event.getCurrentItem();
        ItemStack cursorItem = event.getCursor();

        // Wenn die Elytra in den Slot geklickt wird
        if (clickedItem != null && clickedItem.getType() == Material.ELYTRA) {
            event.setCancelled(true);
            player.getInventory().addItem(clickedItem);
        }

        // Wenn der Spieler versucht, die Elytra aus dem Slot zu nehmen
        if (cursorItem != null && cursorItem.getType() == Material.ELYTRA) {
            event.setCancelled(true);
            player.getInventory().addItem(cursorItem);
            event.setCursor(null); // Entfernt die Elytra von der Maus des Spielers
        }
    }
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getHand() == EquipmentSlot.HAND && event.getPlayer().getInventory().getItemInMainHand().getType() == Material.ELYTRA) {
            event.setCancelled(true);
        }
    }
}
