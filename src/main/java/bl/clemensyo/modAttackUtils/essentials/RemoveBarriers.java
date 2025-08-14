package bl.clemensyo.modAttackUtils.essentials;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


public class RemoveBarriers implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Dieser Befehl kann nur von einem Spieler ausgeführt werden.");
            return true;
        }

        if (!player.isOp()) {
            player.sendMessage("Du musst OP dafür sein.");
            return true;
        }

        if (args.length != 1) {
            player.sendMessage("Verwendung: /removebarriers <radius>");
            return true;
        }

        int radius;
        try {
            radius = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            player.sendMessage("Der Radius muss eine Zahl sein.");
            return true;
        }

        Location center = player.getLocation();
        int removedBlocks = 0;

        for (int x = center.getBlockX() - radius; x <= center.getBlockX() + radius; x++) {
            for (int y = center.getBlockY() - radius; y <= center.getBlockY() + radius; y++) {
                for (int z = center.getBlockZ() - radius; z <= center.getBlockZ() + radius; z++) {
                    Location loc = new Location(center.getWorld(), x, y, z);
                    if (loc.getBlock().getType() == Material.BARRIER) {
                        loc.getBlock().setType(Material.AIR);
                        removedBlocks++;
                    }
                }
            }
        }

        player.sendMessage("Es wurden " + removedBlocks + " Barrier-Blöcke entfernt.");
        return true;
    }
}
