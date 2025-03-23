package bl.clemensyo.modAttackUtils.essentials;

import bl.clemensyo.modAttackUtils.ModAttackUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class tpaaccept implements CommandExecutor {
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Nur Spieler können diesen Befehl benutzen.");
            return true;
        }

        Player targetPlayer = (Player) sender;
        if (args.length < 1) {
            targetPlayer.sendMessage("Falsche Verwendung: /tpaaccept <Spieler>");
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
        long currentTime = System.currentTimeMillis();
        ModAttackUtils pg = ModAttackUtils.getInstance();
        if (pg.getCombatLog().containsKey(requesterUUID)) {
            long lastCombatTime = pg.getCombatLog().get(requesterUUID);
            if (currentTime - lastCombatTime < 10000) {
                targetPlayer.sendMessage(net.md_5.bungee.api.ChatColor.RED +"Dieser Spieler ist gerade im Kampf! Er kann gerade nicht zu dir teleportiert werden!");
                return true;
            }
        }
        // Überprüfen, ob es eine TPA- oder TPHere-Anfrage gibt
        if (plugin.getTpaRequests().containsKey(targetUUID) && plugin.getTpaRequests().get(targetUUID).equals(requesterUUID)) {
            // TPA-Anfrage
            requester.teleport(targetPlayer.getLocation());
            requester.sendMessage(ChatColor.GREEN + "Du wurdest zu " + targetPlayer.getName() + " teleportiert.");
            targetPlayer.sendMessage(ChatColor.GREEN + "Anfrage akzeptiert!");
            plugin.getTpaRequests().remove(targetUUID);

        } else if (plugin.getTpahereRequests().containsKey(targetUUID) && plugin.getTpahereRequests().get(targetUUID).equals(requesterUUID)) {
            // TPHere-Anfrage
            targetPlayer.teleport(requester.getLocation());
            requester.sendMessage(ChatColor.GREEN + targetPlayer.getName() + " wurde zu dir teleportiert.");
            targetPlayer.sendMessage(ChatColor.GREEN + "Du wurdest zu " + requester.getName() + " teleportiert.");
            plugin.getTpahereRequests().remove(targetUUID);

        } else {
            targetPlayer.sendMessage("Es gibt keine aktuelle TPA- oder TPHere-Anfrage von diesem Spieler.");
        }

        return true;
    }
}
