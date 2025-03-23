package bl.clemensyo.modAttackUtils.essentials;

import bl.clemensyo.modAttackUtils.ModAttackUtils;
import bl.clemensyo.modAttackUtils.config;
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
        if (config.isevent){
            player.sendMessage("Aktuell läuft ein Event. In dieser Zeit kannst du dich nicht zum Spawn teleportieren");
            return true;
        }
        long currentTime = System.currentTimeMillis();
        ModAttackUtils pg = ModAttackUtils.getInstance();
        if (pg.getCombatLog().containsKey(player.getUniqueId())) {
            long lastCombatTime = pg.getCombatLog().get(player.getUniqueId());
            if (currentTime - lastCombatTime < 10000) {
                player.sendMessage(net.md_5.bungee.api.ChatColor.RED +"Du bist gerade in einem Kampf und kannst dich daher nicht zum Spawn teleportieren.");
                return true;
            }
        }
        Location spawnLocation = new Location(Bukkit.getWorld("world"), -424, 124, 569);
        player.teleport(spawnLocation);
        player.sendMessage(ChatColor.GREEN+"Du wurdest zum Spawn teleportiert.");
        return true;


    }
}
