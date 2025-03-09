package bl.clemensyo.modAttackUtils;

import bl.clemensyo.modAttackUtils.adminmoderation.ban;
import bl.clemensyo.modAttackUtils.adminmoderation.unban;
import bl.clemensyo.modAttackUtils.clan.clan;
import bl.clemensyo.modAttackUtils.essentials.tpa;
import bl.clemensyo.modAttackUtils.essentials.tpaaccept;
import bl.clemensyo.modAttackUtils.essentials.tpadeclince;
import bl.clemensyo.modAttackUtils.essentials.tpahere;
import bl.clemensyo.modAttackUtils.events.noelytra;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.UUID;

public final class ModAttackUtils extends JavaPlugin implements Listener {

    JavaPlugin plugin;

    private static ModAttackUtils instance;
    private final HashMap<UUID, UUID> tpaRequests = new HashMap<>();
    private final HashMap<UUID, UUID> tpahereRequests = new HashMap<>();

    @Override
    public void onEnable() {
        instance = this;
        getLogger().info("online");
        PluginManager manager = Bukkit.getPluginManager();
        manager.registerEvents(this, this);
        manager.registerEvents(new noelytra(), this);
        getCommand("ban").setExecutor(new ban());
        getCommand("unban").setExecutor(new unban());
        getCommand("tpa").setExecutor(new tpa());
        getCommand("tpaaccept").setExecutor(new tpaaccept());
        getCommand("tpadecline").setExecutor(new tpadeclince());
        getCommand("tpahere").setExecutor(new tpahere());
        getCommand("clan").setExecutor(new clan());

        Connection conn =null;
        try {
            conn = DriverManager.getConnection("jdbc:sqlite:modattackutils.db");
            System.out.println("Created database-connection");

            conn.createStatement().execute("CREATE TABLE IF NOT EXISTS clans (" +
                    "name STRING PRIMARY KEY," +
                    "key STRING, " +
                    "colour STRING," +
                    "leader STRING, " +
                    "max_players INTEGER" +
                    ")");
            conn.createStatement().execute("CREATE TABLE IF NOT EXISTS players (" +
                    "player STRING PRIMARY KEY," +
                    "clan STRING," +
                    "rank STRING" +
                    ")");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
    public static ModAttackUtils getInstance() {
        return instance;
    }

    public HashMap<UUID, UUID> getTpaRequests() {
        return tpaRequests;
    }
    public HashMap<UUID, UUID> getTpahereRequests() {
        return tpahereRequests;
    }
}
