package hu.ClashRoyale456.wardenAnticheat.Commands.subcommands;

import hu.ClashRoyale456.wardenAnticheat.Commands.SubCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.*;
import org.bukkit.ChatColor;

public class AlertsSubCommand implements SubCommand {

    public static final Set<UUID> alertsEnabled = new HashSet<>();

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Warden » You are not an Player");
            return;
        }

        Player p = (Player) sender;

        if (alertsEnabled.contains(p.getUniqueId())) {
            alertsEnabled.remove(p.getUniqueId());
            p.sendMessage(ChatColor.RED + "Warden » Alerts §lKIKAPCSOLVA§r§c.");
        } else {
            alertsEnabled.add(p.getUniqueId());
            p.sendMessage(ChatColor.GREEN + " Warden » Alerts BEKAPCSOLVA");
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) { return Collections.emptyList(); }

    @Override
    public String getPermission() { return "warden.alerts"; }
    @Override
    public String getUsage() { return "/warden alerts"; }
    @Override
    public String getDescription() { return "Alert ki/be kapcsolás"; }
}