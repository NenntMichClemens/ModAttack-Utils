package bl.clemensyo.modAttackUtils.clan;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import bl.clemensyo.modAttackUtils.config;

import javax.naming.NamingEnumeration;
import javax.print.CancelablePrintJob;
import javax.swing.table.TableRowSorter;
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
        if (args.length == 0) {
            player.sendMessage(ChatColor.RED + "Falsche Verwendung: /clan create|edit|delete|info|invite|accept|decline|kick|leave|setleader|addmanager");
            return true;
        }
        if (!clanargs.contains(args[0])) {
            player.sendMessage(ChatColor.RED + "Falsche Verwendung: /clan create|edit|delete|info|\n" +
                    "invite|accept|decline|\n" +
                    "kick|leave|\n" +
                    "setleader|addmanager\n" +
                    "sethome|home");
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

                if (!config.colorMap.containsKey(colour)) {
                    player.sendMessage(ChatColor.RED + "Falsche Farbangabe: Bitte verwende folgende Angaben: Schwarz|Dunkelblau|Dunkelgrün|Dunkelaqua|Dunkelrot|Dunkellila|Gold|Grau|Dunkelgrau|Blau|Grün|Aqua|Rot|Helllila|Gelb|Weiß");
                    return true;
                }
                try {
                    PreparedStatement statement = config.connection.prepareStatement("INSERT INTO clans (name, key, colour, leader) VALUES (?, ?, ?, ?)");
                    statement.setString(1, name);
                    statement.setString(2, key);
                    statement.setString(3, colour);
                    statement.setString(4, player.getUniqueId().toString());
                    statement.execute();

                    PreparedStatement ps = config.connection.prepareStatement("INSERT INTO players (player, clan, rank) VALUES (?, ?, ?)");
                    ps.setString(1, player.getUniqueId().toString());
                    ps.setString(2, name);
                    ps.setInt(3, 3);
                    ps.execute();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
//                        player.sendMessage(ChatColor.RED + "Es gibt bereits einen Clan mit dem Namen \"" + name + "\"! Verwende einen anderen Namen.");
//                        return true;
                }
                player.setDisplayName("[" + config.colorMap.get(colour) + key + "§r] " + player.getName());
                player.setPlayerListName("[" + config.colorMap.get(colour) + key + "§r] " + player.getName());
                player.sendMessage(ChatColor.GREEN + "Clan erstellt: Der Clan mit dem Namen " + name + " wurde erstellt! Lade Spieler mit /clan invite ein.");
                break;

            case "edit":
                if (!isLeader(player)){
                    player.sendMessage(ChatColor.RED+"Du musst der "+ChatColor.BOLD + "Leader" + ChatColor.BOLD + " des Clans sein um diesen Befehl zu verwenden!");
                    return true;
                }
                if (args.length != 3) {
                    player.sendMessage(ChatColor.RED + "Falsche Verwendung: /clan edit <name/key/colour> <WERT>");
                    return true;
                }

                String player_clan = "";
                try {
                    PreparedStatement getclan = config.connection.prepareStatement("SELECT name FROM clans WHERE leader = ?");
                    getclan.setString(1, player.getUniqueId().toString());

                    ResultSet rs = getclan.executeQuery();
                    while (rs.next()){
                        player_clan = rs.getString("name");
                    }

                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                //Get clan info
                String clan_key = "";
                String clan_colour = "";
                try {
                    PreparedStatement claninfo = config.connection.prepareStatement("SELECT key, colour FROM clans WHERE name = ?");
                    claninfo.setString(1, player_clan);
                    ResultSet rs = claninfo.executeQuery();
                    while (rs.next()){
                        clan_key = rs.getString("key");
                        clan_colour = rs.getString("colour");
                    }
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                String new_value = args[2];
                switch (args[1].toLowerCase()){
                    case "name":
                        try {
                            PreparedStatement statement = config.connection.prepareStatement("UPDATE clans SET name = ? WHERE name = ?");
                            statement.setString(1, new_value);
                            statement.setString(2, player_clan);
                            statement.execute();
                            System.out.println(new_value);
                            System.out.println(player_clan);
                            PreparedStatement stm = config.connection.prepareStatement("UPDATE players SET clan = ? WHERE clan = ?");
                            stm.setString(1, new_value);
                            stm.setString(2, player_clan);
                            statement.execute();
                            stm.execute();
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                        player.sendMessage(ChatColor.GREEN + "Der Name deines Clans wurde erfolgreich zu \"" + new_value + "\" geändert!");
                        break;
                    case "key":
                        try {
                            PreparedStatement statement = config.connection.prepareStatement("UPDATE clans SET key = ? WHERE name = ?");
                            statement.setString(1, new_value);
                            statement.setString(2, player_clan);
                            statement.execute();

                            PreparedStatement change_name_stm = config.connection.prepareStatement("SELECT player FROM players WHERE clan = ?");
                            change_name_stm.setString(1, player_clan);
                            ResultSet rs = change_name_stm.executeQuery();
                            while (rs.next()){
                                String uuid = rs.getString("player");
                                Player target = Bukkit.getPlayer(uuid);
                                if (target != null && target != player){
                                    target.setDisplayName("[" + config.colorMap.get(clan_colour) + new_value + "§r] " + target.getName());
                                    target.setPlayerListName("[" + config.colorMap.get(clan_colour) + new_value + "§r] " + target.getName());
                                }
                                player.setDisplayName("[" + config.colorMap.get(clan_colour) + new_value + "§r] " + player.getName());
                                player.setPlayerListName("[" + config.colorMap.get(clan_colour) + new_value + "§r] " + player.getName());
                            }
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }

                        player.sendMessage(ChatColor.GREEN + "Der Kürzel deines Clans wurde erfolgreich zu \"" + new_value + "\" geändert!");
                        break;
                    case "colour":
                        if (!config.colorMap.containsKey(new_value)) {
                            player.sendMessage(ChatColor.RED + "Falsche Farbangabe: Bitte verwende folgende Angaben: Schwarz|Dunkelblau|Dunkelgrün|Dunkelaqua|Dunkelrot|Dunkellila|Gold|Grau|Dunkelgrau|Blau|Grün|Aqua|Rot|Helllila|Gelb|Weiß");
                            return true;
                        }
                        try {
                            PreparedStatement statement = config.connection.prepareStatement("UPDATE clans SET colour = ? WHERE name = ?");
                            statement.setString(1, new_value);
                            statement.setString(2, player_clan);
                            statement.execute();

                            PreparedStatement change_name_stm = config.connection.prepareStatement("SELECT player FROM players WHERE clan = ?");
                            change_name_stm.setString(1, player_clan);
                            ResultSet rs = change_name_stm.executeQuery();
                            while (rs.next()){
                                String uuid = rs.getString("player");
                                Player target = Bukkit.getPlayer(uuid);
                                if (target != null && target != player){
                                    target.setDisplayName("[" + config.colorMap.get(new_value) + clan_key + "§r] " + target.getName());
                                    target.setPlayerListName("[" + config.colorMap.get(new_value) + clan_key + "§r] " + target.getName());
                                }
                                player.setDisplayName("[" + config.colorMap.get(new_value) + clan_key + "§r] " + player.getName());
                                player.setPlayerListName("[" + config.colorMap.get(new_value) + clan_key + "§r] " + player.getName());
                            }
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }

                        player.sendMessage(ChatColor.GREEN + "Die Farbe deines Clans wurde erfolgreich zu \""+ChatColor.RESET+config.colorMap.get(new_value)+ new_value +ChatColor.GREEN+"\" geändert!");
                }
                break;

            default:
                player.sendMessage(ChatColor.RED + "Falsche Verwendung: /clan edit <name/key/colour>");
                return true;
        }
        return true;
    }

    public boolean isLeader(Player player) {
        try {
            // Vorbereitung der SQL-Abfrage
            PreparedStatement statement = config.connection.prepareStatement("SELECT leader FROM clans WHERE leader = ?");
            statement.setString(1, player.getUniqueId().toString());

            // Ausführen der Abfrage
            ResultSet rs = statement.executeQuery();

            // Überprüfen, ob ein Ergebnis vorhanden ist
            if (rs.next()) {
                return true; // Der Spieler ist der Leader eines Clans
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return false; // Der Spieler ist nicht der Leader eines Clans
    }

}
