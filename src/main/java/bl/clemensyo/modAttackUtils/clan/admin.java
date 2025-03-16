package bl.clemensyo.modAttackUtils.clan;

import bl.clemensyo.modAttackUtils.config;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.RecursiveTask;

public class admin implements CommandExecutor {

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

        if (args.length < 1) {
            player.sendMessage("Verwendung: /admin <deleteclan/removeplayer>");
            return true;
        }
        if (args[0].equals("deleteclan")){
            if (args.length != 2){
                player.sendMessage(ChatColor.RED +"Falsche Verwendung: /admin deleteclan <Clanname>");
                return true;
            }
            String name = args[1];
            try {
                PreparedStatement statement = config.connection.prepareStatement("SELECT COUNT(*) AS count FROM clans WHERE name = ?");
                statement.setString(1, name);
                ResultSet rs = statement.executeQuery();
                if (rs.next()){
                    if (rs.getInt("count") == 0){
                        player.sendMessage(ChatColor.RED+"Clan nicht gefunden.");
                        return true;
                    }
                }
                PreparedStatement delstatement = config.connection.prepareStatement("DELETE FROM clans WHERE name = ?");
                delstatement.setString(1, name);
                delstatement.execute();

                PreparedStatement getPlayers = config.connection.prepareStatement("SELECT player FROM players WHERE clan = ?");
                getPlayers.setString(1, name);
                ResultSet players = getPlayers.executeQuery();
                while (players.next()){
                    String uuid = players.getString("player");
                    Player target = Bukkit.getPlayer(uuid);
                    if (target != null && target != player){
                        target.setDisplayName(target.getName());
                        target.setPlayerListName(target.getName());
                    }
                }
                PreparedStatement deleteplayers = config.connection.prepareStatement("DELETE FROM players WHERE clan = ?");
                deleteplayers.setString(1, name);
                deleteplayers.execute();
                player.sendMessage(ChatColor.GREEN+"Der Clan mit dem Namen "+ name +" wurde erfolgreich aus dem System gelöscht!");
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        return true;
    }
}