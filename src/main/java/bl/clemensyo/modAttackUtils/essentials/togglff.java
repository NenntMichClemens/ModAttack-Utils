package bl.clemensyo.modAttackUtils.essentials;

import bl.clemensyo.modAttackUtils.config;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class togglff implements CommandExecutor {
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
        if (config.pvp){
            config.pvp = false;
            player.sendMessage(ChatColor.GREEN +"Spieler sind nun nicht mehr in der Lage sich gegenseitig anzugreifen!");
        }
        else {
            config.pvp = true;
            player.sendMessage(ChatColor.GREEN +"Spieler sind nun wieder in der Lage sich gegenseitig anzugreifen!");
        }
        return true;

    }
}
