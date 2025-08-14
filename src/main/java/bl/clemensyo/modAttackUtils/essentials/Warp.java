package bl.clemensyo.modAttackUtils.essentials;

import bl.clemensyo.modAttackUtils.ModAttackUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Warp implements CommandExecutor, TabCompleter {

        public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
            if (args.length == 1) {
                // Vorschläge für das erste Argument zurückgeben
                List<String> completions = new ArrayList<>();
                List<String> warpargs = Arrays.asList("end");
                StringUtil.copyPartialMatches(args[0], warpargs, completions);
                Collections.sort(completions);
                return completions;
            }
            return null; // Keine Vorschläge für weitere Argumente
        }

        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("Dieser Befehl kann nur von einem Spieler ausgeführt werden.");
                return true;
            }
            Player player = (Player) sender;
            long currentTime = System.currentTimeMillis();
            ModAttackUtils pg = ModAttackUtils.getInstance();
            if (pg.getCombatLog().containsKey(player.getUniqueId())) {
                long lastCombatTime = pg.getCombatLog().get(player.getUniqueId());
                if (currentTime - lastCombatTime < 10000) {
                    player.sendMessage(net.md_5.bungee.api.ChatColor.RED +"Du bist gerade im Kampf! Du kannst dich gerade nicht teleportieren!");
                    return true;
                }
            }
            if (args.length != 1){
                player.sendMessage(ChatColor.RED +"Falsche Verwendung: /warp <warp>");
                return true;
            }
            //Note: All warps are hard-coded - there is no /warp add command or something like this
            if (args[0].equals("end")){
                Location location = new Location(Bukkit.getWorld("world"), 914, -4, 1276);
                player.teleport(location);
                player.sendMessage(ChatColor.GREEN+"Du wurdest zum Warp Stronghold teleportiert!");
                return true;
            }
            else {
                player.sendMessage(ChatColor.RED+"Warp existiert nicht: Der Warp " + args[0] +" existiert nicht!");
            }
        return true;
        }
}
