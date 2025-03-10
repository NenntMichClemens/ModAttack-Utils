package bl.clemensyo.modAttackUtils.clan;

import bl.clemensyo.modAttackUtils.ModAttackUtils;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import bl.clemensyo.modAttackUtils.config;
import org.bukkit.scheduler.BukkitRunnable;
import org.checkerframework.framework.qual.PreconditionAnnotation;

import javax.naming.NamingEnumeration;
import javax.print.CancelablePrintJob;
import javax.swing.table.TableRowSorter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

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
                if (isinclan(player)){
                    player.sendMessage(ChatColor.RED + "Du bist aktuell in einem Clan! Verlasse diesen zunächst um einen neuen zu erstellen.");
                    return true;
                }
                if (args.length != 4 && args.length != 5) {
                    player.sendMessage(ChatColor.RED + "Falsche Verwendung: /clan create <Name> <Kürzel> <Farbe> <Max. Players>");
                    return true;
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
                if (!isinclan(player)){
                    player.sendMessage(ChatColor.RED + "Du bist aktuell in keinem Clan!");
                    return true;
                }
                if (!isLeader(player)){
                    player.sendMessage(ChatColor.RED+"Du musst der "+ChatColor.BOLD + "Leader" + ChatColor.RESET+ ChatColor.RED+" des Clans sein um diesen Befehl zu verwenden!");
                    return true;
                }
                if (args.length != 3) {
                    player.sendMessage(ChatColor.RED + "Falsche Verwendung: /clan edit <name/key/colour> <WERT>");
                    return true;
                }

                ResultSet rs = getPlayerClan(player);
                String clan_key = null;
                String clan_colour = null;
                String player_clan = null;
                try {
                    player_clan = rs.getString("name");
                    clan_key = rs.getString("key");
                    clan_colour = rs.getString("colour");
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
                            ResultSet resultSet = change_name_stm.executeQuery();
                            while (resultSet.next()){
                                String uuid = resultSet.getString("player");
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
                            ResultSet resultSet = change_name_stm.executeQuery();
                            while (rs.next()){
                                String uuid = resultSet.getString("player");
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
                        break;
                    default:
                        player.sendMessage(ChatColor.RED + "Falsche Verwendung: /clan edit <name/key/colour>");
                        return true;
                }
                break;
            case "delete":
                if (!isinclan(player)){
                    player.sendMessage(ChatColor.RED + "Du bist aktuell in keinem Clan!");
                    return true;
                }
                if (!isLeader(player)){
                    player.sendMessage(ChatColor.RED+"Du musst der "+ChatColor.BOLD + "Leader" + ChatColor.RESET+ ChatColor.RED+" des Clans sein um diesen Befehl zu verwenden!");
                    return true;
                }
                ResultSet resultSet = getPlayerClan(player);
                try {
                    String clan_name = resultSet.getString("name");
                    PreparedStatement statement = config.connection.prepareStatement("DELETE FROM clans WHERE name = ?");
                    statement.setString(1, clan_name);
                    statement.execute();

                    PreparedStatement getPlayers = config.connection.prepareStatement("SELECT player FROM players WHERE clan = ?");
                    getPlayers.setString(1, clan_name);
                    ResultSet players = getPlayers.executeQuery();
                    while (players.next()){
                        String uuid = players.getString("player");
                        Player target = Bukkit.getPlayer(uuid);
                        if (target != null && target != player){
                            target.setDisplayName(target.getName());
                            target.setPlayerListName(target.getName());
                        }
                        player.setDisplayName(player.getName());
                        player.setPlayerListName(player.getName());
                    }
                    PreparedStatement deleteplayers = config.connection.prepareStatement("DELETE FROM players WHERE clan = ?");
                    deleteplayers.setString(1, clan_name);
                    deleteplayers.execute();

                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                break;
            case "invite":
                if (!isinclan(player)){
                    player.sendMessage(ChatColor.RED + "Du bist aktuell in keinem Clan!");
                    return true;
                }
                if ((!isLeader(player)) || (!ismanager(player))) {
                    player.sendMessage(ChatColor.RED+"Du musst der "+ChatColor.BOLD + "Leader oder ein Manager" + ChatColor.RESET+ ChatColor.RED+" des Clans sein um diesen Befehl zu verwenden!");
                    return true;
                }
                if (args.length != 3){
                    player.sendMessage(ChatColor.RED + "Falsche Verwendung: /clan invite <Spieler>");
                    return true;
                }
                String playername = args[2];
                Player target = Bukkit.getPlayer(playername);
                if (target == null){
                    player.sendMessage(ChatColor.RED +"Spieler nicht gefunden.");
                    return true;
                }
                if (isinclan(target)){
                    player.sendMessage(ChatColor.RED + "Der Spieler ist bereits in diesem oder einem anderen Clan!");
                    return true;
                }
                ResultSet claninfo = getPlayerClan(player);
                String clanname = null;
                String clankey = null;
                String clancolour = null;
                try {
                    while (claninfo.next()){
                        clanname = claninfo.getString("name");
                        clankey = claninfo.getString("key");
                        clancolour = claninfo.getString("colour");
                    }
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                TextComponent message = new TextComponent();
                message.addExtra(new TextComponent(net.md_5.bungee.api.ChatColor.BOLD + "" + net.md_5.bungee.api.ChatColor.UNDERLINE + "Spieler"));
                message.addExtra(new TextComponent(net.md_5.bungee.api.ChatColor.RESET + " " + net.md_5.bungee.api.ChatColor.BOLD + "lädt dich in seinen Clan "+ config.colorMap.get(clancolour) + clanname + "["+ clankey + "] " + ChatColor.RESET + ChatColor.BOLD + "ein."));
                message.addExtra(new TextComponent(net.md_5.bungee.api.ChatColor.BOLD + "" + net.md_5.bungee.api.ChatColor.UNDERLINE + "" + net.md_5.bungee.api.ChatColor.DARK_RED + "Du hast 120 Sekunden um diese Anfrage zu bearbeiten.\n"));

                TextComponent accept = new TextComponent(net.md_5.bungee.api.ChatColor.GREEN + "[Accept]");
                accept.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/clan accept " + player.getName()));

                TextComponent decline = new TextComponent(net.md_5.bungee.api.ChatColor.RED + "[Decline]");
                decline.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/clan decline" + player.getName()));
                message.addExtra(accept);
                message.addExtra(new TextComponent(" ")); // Leerzeichen zwischen den Buttons
                message.addExtra(decline);
                ModAttackUtils.getInstance().getClanrequests().put(target.getUniqueId(), player.getUniqueId());
                target.spigot().sendMessage(message);

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (ModAttackUtils.getInstance().getTpaRequests().containsKey(target.getUniqueId())) {
                            ModAttackUtils.getInstance().getTpaRequests().remove(target.getUniqueId());
                            target.sendMessage(org.bukkit.ChatColor.RED + "Die Clan-Einladung von " + player.getName() + " ist abgelaufen");
                            player.sendMessage(org.bukkit.ChatColor.RED + "Deine Clan-Einladung an " + target.getName() + " ist abgelaufen.");
                        }
                    }
                }.runTaskLater(ModAttackUtils.getInstance(), 2400L); // 2400 Ticks = 120 Sekunden

                break;
            case "accept":
                Player targetPlayer = (Player) sender;
                if (args.length != 2) {
                    targetPlayer.sendMessage("Falsche Verwendung: /clan accept <Spieler>");
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
                ResultSet ci = getPlayerClan(requester);
                String cname = null;
                String ckey = null;
                String ccoulor = null;
                try {
                    while (ci.next()){
                        cname = ci.getString("name");
                        ckey = ci.getString("key");
                        ccoulor = ci.getString("colour");
                    }
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }

                if (plugin.getClanrequests().containsKey(targetUUID) && plugin.getClanrequests().get(targetUUID).equals(requesterUUID)){
                    try {
                        PreparedStatement statement = config.connection.prepareStatement("INSERT INTO players (player, clan, rank) VALUES (?, ?, ?)");
                        statement.setString(1, player.getUniqueId().toString());
                        statement.setString(2, cname);
                        statement.setInt(3, 1);
                        statement.execute();
                        player.setDisplayName("[" + config.colorMap.get(ccoulor) + ckey + "§r] " + player.getName());
                        player.setPlayerListName("[" + config.colorMap.get(ccoulor) + ckey + "§r] " + player.getName());
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                    targetPlayer.sendMessage(ChatColor.GREEN+"Du bist nun im " + cname +" Clan!");
                    requester.sendMessage(ChatColor.GREEN+ targetPlayer.getName() +" hat deine Einladung akzeptiert und ist nun Teil des Clans!");
                    plugin.getClanrequests().remove(targetUUID);
                } else {
                    player.sendMessage(ChatColor.RED + "Es gibt aktuell keine Clan-Einladungen von diesem Spieler.");
                    return true;
                }
                break;
            case "decline":
                Player tp = (Player) sender;
                if (args.length != 2) {
                    tp.sendMessage("Falsche Verwendung: /clan accept <Spieler>");
                    return true;
                }

                Player rq = Bukkit.getPlayer(args[0]);
                if (rq == null) {
                    tp.sendMessage("Spieler nicht gefunden.");
                    return true;
                }

                ModAttackUtils pg = ModAttackUtils.getInstance();
                if (pg == null) {
                    tp.sendMessage("Plugin-Instanz ist null. Bitte melde dies dem Administrator.");
                    return true;
                }

                UUID targetUUID1 = tp.getUniqueId();
                UUID requesterUUID1 = rq.getUniqueId();
                if (pg.getClanrequests().containsKey(targetUUID1) && pg.getClanrequests().get(targetUUID1).equals(requesterUUID1)){
                    rq.sendMessage(ChatColor.RED + "Deine Clan-Einladung an " + tp.getName() + " wurde abgelehnt.");
                    tp.sendMessage(ChatColor.RED + "Du hast die Clan-Einladung abgelehnt.");
                    pg.getTpaRequests().remove(targetUUID1);
                }else {
                    player.sendMessage(ChatColor.RED + "Es gibt aktuell keine Clan-Einladungen von diesem Spieler.");
                    return true;
                }
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
    public boolean ismanager(Player player) {
        try {
            // Vorbereitung der SQL-Abfrage
            PreparedStatement statement = config.connection.prepareStatement("SELECT rank FROM players WHERE player = ?");
            statement.setString(1, player.getUniqueId().toString());

            // Ausführen der Abfrage
            ResultSet rs = statement.executeQuery();

            // Überprüfen, ob ein Ergebnis vorhanden ist
            if (rs.next()) {
                int rank = rs.getInt("rank");
                return rank == 2;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return false; // Der Spieler ist nicht der Leader eines Clans
    }
    public boolean isinclan(Player player){
        try {
            // Vorbereitung der SQL-Abfrage
            PreparedStatement statement = config.connection.prepareStatement("SELECT player FROM players WHERE player = ?");
            statement.setString(1, player.getUniqueId().toString());

            // Ausführen der Abfrage
            ResultSet rs = statement.executeQuery();

            // Überprüfen, ob ein Ergebnis vorhanden ist
            if (rs.next()) {
                return true; // Der Spieler ist in einem Clan
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return false; // Der Spieler ist in einem Clan
    }
    public ResultSet getPlayerClan(Player player){
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
        try {
            PreparedStatement claninfo = config.connection.prepareStatement("SELECT name, key, colour FROM clans WHERE name = ?");
            claninfo.setString(1, player_clan);
            return claninfo.executeQuery();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

}
