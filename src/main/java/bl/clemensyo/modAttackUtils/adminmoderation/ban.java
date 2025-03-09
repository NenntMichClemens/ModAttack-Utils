package bl.clemensyo.modAttackUtils.adminmoderation;

import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ban implements CommandExecutor {
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings){
        if (!(commandSender instanceof Player)){
            commandSender.sendMessage("This command can only be used by players");
            return true;
        }
        Player player = (Player) commandSender;
        if (strings.length < 2){
            player.sendMessage("Wrong usage: /ban <Player> <Reason>");
            return true;
        }

        String playername = strings[0];
        String reason = strings[1];
        Player target = Bukkit.getPlayer(playername);
        if (target == null){
            player.sendMessage("Player not found");
            return true;
        }
        Bukkit.getBanList(BanList.Type.NAME).addBan(playername, reason, null, commandSender.getName());
        target.kickPlayer(ChatColor.RED + "Du wurdest mit dem Grund " + reason + " von einem Operator gebannt! Wenn du denkst dies ist ein Fehler kontaktiere einen Operator per Discord.");
        player.sendMessage(target.getName() + " was banned with the reason "+ reason + ".");
        return true;
    }
}
