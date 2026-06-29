package hu.ClashRoyale456.wardenAnticheat.Commands;

import org.bukkit.command.CommandSender;
import java.util.List;

public interface SubCommand {
    void execute(CommandSender sender, String[] args);
    List<String> tabComplete(CommandSender sender, String[] args);
    String getPermission();
    String getUsage();
    String getDescription();
}