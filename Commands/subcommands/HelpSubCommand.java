package hu.ClashRoyale456.wardenAnticheat.Commands.subcommands;

import hu.ClashRoyale456.wardenAnticheat.Commands.SubCommand;
import org.bukkit.command.CommandSender;
import java.util.*;

public class HelpSubCommand implements SubCommand {

    @Override
    public void execute(CommandSender sender, String[] args) {
        sender.sendMessage("§6§l=== Warden Anticheat ===");
        sender.sendMessage("§e/warden help §7- Parancsok listája");
        sender.sendMessage("§e/warden playerinfo <játékos> §7- Játékos adatai");
        sender.sendMessage("§e/warden violations <játékos> §7- Violations lista");
        sender.sendMessage("§e/warden alerts §7- Alert ki/be kapcsolás");
        sender.sendMessage("§e/warden kick <játékos> [ok] §7- Kirúgás");
        sender.sendMessage("§e/warden ban <játékos> [ok] §7- Bannolás");
        sender.sendMessage("§e/warden reload §7- Config újratöltés");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) { return Collections.emptyList(); }

    @Override
    public String getPermission() { return "warden.help"; }
    @Override
    public String getUsage() { return "/warden help"; }
    @Override
    public String getDescription() { return "Parancsok listája"; }
}