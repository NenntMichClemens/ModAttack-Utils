package bl.clemensyo.modAttackUtils.essentials;

import bl.clemensyo.modAttackUtils.ModAttackUtils;
import bl.clemensyo.modAttackUtils.helpers;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class SendTPA implements CommandExecutor {

    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage("This command can only be used by players");
            return true;
        }

        if (strings.length != 1){
            player.sendMessage("Falsche Verwendung: /tpa <Player>");
            return true;
        }
        long currentTime = System.currentTimeMillis();
        ModAttackUtils pg = ModAttackUtils.getInstance();
        if (pg.getCombatLog().containsKey(player.getUniqueId())) {
            long lastCombatTime = pg.getCombatLog().get(player.getUniqueId());
            if (currentTime - lastCombatTime < 10000) {
                player.sendMessage(ChatColor.RED +"Du bist gerade im Kampf! Du kannst dich gerade nicht teleportieren!");
                return true;
            }
        }

        String targetname = strings[0];
        Player target = Bukkit.getPlayer(targetname);
        if (target == null){
            player.sendMessage("Spieler nicht gefunden.");
            return true;
        }
        TextComponent message = new TextComponent();
        message.addExtra(new TextComponent(ChatColor.BOLD + "" + ChatColor.UNDERLINE + player.getName()));
        message.addExtra(new TextComponent(ChatColor.RESET + " " + ChatColor.BOLD + "möchte sich zu dir teleportieren!\n\n"));
        message.addExtra(new TextComponent(ChatColor.BOLD + "" + ChatColor.UNDERLINE + "" + ChatColor.DARK_RED + "Du hast 60 Sekunden um diese Anfrage zu bearbeiten.\n"));

        TextComponent accept = new TextComponent(ChatColor.GREEN + "[Akzeptieren]");
        accept.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpaaccept " + player.getName() ));

        TextComponent decline = new TextComponent(ChatColor.RED + "[Ablehnen]");
        decline.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpadecline " + player.getName()));
        message.addExtra(accept);
        message.addExtra(new TextComponent(" ")); // Leerzeichen zwischen den Buttons
        message.addExtra(decline);

        // Nachricht senden

        ModAttackUtils.getInstance().getTpaRequests().put(target.getUniqueId(), player.getUniqueId());
        target.spigot().sendMessage(message);
        player.sendMessage(ChatColor.GREEN+"Deine TPA an " + target.getName() +" wurde versendet. Der Spieler hat 60 Sekunden Zeit diese zu bearbeiten.");

        new BukkitRunnable() {
            @Override
            public void run() {
                if (ModAttackUtils.getInstance().getTpaRequests().containsKey(target.getUniqueId())) {
                    ModAttackUtils.getInstance().getTpaRequests().remove(target.getUniqueId());
                    target.sendMessage(org.bukkit.ChatColor.RED + "Die TPA von " + player.getName() + " ist abgelaufen");
                    player.sendMessage(org.bukkit.ChatColor.RED + "Deine TPA an " + target.getName() + " ist abgelaufen.");
                }
            }
        }.runTaskLater(ModAttackUtils.getInstance(), 1200L); // 1200 Ticks = 60 Sekunden
        return true;
    }
}
