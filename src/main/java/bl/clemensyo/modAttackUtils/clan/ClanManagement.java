package bl.clemensyo.modAttackUtils.clan;

import bl.clemensyo.modAttackUtils.ModAttackUtils;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import bl.clemensyo.modAttackUtils.helpers;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.StringUtil;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class ClanManagement implements CommandExecutor, TabCompleter {

    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            // Vorschläge für das erste Argument zurückgeben
            List<String> completions = new ArrayList<>();
            List<String> clanargs = Arrays.asList("create", "edit", "delete", "invite", "accept", "decline",
                    "kick", "leave", "setleader", "addmanager",
                    "removemanager", "info", "sethome", "home", "list");
            StringUtil.copyPartialMatches(args[0], clanargs, completions);
            Collections.sort(completions);
            return completions;
        }
        return null; // Keine Vorschläge für weitere Argumente
    }
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }
        List<String> clanargs = getArgs();
        if (args.length == 0) {
            player.sendMessage(ChatColor.RED + "Falsche Verwendung: /clan <create/edit/delete/invite/accept/decline/kick/leave/setleader/addmanager/info/list/sethome/home>");
            return true;
        }
        if (!clanargs.contains(args[0])) {
            player.sendMessage(ChatColor.RED + "Falsche Verwendung: /clan <create/edit/delete/invite/accept/decline/kick/leave/setleader/addmanager/info/list/sethome/home>");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "create":
                if (isinclan(player)){
                    player.sendMessage(ChatColor.RED + "Du bist aktuell in einem Clan! Verlasse diesen zunächst um einen neuen zu erstellen.");
                    return true;
                }
                if (args.length != 4) {
                    player.sendMessage(ChatColor.RED + "Falsche Verwendung: /clan create <Name> <Kürzel> <Farbe>");
                    return true;
                }

                String name = args[1];
                String key = args[2];
                String colour = args[3];

                if (!helpers.colorMap.containsKey(colour)) {
                    player.sendMessage(ChatColor.RED + "Falsche Farbangabe: Bitte verwende folgende Angaben: Schwarz|Dunkelblau|Dunkelgrün|Dunkelaqua|Dunkelrot|Dunkellila|Gold|Grau|Dunkelgrau|Blau|Grün|Aqua|Rot|Helllila|Gelb|Weiß");
                    return true;
                }
                try {
                    PreparedStatement statement = helpers.connection.prepareStatement("INSERT INTO clans (name, key, colour, leader) VALUES (?, ?, ?, ?)");
                    statement.setString(1, name);
                    statement.setString(2, key);
                    statement.setString(3, colour);
                    statement.setString(4, player.getUniqueId().toString());
                    statement.execute();

                    PreparedStatement ps = helpers.connection.prepareStatement("INSERT INTO players (player, clan, rank) VALUES (?, ?, ?)");
                    ps.setString(1, player.getUniqueId().toString());
                    ps.setString(2, name);
                    ps.setInt(3, 3);
                    ps.execute();
                } catch (SQLException e) {
                    player.sendMessage(ChatColor.RED + "Es gibt bereits einen Clan mit dem Namen \"" + name + "\"! Verwende einen anderen Namen.");
                    return true;
                }
                player.setDisplayName("[" + helpers.colorMap.get(colour) + key + "§r] " + player.getName());
                player.setPlayerListName("[" + helpers.colorMap.get(colour) + key + "§r] " + player.getName());
                player.sendMessage(ChatColor.GREEN + "Clan erstellt: Der Clan mit dem Namen " + name + " wurde erstellt! Lade Spieler mit /clan invite ein.");
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (isClanEmpty(name)){
                            try {
                                AdminClanManagment.deleteclan(player, name);
                            } catch (SQLException e) {
                                throw new RuntimeException(e);
                            }
                            if (player.isOnline()){
                                player.sendMessage(ChatColor.RED + "Dein Clan wurde gelöscht, da du nach einer Stunde noch keinen Spieler eingeladen hast.");
                            }
                        }
                    }
                }.runTaskLater(ModAttackUtils.getInstance(), 72000L); // 72000 Ticks = 1 Stunde
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
                    player.sendMessage(ChatColor.RED + "Falsche Verwendung: /clan edit <name/tag/colour> <WERT>");
                    return true;
                }

                ResultSet rs = getPlayerClan(player.getUniqueId());
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
                            PreparedStatement statement = helpers.connection.prepareStatement("UPDATE clans SET name = ? WHERE name = ?");
                            statement.setString(1, new_value);
                            statement.setString(2, player_clan);
                            statement.execute();
                            PreparedStatement stm = helpers.connection.prepareStatement("UPDATE players SET clan = ? WHERE clan = ?");
                            stm.setString(1, new_value);
                            stm.setString(2, player_clan);
                            statement.execute();
                            stm.execute();
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                        player.sendMessage(ChatColor.GREEN + "Der Name deines Clans wurde erfolgreich zu \"" + new_value + "\" geändert!");
                        break;
                    case "tag":
                        try {
                            PreparedStatement statement = helpers.connection.prepareStatement("UPDATE clans SET key = ? WHERE name = ?");
                            statement.setString(1, new_value);
                            statement.setString(2, player_clan);
                            statement.execute();

                            PreparedStatement update_players_stm = helpers.connection.prepareStatement("SELECT player FROM players WHERE clan = ?");
                            update_players_stm.setString(1, player_clan);
                            ResultSet resultSet = update_players_stm.executeQuery();
                            while (resultSet.next()){
                                String uuid = resultSet.getString("player");
                                Player target = Bukkit.getPlayer(uuid);
                                if (target != null && target != player){
                                    target.setDisplayName("[" + helpers.colorMap.get(clan_colour) + new_value + "§r] " + target.getName());
                                    target.setPlayerListName("[" + helpers.colorMap.get(clan_colour) + new_value + "§r] " + target.getName());
                                }
                                player.setDisplayName("[" + helpers.colorMap.get(clan_colour) + new_value + "§r] " + player.getName());
                                player.setPlayerListName("[" + helpers.colorMap.get(clan_colour) + new_value + "§r] " + player.getName());
                            }
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }

                        player.sendMessage(ChatColor.GREEN + "Der Kürzel deines Clans wurde erfolgreich zu \"" + new_value + "\" geändert!");
                        break;
                    case "colour":
                        if (!helpers.colorMap.containsKey(new_value)) {
                            player.sendMessage(ChatColor.RED + "Falsche Farbangabe: Bitte verwende folgende Angaben: Schwarz|Dunkelblau|Dunkelgrün|Dunkelaqua|Dunkelrot|Dunkellila|Gold|Grau|Dunkelgrau|Blau|Grün|Aqua|Rot|Helllila|Gelb|Weiß");
                            return true;
                        }
                        try {
                            PreparedStatement statement = helpers.connection.prepareStatement("UPDATE clans SET colour = ? WHERE name = ?");
                            statement.setString(1, new_value);
                            statement.setString(2, player_clan);
                            statement.execute();

                            PreparedStatement update_players = helpers.connection.prepareStatement("SELECT player FROM players WHERE clan = ?");
                            update_players.setString(1, player_clan);
                            ResultSet resultSet = update_players.executeQuery();
                            while (rs.next()){
                                String uuid = resultSet.getString("player");
                                Player target = Bukkit.getPlayer(uuid);
                                if (target != null && target != player){
                                    target.setDisplayName("[" + helpers.colorMap.get(new_value) + clan_key + "§r] " + target.getName());
                                    target.setPlayerListName("[" + helpers.colorMap.get(new_value) + clan_key + "§r] " + target.getName());
                                }
                                player.setDisplayName("[" + helpers.colorMap.get(new_value) + clan_key + "§r] " + player.getName());
                                player.setPlayerListName("[" + helpers.colorMap.get(new_value) + clan_key + "§r] " + player.getName());
                            }
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }

                        player.sendMessage(ChatColor.GREEN + "Die Farbe deines Clans wurde erfolgreich zu \""+ChatColor.RESET+ helpers.colorMap.get(new_value)+ new_value +ChatColor.GREEN+"\" geändert!");
                        break;
                    default:
                        player.sendMessage(ChatColor.RED + "Falsche Verwendung: /clan edit <name/tag/colour>");
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
                ResultSet resultSet = getPlayerClan(player.getUniqueId());
                try {
                    String clan_name = resultSet.getString("name");
                    PreparedStatement statement = helpers.connection.prepareStatement("DELETE FROM clans WHERE name = ?");
                    statement.setString(1, clan_name);
                    statement.execute();

                    PreparedStatement getPlayers = helpers.connection.prepareStatement("SELECT player FROM players WHERE clan = ?");
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
                    PreparedStatement deleteplayers = helpers.connection.prepareStatement("DELETE FROM players WHERE clan = ?");
                    deleteplayers.setString(1, clan_name);
                    deleteplayers.execute();

                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                player.sendMessage(ChatColor.GREEN +"Clan erfolgreich gelöscht.");
                break;
            case "invite":
                if (!isinclan(player)){
                    player.sendMessage(ChatColor.RED + "Du bist aktuell in keinem Clan!");
                    return true;
                }
                if (!ismanager(player.getUniqueId())) {
                    player.sendMessage(ChatColor.RED+"Du musst der "+ChatColor.BOLD + "Leader oder ein Manager" + ChatColor.RESET+ ChatColor.RED+" des Clans sein um diesen Befehl zu verwenden!");
                    return true;
                }
                if (args.length != 2){
                    player.sendMessage(ChatColor.RED + "Falsche Verwendung: /clan invite <Spieler>");
                    return true;
                }
                String playername = args[1];
                Player target = Bukkit.getPlayer(playername);
                if (target == null){
                    player.sendMessage(ChatColor.RED +"Spieler nicht gefunden.");
                    return true;
                }
                if (isinclan(target)){
                    player.sendMessage(ChatColor.RED + "Der Spieler ist bereits in diesem oder einem anderen Clan!");
                    return true;
                }
                ResultSet claninfo = getPlayerClan(player.getUniqueId());
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
                message.addExtra(new TextComponent(net.md_5.bungee.api.ChatColor.RESET + " " + net.md_5.bungee.api.ChatColor.BOLD + "lädt dich in seinen Clan "+ helpers.colorMap.get(clancolour) + clanname + "["+ clankey + "] " + ChatColor.RESET + ChatColor.BOLD + "ein."));
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
                player.sendMessage(ChatColor.GREEN+"Deine Clan-Einladung an " + target.getName() +" wurde versendet. Der Spieler hat 120 Sekunden diese zu bearbeiten.");
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (ModAttackUtils.getInstance().getClanrequests().containsKey(target.getUniqueId())) {
                            ModAttackUtils.getInstance().getClanrequests().remove(target.getUniqueId());
                            target.sendMessage(ChatColor.RED + "Die Clan-Einladung von " + player.getName() + " ist abgelaufen");
                            player.sendMessage(ChatColor.RED + "Deine Clan-Einladung an " + target.getName() + " ist abgelaufen.");
                        }
                    }
                }.runTaskLater(ModAttackUtils.getInstance(), 2400L); // 2400 Ticks = 120 Sekunden

                break;
            case "accept":
                if (args.length != 2) {
                    player.sendMessage("Falsche Verwendung: /clan accept <Spieler>");
                    return true;
                }

                Player requester = Bukkit.getPlayer(args[1]);
                if (requester == null) {
                    player.sendMessage(ChatColor.RED +"Der Spieler, der dir eine Clan-Einladung gesendet haben soll, konnte nicht gefunden werden.");
                    return true;
                }

                ModAttackUtils plugin = ModAttackUtils.getInstance();
                if (plugin == null) {
                    player.sendMessage("Plugin-Instanz ist null. Bitte melde dies dem Administrator.");
                    return true;
                }

                UUID targetUUID = player.getUniqueId();
                UUID requesterUUID = requester.getUniqueId();
                ResultSet ci = getPlayerClan(requester.getUniqueId());
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
                        PreparedStatement statement = helpers.connection.prepareStatement("INSERT INTO players (player, clan, rank) VALUES (?, ?, ?)");
                        statement.setString(1, player.getUniqueId().toString());
                        statement.setString(2, cname);
                        statement.setInt(3, 1);
                        statement.execute();
                        player.setDisplayName("[" + helpers.colorMap.get(ccoulor) + ckey + "§r] " + player.getName());
                        player.setPlayerListName("[" + helpers.colorMap.get(ccoulor) + ckey + "§r] " + player.getName());
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                    player.sendMessage(ChatColor.GREEN+"Du bist nun im " + cname +" Clan!");
                    requester.sendMessage(ChatColor.GREEN+ player.getName() +" hat deine Einladung akzeptiert und ist nun Teil des Clans!");
                    plugin.getClanrequests().remove(targetUUID);
                } else {
                    player.sendMessage(ChatColor.RED + "Es gibt aktuell keine Clan-Einladungen von diesem Spieler.");
                    return true;
                }
                break;
            case "decline":
                if (args.length != 2) {
                    player.sendMessage("Falsche Verwendung: /clan decline <Spieler>");
                    return true;
                }

                Player rq = Bukkit.getPlayer(args[1]);
                if (rq == null) {
                    player.sendMessage(ChatColor.RED +"Der Spieler, der dir eine Clan-Einladung gesendet haben soll, konnte nicht gefunden werden.");
                    return true;
                }

                ModAttackUtils pg = ModAttackUtils.getInstance();
                if (pg == null) {
                    player.sendMessage("Plugin-Instanz ist null. Bitte melde dies dem Administrator.");
                    return true;
                }

                UUID targetUUID1 = player.getUniqueId();
                UUID requesterUUID1 = rq.getUniqueId();
                if (pg.getClanrequests().containsKey(targetUUID1) && pg.getClanrequests().get(targetUUID1).equals(requesterUUID1)){
                    rq.sendMessage(ChatColor.RED + "Deine Clan-Einladung an " + player.getName() + " wurde abgelehnt.");
                    player.sendMessage(ChatColor.RED + "Du hast die Clan-Einladung abgelehnt.");
                    pg.getTpaRequests().remove(targetUUID1);
                }else {
                    player.sendMessage(ChatColor.RED + "Es gibt aktuell keine Clan-Einladungen von diesem Spieler.");
                    return true;
                }
                break;
            case "kick":
                if (!ismanager(player.getUniqueId())) {
                    player.sendMessage(ChatColor.RED + "Du musst der " + ChatColor.BOLD + "Leader oder ein Manager" + ChatColor.RESET + ChatColor.RED + " des Clans sein um diesen Befehl zu verwenden!");
                    return true;
                }
                if (args.length != 2) {
                    player.sendMessage(ChatColor.RED + "Falsche Verwendung: /clan kick <Spieler>");
                    return true;
                }

                String pn = args[1];
                Player kickuser = Bukkit.getPlayer(pn);
                OfflinePlayer offlineKickUser = null;

                // Falls offline, versuche OfflinePlayer zu bekommen
                if (kickuser == null) {
                    offlineKickUser = Bukkit.getOfflinePlayer(pn);
                    if (offlineKickUser == null || !offlineKickUser.hasPlayedBefore()) {
                        player.sendMessage(ChatColor.RED + "Spieler nicht gefunden.");
                        return true;
                    }
                }

                ResultSet clannamesource = getPlayerClan(player.getUniqueId());
                String cn = null;
                try {
                    cn = clannamesource.getString("name");
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }

                UUID kickUserUUID = (kickuser != null) ? kickuser.getUniqueId() : offlineKickUser.getUniqueId();

                if (ismanager(kickUserUUID)) { // Methode muss UUID statt Player akzeptieren
                    player.sendMessage(ChatColor.RED + "Du kannst keinen Manager oder den Leader des Clans kicken!");
                    return true;
                }
                if (!isinspecclan(kickUserUUID, cn)) { // Methode muss ebenfalls UUID akzeptieren
                    player.sendMessage(ChatColor.RED + "Der Spieler ist nicht in deinem Clan!");
                    return true;
                }

                try {
                    PreparedStatement stm = helpers.connection.prepareStatement("DELETE FROM players WHERE player = ?");
                    stm.setString(1, kickUserUUID.toString());
                    stm.execute();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }

                player.sendMessage(ChatColor.GREEN + ((kickuser != null) ? kickuser.getName() : offlineKickUser.getName()) + " wurde erfolgreich aus dem Clan gekickt.");

                if (kickuser != null) {
                    kickuser.setDisplayName(kickuser.getName());
                    kickuser.setPlayerListName(kickuser.getName());
                    kickuser.sendMessage(ChatColor.RED + "Du wurdest aus deinem Clan " + cn + " von einem Manager gekickt.");
                }
                break;

            case "leave":
                if(isLeader(player)){
                    player.sendMessage(ChatColor.RED +"Da du der Leader des Clans bist, kannst du diesen aktuell nicht verlassen.");
                    return true;
                }
                if (!isinclan(player)){
                    player.sendMessage(ChatColor.RED+"Du bist aktuell in keinem Clan.");
                    return true;
                }
                try {
                    PreparedStatement stm = helpers.connection.prepareStatement("DELETE FROM players WHERE player = ?");
                    stm.setString(1, player.getUniqueId().toString());
                    stm.execute();
                    player.setDisplayName(player.getName());
                    player.setPlayerListName(player.getName());
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                player.sendMessage(ChatColor.GREEN+"Du hast den Clan erfolgreich verlassen.");
                break;
            case "setleader":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Falsche Verwendung: /clan setleader <Spieler>");
                    return true;
                }
                if (args[1].equals("confirm")) {
                    String confirmname = args[2];
                    Player confirmplayer = Bukkit.getPlayerExact(confirmname);
                    if (confirmplayer == null){
                        player.sendMessage("Spieler nicht gefunden");
                        return true;
                    }
                    ModAttackUtils instance = ModAttackUtils.getInstance();
                    if (instance == null) {
                        player.sendMessage("Plugin-Instanz ist null. Bitte melde dies dem Administrator.");
                        return true;
                    }

                    UUID targetleaderUUID = confirmplayer.getUniqueId();
                    UUID leaderRequesterUUID = player.getUniqueId();
                    if (instance.getClanLeaderRequest().containsKey(targetleaderUUID) && instance.getClanLeaderRequest().get(targetleaderUUID).equals(leaderRequesterUUID)){
                        try {
                            PreparedStatement statement = helpers.connection.prepareStatement("UPDATE clans SET leader = ? WHERE name = ?");
                            statement.setString(1, targetleaderUUID.toString());
                            statement.setString(2, getPlayerClanName(player));
                            statement.execute();
                            PreparedStatement stm = helpers.connection.prepareStatement("UPDATE players SET rank = ? WHERE player = ?");
                            stm.setInt(1, 2);
                            stm.setString(2, player.getUniqueId().toString());
                            stm.execute();
                            PreparedStatement stmt = helpers.connection.prepareStatement("UPDATE players SET rank = ? WHERE player = ?");
                            stmt.setInt(1, 3);
                            stmt.setString(2, targetleaderUUID.toString());
                            stmt.execute();

                            player.sendMessage(ChatColor.GREEN+confirmplayer.getName() +" ist nun der Leader des Clans. Du bist nun ein Manager.");
                            confirmplayer.sendMessage(ChatColor.GREEN +"Du bist nun der Leader des Clans! Beachte: " + player.getName() + " ist nun ein Manager. Um dies zu ändern verwende /removemanager " + player.getName() +"!");
                            instance.getClanLeaderRequest().remove(targetleaderUUID);
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    return true;
                }
                if (!isinclan(player)){
                    player.sendMessage(ChatColor.RED+"Du bist aktuell in keinem Clan");
                    return true;
                }

                if (!isLeader(player)){
                    player.sendMessage(ChatColor.RED+"Du musst der Leader des Clans sein um diesen Befehl auszuführen.");
                    return true;
                }
                if (args.length != 2){
                    player.sendMessage(ChatColor.RED+"Falsche Verwendung: /clan setleader <Spieler>");
                    return true;
                }
                String nameofplayer = args[1];
                Player targetleader = Bukkit.getPlayerExact(nameofplayer);
                if (targetleader == null){
                    player.sendMessage("Spieler nicht gefunden.");
                    return true;
                }
                if (isLeader(targetleader)){
                    player.sendMessage(ChatColor.RED+"Du bist bereits der Leader des Clans");
                    return true;
                }
                if(!isinspecclan(targetleader.getUniqueId(), getPlayerClanName(player))){
                    player.sendMessage(ChatColor.RED+"Der Spieler ist nicht in deinem Clan.");
                    return true;
                }
                player.sendMessage(ChatColor.RED + ""+ ChatColor.BOLD +"Bist du dir sicher? Möchtest du wirklich den Clan-Leader Stand an " + targetleader.getName() +" abgeben?" + ChatColor.RESET + ChatColor.RED + "Du wirst nach dieser Aktion ein Manager des Clans. Die Aktion kann nicht von dir selber Rückgängig gemacht werden." + ChatColor.RESET + ChatColor.BOLD +"\n\n Verwende /clan setleader confirm <Spieler> um die Aktion durchzuführen. Dafür hast du 60 Sekunden Zeit.");
                ModAttackUtils.getInstance().getClanLeaderRequest().put(targetleader.getUniqueId(), player.getUniqueId());
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (ModAttackUtils.getInstance().getClanLeaderRequest().containsKey(targetleader.getUniqueId())) {
                            ModAttackUtils.getInstance().getClanLeaderRequest().remove(targetleader.getUniqueId());
                            player.sendMessage(ChatColor.RED + "Deine Anfrage zur Übernahme des Clans an " + targetleader.getName() + " ist abgelaufen.");
                        }
                    }
                }.runTaskLater(ModAttackUtils.getInstance(), 1200L); // 1200 Ticks = 60 Sekunden
                break;
            case "addmanager":
                if (!isinclan(player)) {
                    player.sendMessage(ChatColor.RED + "Du bist aktuell in keinem Clan.");
                    return true;
                }
                if (!isLeader(player)) {
                    player.sendMessage(ChatColor.RED + "Du musst der Leader des Clans sein um diesen Befehl zu verwenden");
                    return true;
                }
                if (args.length != 2) {
                    player.sendMessage(ChatColor.RED + "Falsche Verwendung: /clan addmanager <Spieler>");
                    return true;
                }

                String val = args[1];
                Player managerplayer = Bukkit.getPlayerExact(val);
                OfflinePlayer offlinePlayer = null;

                if (managerplayer == null) {
                    offlinePlayer = Bukkit.getOfflinePlayer(val);
                    if (offlinePlayer == null || !offlinePlayer.hasPlayedBefore()) {
                        player.sendMessage(ChatColor.RED + "Spieler nicht gefunden.");
                        return true;
                    }
                }

                UUID targetManagerUUID = (managerplayer != null) ? managerplayer.getUniqueId() : offlinePlayer.getUniqueId();

                if (ismanager(targetManagerUUID)) {
                    player.sendMessage(ChatColor.RED + "Der Spieler ist bereits ein Manager deines Clans.");
                    return true;
                }
                if (!isinspecclan(targetManagerUUID, getPlayerClanName(player))) {
                    player.sendMessage(ChatColor.RED + "Der Spieler ist aktuell nicht in deinem Clan.");
                    return true;
                }

                try {
                    PreparedStatement statement = helpers.connection.prepareStatement(
                            "UPDATE players SET rank = ? WHERE player = ?"
                    );
                    statement.setInt(1, 2);
                    statement.setString(2, targetManagerUUID.toString());
                    statement.execute();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }

                player.sendMessage(ChatColor.GREEN + ((managerplayer != null) ? managerplayer.getName() : offlinePlayer.getName()) + " ist nun ein Manager deines Clans.");
                if (managerplayer != null) {
                    managerplayer.sendMessage(ChatColor.GREEN + "Du bist nun Manager des Clans " + getPlayerClanName(player));
                }
                break;

            case "removemanager":
                if (!isinclan(player)) {
                    player.sendMessage(ChatColor.RED + "Du bist aktuell in keinem Clan.");
                    return true;
                }
                if (!isLeader(player)) {
                    player.sendMessage(ChatColor.RED + "Du musst der Leader des Clans sein, um diesen Befehl zu verwenden.");
                    return true;
                }
                if (args.length != 2) {
                    player.sendMessage(ChatColor.RED + "Falsche Verwendung: /clan removemanager <Spieler>");
                    return true;
                }

                String val1 = args[1];
                Player rmanagerplayer = Bukkit.getPlayerExact(val1);
                OfflinePlayer offlineRManager = null;

                if (rmanagerplayer == null) {
                    offlineRManager = Bukkit.getOfflinePlayer(val1);
                    if (offlineRManager == null || !offlineRManager.hasPlayedBefore()) {
                        player.sendMessage(ChatColor.RED + "Spieler nicht gefunden.");
                        return true;
                    }
                }

                UUID targetUUID2 = (rmanagerplayer != null) ? rmanagerplayer.getUniqueId() : offlineRManager.getUniqueId();

                if (!ismanager(targetUUID2)) {
                    player.sendMessage(ChatColor.RED + "Der Spieler ist kein Manager deines Clans.");
                    return true;
                }
                if (!isinspecclan(targetUUID2, getPlayerClanName(player))) {
                    player.sendMessage(ChatColor.RED + "Der Spieler ist aktuell nicht in deinem Clan.");
                    return true;
                }

                try {
                    PreparedStatement statement = helpers.connection.prepareStatement(
                            "UPDATE players SET rank = ? WHERE player = ?"
                    );
                    statement.setInt(1, 1);
                    statement.setString(2, targetUUID2.toString());
                    statement.execute();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }

                player.sendMessage(ChatColor.GREEN + ((rmanagerplayer != null) ? rmanagerplayer.getName() : offlineRManager.getName()) + " ist nun kein Manager mehr.");
                if (rmanagerplayer != null) {
                    rmanagerplayer.sendMessage(ChatColor.GREEN + "Du bist nun kein Manager des Clans " + getPlayerClanName(player) + " mehr.");
                }
                break;

            case "sethome":
                if (!isinclan(player)){
                    player.sendMessage(ChatColor.RED +"Du bist aktuell in keinem Clan.");
                    return true;
                }
                if (!isLeader(player)){
                    player.sendMessage(ChatColor.RED+"Du musst der Leader des Clans sein um diesen Befehl zu verwenden");
                    return true;
                }
                try {
                  PreparedStatement statement = helpers.connection.prepareStatement("UPDATE clans SET homex = ?, homey = ?, homez = ? WHERE name = ?");
                  statement.setDouble(1, player.getLocation().getX());
                  statement.setDouble(2, player.getLocation().getY());
                  statement.setDouble(3, player.getLocation().getZ());
                  statement.setString(4, getPlayerClanName(player));
                  statement.execute();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                player.sendMessage(ChatColor.GREEN +"Das Clan-Home ist nun auf " + Math.round(player.getLocation().getX()) + ", " + Math.round(player.getLocation().getY()) +", " + Math.round(player.getLocation().getZ()) +" gesetzt. Clan-Mitglieder können sich mit /clan home dorthin teleportieren!");
                break;
            case "home":
                if (!isinclan(player)){
                    player.sendMessage(ChatColor.RED +"Du bist aktuell in keinem Clan.");
                    return true;
                }
                long currentTime = System.currentTimeMillis();
                ModAttackUtils pwg = ModAttackUtils.getInstance();
                if (pwg.getCombatLog().containsKey(player.getUniqueId())) {
                    long lastCombatTime = pwg.getCombatLog().get(player.getUniqueId());
                    if (currentTime - lastCombatTime < 10000) {
                        player.sendMessage(net.md_5.bungee.api.ChatColor.RED +"Du bist gerade im Kampf! Du kannst dich gerade nicht in dein Clan-Home teleportieren.!");
                        return true;
                    }
                }
                try {
                    PreparedStatement statement = helpers.connection.prepareStatement("SELECT homex, homey, homez FROM clans WHERE name = ?");
                    statement.setString(1, getPlayerClanName(player));
                    ResultSet home = statement.executeQuery();
                    while (home.next()){
                        double x = home.getDouble("homex");
                        double y = home.getDouble("homey");
                        double z = home.getDouble("homez");
                        if (x == 0){
                            player.sendMessage(ChatColor.RED +"Für diese        n Clan wurde noch kein Home festgelegt.");
                            return true;
                        }
                        Location location = new Location(Bukkit.getServer().getWorld("world"), x, y, z);
                        player.teleport(location);
                    }
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                break;
            case "info":
                String clan = "";
                if (args.length == 1 && !isinclan(player)) {
                    player.sendMessage(ChatColor.RED + "Du musst in einem Clan sein oder den Namen eines Clans angeben, um Infos über diesen zu erhalten (/clan info <clanname>).");
                    return true;
                }
                if (args.length == 2) {
                    clan = args[1];
                } else if (args.length == 1) {
                    clan = getPlayerClanName(player);
                }
                ResultSet resultSet1 = getClanInfoByName(clan);
                try {
                    if (!resultSet1.next()) {
                        player.sendMessage("Clan nicht gefunden.");
                        return true;
                    }
                    String claninfoname = resultSet1.getString("name");
                    String claninfotag = resultSet1.getString("key");
                    String claninfocolour = resultSet1.getString("colour");
                    String claninfoleaderuuid = resultSet1.getString("leader");

                    // Leader abfragen (online oder offline)
                    System.out.println("Leader UUID: " + claninfoleaderuuid);
                    Player claninfoleader = Bukkit.getPlayer(UUID.fromString(claninfoleaderuuid));
                    OfflinePlayer offlineleader = null;
                    if (claninfoleader == null) {
                        System.out.println(claninfoleader + " (1)");
                        offlineleader = Bukkit.getOfflinePlayer(UUID.fromString(claninfoleaderuuid));
                        System.out.println(offlineleader);
                    }
                    String leaderName = "Unbekannt";
                    if (claninfoleaderuuid != null && !claninfoleaderuuid.isEmpty()) {
                        UUID leaderUUID = UUID.fromString(claninfoleaderuuid);
                        OfflinePlayer offlineLeader = Bukkit.getOfflinePlayer(leaderUUID);
                        leaderName = offlineLeader.getName() != null ? offlineLeader.getName() : leaderUUID.toString();
                    }


                    PreparedStatement statement = helpers.connection.prepareStatement("SELECT COUNT(*) AS count FROM players WHERE clan = ?");
                    statement.setString(1, clan);
                    ResultSet resultSet2 = statement.executeQuery();
                    int playercount = 0;
                    while (resultSet2.next()) {
                        playercount = resultSet2.getInt("count");
                    }

                    PreparedStatement managerStatement = helpers.connection.prepareStatement("SELECT player FROM players WHERE clan = ? AND rank = 2");
                    managerStatement.setString(1, claninfoname);
                    ResultSet managerResultSet = managerStatement.executeQuery();
                    List<String> managers = new ArrayList<>();
                    while (managerResultSet.next()) {
                        UUID managerUUID = UUID.fromString(managerResultSet.getString("player"));
                        Player manager = Bukkit.getPlayer(managerUUID);
                        if (manager != null) {
                            managers.add(manager.getName()); // Spieler ist online
                        } else {
                            OfflinePlayer offlineManager = Bukkit.getOfflinePlayer(managerUUID);
                            if (offlineManager.getName() != null) {
                                managers.add(offlineManager.getName()); // Spieler ist offline
                            }
                        }
                    }

                    String managerList = "Keine";
                    if (!managers.isEmpty()) {
                        managerList = String.join(", ", managers);
                    }

                    player.sendMessage(ChatColor.BOLD + "" + ChatColor.UNDERLINE + "Informationen zum Clan: " + claninfoname + "\n\n" +
                            ChatColor.RESET + ChatColor.BOLD +
                            "Name: " + ChatColor.RESET + claninfoname + "\n" +
                            ChatColor.RESET + ChatColor.BOLD +
                            "Kürzel: " + ChatColor.RESET + "[" + helpers.colorMap.get(claninfocolour) + claninfotag + "§r]\n" +
                            ChatColor.RESET + ChatColor.BOLD +
                            "Clan-Leader: " + ChatColor.RESET + leaderName + "\n" +
                            ChatColor.RESET + ChatColor.BOLD +
                            "Clan-Mitglieder: " + ChatColor.RESET + playercount + "\n" +
                            ChatColor.RESET + ChatColor.BOLD +
                            "Manager: " + ChatColor.RESET + managerList);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                break;
            case "list":
                if (args.length == 2) {
                    // Wenn ein Clanname angegeben ist
                    String clanName = args[1];
                    try {
                        PreparedStatement statement = helpers.connection.prepareStatement("SELECT player FROM players WHERE clan = ?");
                        statement.setString(1, clanName);
                        ResultSet rs2 = statement.executeQuery();
                        List<String> members = new ArrayList<>();
                        while (rs2.next()) {
                            String playerUUID = rs2.getString("player");
                            OfflinePlayer offlineMembers = Bukkit.getOfflinePlayer(UUID.fromString(playerUUID));
                            if (offlineMembers.hasPlayedBefore() || offlineMembers.isOnline()) {
                                members.add(offlineMembers.getName());
                            }
                        }
                        if (members.isEmpty()) {
                            player.sendMessage(ChatColor.RED + "Keine Mitglieder im Clan " + clanName + " gefunden oder Clan existiert nicht.");
                        } else {
                            String memberList = String.join(", ", members);
                            player.sendMessage(ChatColor.BOLD + "Mitglieder des Clans " + clanName + ": " + ChatColor.RESET + memberList);
                        }
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                } else if (args.length == 1) {
                    // Wenn kein Clanname angegeben ist
                    try {
                        PreparedStatement statement = helpers.connection.prepareStatement("SELECT name, key, colour FROM clans");
                        ResultSet resultSet3 = statement.executeQuery();
                        List<String> clans = new ArrayList<>();
                        while (resultSet3.next()) {
                            String name1 = resultSet3.getString("name");
                            String key1 = resultSet3.getString("key");
                            String colour1 = resultSet3.getString("colour");
                            clans.add("["+ helpers.colorMap.get(colour1) + key1 + ChatColor.RESET + "] - " + name1);
                        }
                        if (clans.isEmpty()) {
                            player.sendMessage(ChatColor.RED + "Es gibt keine existierenden Clans.");
                        } else {
                            String clanList = String.join("\n", clans);
                            player.sendMessage(ChatColor.BOLD + "Alle Clans: " + ChatColor.RESET + "\n" + clanList);
                        }
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    player.sendMessage(ChatColor.RED + "Falsche Verwendung: /clan list <clanname> (Ohne Clanname Liste aller Clans, mit Clanname Liste aller Clanmitglieder eines Clans)");
                }
                break;
            default:
                player.sendMessage(ChatColor.RED + "Falsche Verwendung: /clan <create/edit/delete/invite/accept/decline/kick/leave/setleader/addmanager/info/list/sethome/home>");
                break;
        }
        return true;
    }

    private static List<String> getArgs() {
        List<String> clanargs = new ArrayList<>();
        clanargs.add("create"); //d
        clanargs.add("edit"); //d
        clanargs.add("delete"); //d
        clanargs.add("invite"); //d
        clanargs.add("accept"); //d
        clanargs.add("decline"); //d
        clanargs.add("kick"); //d
        clanargs.add("leave"); //d
        clanargs.add("setleader"); //d
        clanargs.add("addmanager"); //d
        clanargs.add("removemanager"); //d
        clanargs.add("info");
        clanargs.add("sethome"); //d
        clanargs.add("home"); //d
        clanargs.add("list");
        return clanargs;
    }

    public boolean isLeader(Player player) {
        try {
            // Vorbereitung der SQL-Abfrage
            PreparedStatement statement = helpers.connection.prepareStatement("SELECT leader FROM clans WHERE leader = ?");
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
    public boolean ismanager(UUID playerUUID) {
        try {
            PreparedStatement statement = helpers.connection.prepareStatement(
                    "SELECT rank FROM players WHERE player = ?"
            );
            statement.setString(1, playerUUID.toString());

            ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                int rank = rs.getInt("rank");
                return rank == 2 || rank == 3;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return false;
    }
    public boolean isinspecclan(UUID playerUUID, String clan) {
        try {
            PreparedStatement statement = helpers.connection.prepareStatement(
                    "SELECT player FROM players WHERE player = ? AND clan = ?"
            );
            statement.setString(1, playerUUID.toString());
            statement.setString(2, clan);

            ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                return true;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return false;
    }
    public boolean isinclan(Player player){
        try {
            // Vorbereitung der SQL-Abfrage
            PreparedStatement statement = helpers.connection.prepareStatement("SELECT player FROM players WHERE player = ?");
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
    public ResultSet getPlayerClan(UUID playerUUID) {
        String player_clan = "";
        try {
            PreparedStatement getclan = helpers.connection.prepareStatement(
                    "SELECT name FROM clans WHERE leader = ?"
            );
            getclan.setString(1, playerUUID.toString());

            ResultSet rs = getclan.executeQuery();
            if (rs.next()) {
                player_clan = rs.getString("name");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        try {
            PreparedStatement claninfo = helpers.connection.prepareStatement(
                    "SELECT name, key, colour, leader FROM clans WHERE name = ?"
            );
            claninfo.setString(1, player_clan);
            return claninfo.executeQuery();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public ResultSet getClanInfoByName(String clanName) {
        try {
            PreparedStatement claninfo = helpers.connection.prepareStatement(
                    "SELECT name, key, colour, leader FROM clans WHERE name = ?"
            );
            claninfo.setString(1, clanName);
            return claninfo.executeQuery();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    public String getPlayerClanName(Player player) {
        String player_clan = "";
        try {
            PreparedStatement getclan = helpers.connection.prepareStatement("SELECT clan FROM players WHERE player = ?");
            getclan.setString(1, player.getUniqueId().toString());

            ResultSet rs = getclan.executeQuery();
            while (rs.next()) {
                player_clan = rs.getString("clan");
            }
            return player_clan;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    public boolean isClanEmpty(String clan) {
        // true, wenn <= 1 Spieler im Clan ist; false, wenn mehr als 1 Spieler im Clan ist
        try {
            PreparedStatement statement = helpers.connection.prepareStatement("SELECT COUNT(*) AS count FROM players WHERE clan = ?");
            statement.setString(1, clan);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                int count = rs.getInt("count");
                // Wenn die Anzahl der Spieler kleiner oder gleich 1 ist, gilt der Clan als leer
                return count <= 1;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return false; // Standardfall, falls kein ResultSet zurückkommt
    }


}
