package bl.clemensyo.modAttackUtils.essentials;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class spawn implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Dieser Befehl kann nur von einem Spieler ausgeführt werden.");
            return true;
        }
        Player player = (Player) sender;
        Location spawnLocation = new Location(Bukkit.getWorld("world"), -424, 124, 569);
        player.teleport(spawnLocation);
        player.sendMessage(ChatColor.GREEN+"Du wurdest zum Spawn teleportiert.");
        return true;


    }
}
