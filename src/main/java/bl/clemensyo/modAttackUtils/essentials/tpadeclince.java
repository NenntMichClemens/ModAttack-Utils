package bl.clemensyo.modAttackUtils.essentials;

import bl.clemensyo.modAttackUtils.ModAttackUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class tpadeclince implements CommandExecutor {
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Nur Spieler können diesen Befehl benutzen.");
            return true;
        }

        Player targetPlayer = (Player) sender;
        if (args.length < 1) {
            targetPlayer.sendMessage("Falsche Verwendung: /tpadecline <Spieler>");
            return true;
        }

        Player requester = Bukkit.getPlayer(args[0]);
        if (requester == null) {
            targetPlayer.sendMessage("Spieler nicht gefunden.");
            return true;
        }

        ModAttackUtils plugin = ModAttackUtils.getInstance();
        if (plugin == null) {
            targetPlayer.sendMessage("Plugin-Instanz ist null. Bitte melde dies dem Administrator.");
            return true;
        }

        UUID targetUUID = targetPlayer.getUniqueId();
        UUID requesterUUID = requester.getUniqueId();

        // Überprüfen, ob es eine TPA- oder TPHere-Anfrage gibt
        if (plugin.getTpaRequests().containsKey(targetUUID) && plugin.getTpaRequests().get(targetUUID).equals(requesterUUID)) {
            // TPA-Anfrage ablehnen
            requester.sendMessage(ChatColor.RED + "Deine TPA an " + targetPlayer.getName() + " wurde abgelehnt.");
            targetPlayer.sendMessage(ChatColor.RED + "Du hast die TPA-Anfrage abgelehnt.");
            plugin.getTpaRequests().remove(targetUUID);

        } else if (plugin.getTpahereRequests().containsKey(targetUUID) && plugin.getTpahereRequests().get(targetUUID).equals(requesterUUID)) {
            // TPHere-Anfrage ablehnen
            requester.sendMessage(ChatColor.RED + "Deine TPHere-Anfrage an " + targetPlayer.getName() + " wurde abgelehnt.");
            targetPlayer.sendMessage(ChatColor.RED + "Du hast die TPHere-Anfrage abgelehnt.");
            plugin.getTpahereRequests().remove(targetUUID);

        } else {
            targetPlayer.sendMessage("Es gibt keine aktuelle TPA- oder TPHere-Anfrage von diesem Spieler.");
        }

        return true;
    }
}
