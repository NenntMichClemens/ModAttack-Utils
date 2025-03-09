package bl.clemensyo.modAttackUtils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class config {
    public static Connection connection;
    public static Map<String, String> colorMap = new HashMap<String, String>();

    static {
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:modattackutils.db");
            colorMap.put("Schwarz", "§0");
            colorMap.put("Dunkelblau", "§1");
            colorMap.put("Dunkelgrün", "§2");
            colorMap.put("Dunkelaqua", "§3");
            colorMap.put("Dunkelrot", "§4");
            colorMap.put("Dunkellila", "§5");
            colorMap.put("Gold", "§6");
            colorMap.put("Grau", "§7");
            colorMap.put("Dunkelgrau", "§8");
            colorMap.put("Blau", "§9");
            colorMap.put("Grün", "§a");
            colorMap.put("Aqua", "§b");
            colorMap.put("Rot", "§c");
            colorMap.put("Helllila", "§d");
            colorMap.put("Gelb", "§e");
            colorMap.put("Weiß", "§f");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
