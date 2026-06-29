package hu.ClashRoyale456.wardenAnticheat.Commands.subcommands;

import hu.ClashRoyale456.wardenAnticheat.Commands.SubCommand;
import hu.ClashRoyale456.wardenAnticheat.Data.PlayerData;
import hu.ClashRoyale456.wardenAnticheat.Data.PlayerDataManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.*;
import java.util.stream.Collectors;

public class PlayerInfoSubCommand implements SubCommand {

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§cHasználat: " + getUsage());
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage("§4Warden&7>>A játékos nem található vagy nincs online!");
            return;
        }

        PlayerData data = PlayerDataManager.getData(target);
        if (data == null) {
            sender.sendMessage("§4Warden&7>> Nem található adat ehhez a játékoshoz!");
            return;
        }

        long onlineMins = (System.currentTimeMillis() - data.getJoinTime()) / 60000;

        sender.sendMessage("§6§l=== " + target.getName() + " ===");
        sender.sendMessage("§ePing: §f" + target.getPing() + "ms");
        sender.sendMessage("§eGamemode: §f" + target.getGameMode());
        sender.sendMessage("§eOnline: §f" + onlineMins + " perc");
        sender.sendMessage("§eÖsszes VL: §c" + data.getTotalViolations());

        if (data.getAllViolations().isEmpty()) {
            sender.sendMessage("§7Nincs flag.");
        } else {
            sender.sendMessage("§eViolations:");
            data.getAllViolations().forEach((check, vl) ->
                    sender.sendMessage("  §c" + check + ": §f" + vl + " VL"));
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 2)
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(n -> n.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        return Collections.emptyList();
    }

    @Override
    public String getPermission() { return "warden.playerinfo"; }
    @Override
    public String getUsage() { return "/warden playerinfo <játékos>"; }
    @Override
    public String getDescription() { return "Játékos adatainak megtekintése"; }
}