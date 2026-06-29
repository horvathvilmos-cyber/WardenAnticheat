package hu.ClashRoyale456.wardenAnticheat.Commands.subcommands;

import hu.ClashRoyale456.wardenAnticheat.Commands.SubCommand;
import hu.ClashRoyale456.wardenAnticheat.WardenAnticheat;
import org.bukkit.command.CommandSender;
import java.util.*;

public class ReloadSubCommand implements SubCommand {

    private final WardenAnticheat plugin;

    public ReloadSubCommand(WardenAnticheat plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        plugin.reloadConfig();
        sender.sendMessage("§a[Warden] Config sikeresen újratöltve!");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) { return Collections.emptyList(); }

    @Override
    public String getPermission() { return "warden.reload"; }
    @Override
    public String getUsage() { return "/warden reload"; }
    @Override
    public String getDescription() { return "Config újratöltése"; }
}