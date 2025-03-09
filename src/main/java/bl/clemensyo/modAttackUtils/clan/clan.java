package bl.clemensyo.modAttackUtils.clan;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import bl.clemensyo.modAttackUtils.config;

import javax.naming.NamingEnumeration;
import javax.print.CancelablePrintJob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class clan implements CommandExecutor {

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }
        Player player = (Player) sender;
        List<String> clanargs = new ArrayList<>();
        clanargs.add("create");
        clanargs.add("edit");
        clanargs.add("delete");
        clanargs.add("invite");
        clanargs.add("accept");
        clanargs.add("decline");
        clanargs.add("kick");
        clanargs.add("leave");
        clanargs.add("setleader");
        clanargs.add("addmanager");
        clanargs.add("info");
        if (args.length == 0){
            player.sendMessage(ChatColor.RED+"Falsche Verwendung: /clan create|edit|delete|info|invite|accept|decline|kick|leave|setleader|addmanager");
            return true;
        }
        if (!clanargs.contains(args[0])) {
            player.sendMessage(ChatColor.RED+"Falsche Verwendung: /clan create|edit|delete|info|invite|accept|decline|kick|leave|setleader|addmanager");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "create":
                if (args.length != 4 && args.length != 5) {
                    player.sendMessage(ChatColor.RED + "Falsche Verwendung: /clan create <Name> <Kürzel> <Farbe> <Max. Players>");
                    return true;
                }
                try {
                    PreparedStatement statement = config.connection.prepareStatement("SELECT COUNT(*) AS count FROM players WHERE player = ?");
                    statement.setString(1, player.getUniqueId().toString());
                    ResultSet rs = statement.executeQuery();
                    while (rs.next()) {
                        int count = rs.getInt("count");
                        if (count == 1) {
                            player.sendMessage(ChatColor.RED + "Du bist bereits in einem Clan und kannst daher keinen neuen erstellen. Verlasse den Clan zuerst um einen neuen zu erstellen!");
                            return true;
                        }
                    }
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }

                String name = args[1];
                String key = args[2];
                String colour = args[3];
                int max_players;

                if (args.length == 5) {
                    try {
                        max_players = Integer.parseInt(args[4]);
                    } catch (NumberFormatException e) {
                        player.sendMessage(ChatColor.RED + "Falsche Angabe: max_players muss eine Zahl sein!");
                        return true;
                    }
                } else {
                    max_players = 0; // Setze 0, wenn max_players nicht angegeben ist
                }
                if (!config.colorMap.containsKey(colour)) {
                    player.sendMessage(ChatColor.RED + "Falsche Farbangabe: Bitte verwende folgende Angaben: Schwarz|Dunkelblau|Dunkelgrün|Dunkelaqua|Dunkelrot|Dunkellila|Gold|Grau|Dunkelgrau|Blau|Grün|Aqua|Rot|Helllila|Gelb|Weiß");
                    return true;
                }
                try {
                    PreparedStatement statement = config.connection.prepareStatement("INSERT INTO clans (name, key, colour, leader, max_players) VALUES (?, ?, ?, ?, ?)");
                    statement.setString(1, name);
                    statement.setString(2, key);
                    statement.setString(3, colour);
                    statement.setString(4, player.getUniqueId().toString());
                    statement.setInt(5, max_players);
                    statement.execute();

                    PreparedStatement ps = config.connection.prepareStatement("INSERT INTO players (player, clan, rank) VALUES (?, ?, ?)");
                    ps.setString(1, player.getUniqueId().toString());
                    ps.setString(2, name);
                    ps.setString(3, "LEADER");
                    ps.execute();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
//                        player.sendMessage(ChatColor.RED + "Es gibt bereits einen Clan mit dem Namen \"" + name + "\"! Verwende einen anderen Namen.");
//                        return true;
                }
                player.setDisplayName("["+config.colorMap.get(colour) + key + "§r] " + player.getName());
                player.setPlayerListName("["+config.colorMap.get(colour) + key + "§r] " + player.getName());

                    break;
                }
                return true;
        }
}
