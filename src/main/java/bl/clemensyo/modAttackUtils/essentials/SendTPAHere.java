package bl.clemensyo.modAttackUtils.essentials;

import bl.clemensyo.modAttackUtils.ModAttackUtils;
import bl.clemensyo.modAttackUtils.helpers;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class SendTPAHere implements CommandExecutor {
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player requester)) {
            sender.sendMessage("Nur Spieler können dieses Befehl verwenden");
            return true;
        }
        if (args.length < 1) {
            requester.sendMessage("Falsche Verwendung: /tpahere <player>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            requester.sendMessage("Spieler nicht gefunden.");
            return true;
        }

        long currentTime = System.currentTimeMillis();
        ModAttackUtils pg = ModAttackUtils.getInstance();
        if (pg.getCombatLog().containsKey(target.getUniqueId())) {
            long lastCombatTime = pg.getCombatLog().get(target.getUniqueId());
            if (currentTime - lastCombatTime < 10000) {
                requester.sendMessage(ChatColor.RED +"Dieser Spieler ist gerade im Kampf! Er kann gerade nicht zu dir teleportiert werden.");
                return true;
            }
        }

        // TPHere-Anfrage speichern
        ModAttackUtils.getInstance().getTpahereRequests().put(target.getUniqueId(), requester.getUniqueId());

        // Nachricht und Buttons erstellen
        TextComponent message = new TextComponent();
        message.addExtra(new TextComponent(ChatColor.BOLD + "" + ChatColor.UNDERLINE + requester.getName()));
        message.addExtra(new TextComponent(ChatColor.RESET + " " + ChatColor.BOLD + "möchte, dass du zu ihm teleportiert wirst!\n\n"));
        message.addExtra(new TextComponent(ChatColor.BOLD + "" + ChatColor.UNDERLINE + "" + ChatColor.DARK_RED + "Du hast 60 Sekunden um diese Anfrage zu bearbeiten.\n"));

        TextComponent accept = new TextComponent(ChatColor.GREEN + "[Accept]");
        accept.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpaaccept " + requester.getName()));

        TextComponent decline = new TextComponent(ChatColor.RED + "[Decline]");
        decline.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpadecline " + requester.getName()));

        message.addExtra(accept);
        message.addExtra(new TextComponent(" ")); // Leerzeichen zwischen den Buttons
        message.addExtra(decline);

        // Nachricht an den Zielspieler senden
        target.spigot().sendMessage(message);

        // Timer zum Entfernen der TPA-Anfrage nach 60 Sekunden
        new BukkitRunnable() {
            @Override
            public void run() {
                if (ModAttackUtils.getInstance().getTpaRequests().containsKey(target.getUniqueId())) {
                    ModAttackUtils.getInstance().getTpaRequests().remove(target.getUniqueId());
                    target.sendMessage(org.bukkit.ChatColor.RED + "Die TPA von " + requester.getName() + " ist abgelaufen");
                    requester.sendMessage(org.bukkit.ChatColor.RED + "Deine TPA an " + target.getName() + " ist abgelaufen.");
                }
            }
        }.runTaskLater(ModAttackUtils.getInstance(), 1200L); // 1200 Ticks = 60 Sekunden

        return true;
    }
}
