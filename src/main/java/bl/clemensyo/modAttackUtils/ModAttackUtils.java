package bl.clemensyo.modAttackUtils;

import bl.clemensyo.modAttackUtils.clan.admin;
import bl.clemensyo.modAttackUtils.clan.clan;
import bl.clemensyo.modAttackUtils.essentials.*;
import bl.clemensyo.modAttackUtils.events.headdrop;
import bl.clemensyo.modAttackUtils.events.noelytra;
import bl.clemensyo.modAttackUtils.events.nonetherite;
import bl.clemensyo.modAttackUtils.events.spawnprotection;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.*;
import java.util.HashMap;
import java.util.UUID;

public final class ModAttackUtils extends JavaPlugin implements Listener {

    JavaPlugin plugin;

    private static ModAttackUtils instance;
    private final HashMap<UUID, UUID> tpaRequests = new HashMap<>();
    private final HashMap<UUID, UUID> tpahereRequests = new HashMap<>();
    private final HashMap<UUID, UUID> clanrequests = new HashMap<>();
    private final HashMap<UUID, UUID> setleaderreq = new HashMap<>();
    private final Location spawnLocation = new Location(Bukkit.getWorld("world"), -424, 124, 569); // Hier die Spawn-Koordinaten einfügen
    @Override
    public void onEnable() {
        instance = this;
        getLogger().info("Aktiviere ModAttack | Utils");
        PluginManager manager = Bukkit.getPluginManager();
        manager.registerEvents(this, this);
        manager.registerEvents(new noelytra(), this);
        manager.registerEvents(this, this);
        manager.registerEvents(new headdrop(), this);
        manager.registerEvents(new nonetherite(), this);
        manager.registerEvents(new spawnprotection(), this);
        getCommand("tpa").setExecutor(new tpa());
        getCommand("tpaaccept").setExecutor(new tpaaccept());
        getCommand("tpadecline").setExecutor(new tpadeclince());
        getCommand("tpahere").setExecutor(new tpahere());
        getCommand("clan").setExecutor(new clan());
        getCommand("removebarriers").setExecutor(new startstart());
        getCommand("admin").setExecutor(new admin());
        getCommand("spawn").setExecutor(new spawn());

        Connection conn =null;
        try {
            conn = DriverManager.getConnection("jdbc:sqlite:modattackutils.db");
            conn.createStatement().execute("CREATE TABLE IF NOT EXISTS clans (" +
                    "name STRING PRIMARY KEY," +
                    "key STRING," +
                    "colour STRING," +
                    "leader STRING, " +
                    "homex FLOAT," +
                    "homey FLOAT," +
                    "homez FLOAT" +
                    ")");
            conn.createStatement().execute("CREATE TABLE IF NOT EXISTS players (" +
                    "player STRING PRIMARY KEY," +
                    "clan STRING," +
                    "rank INTEGER" +
                    ")");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event){
        Player player = event.getPlayer();
        if (!player.hasPlayedBefore()){
            player.teleport(spawnLocation);
        }
        try {
            PreparedStatement statement = config.connection.prepareStatement("SELECT clan FROM players WHERE player = ?");
            statement.setString(1, player.getUniqueId().toString());
            ResultSet rs = statement.executeQuery();
            while (rs.next()){
                String name = rs.getString("clan");
                PreparedStatement stm = config.connection.prepareStatement("SELECT key, colour FROM clans WHERE name = ?");
                stm.setString(1, name);
                ResultSet resultSet = stm.executeQuery();
                while (resultSet.next()){
                    String key = resultSet.getString("key");
                    String colour = resultSet.getString("colour");
                    player.setDisplayName("[" + config.colorMap.get(colour) + key + "§r] " + player.getName());
                    player.setPlayerListName("[" + config.colorMap.get(colour) + key + "§r] " + player.getName());
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        if (!event.getRespawnLocation().equals(event.getPlayer().getBedSpawnLocation())) {
            event.setRespawnLocation(spawnLocation);
        }
    }
    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getLogger().info("Disabling ModAttack | Utils");
    }
    public static ModAttackUtils getInstance() {
        return instance;
    }
    public HashMap<UUID, UUID> getClanrequests(){
        return clanrequests;
    }

    public HashMap<UUID, UUID> getTpaRequests() {
        return tpaRequests;
    }
    public HashMap<UUID, UUID> getTpahereRequests() {
        return tpahereRequests;
    }
    public HashMap<UUID, UUID> getClanLeaderRequest(){
        return setleaderreq;
    }
}
