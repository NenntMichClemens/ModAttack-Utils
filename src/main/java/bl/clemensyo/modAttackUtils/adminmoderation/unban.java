package bl.clemensyo.modAttackUtils.adminmoderation;

import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class unban implements CommandExecutor {
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings){
        if (!(commandSender instanceof Player)){
            commandSender.sendMessage("This command can only be used by players");
            return true;
        }
        Player player = (Player) commandSender;

        BanList banlist = Bukkit.getBanList(BanList.Type.NAME);
        if (strings.length != 1){
            player.sendMessage("Wrong usage: /unban <Player>");
            return true;
        }
        String playername = strings[0];
        String reason = strings[1];
        Player target = Bukkit.getPlayer(playername);
        if (target == null){
            player.sendMessage("Player not found.");
            return true;
        }
        banlist.pardon(target);
        commandSender.sendMessage(target.getName() + " unbanned");
        return true;
    }
}
