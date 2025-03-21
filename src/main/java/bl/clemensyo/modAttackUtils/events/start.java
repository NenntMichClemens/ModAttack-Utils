package bl.clemensyo.modAttackUtils.events;

import bl.clemensyo.modAttackUtils.config;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.net.http.WebSocket;

import static bl.clemensyo.modAttackUtils.config.isevent;

public class start implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Dieser Befehl kann nur von einem Spieler ausgeführt werden.");
            return true;
        }
        Player player = (Player) sender;
        if (!player.isOp()) {
            player.sendMessage("Du musst OP dafür sein.");
            return true;
        }
        isevent = true;
        player.sendMessage("Event gestartet.");
    return true;}
}
