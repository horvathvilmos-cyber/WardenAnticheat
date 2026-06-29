package hu.ClashRoyale456.wardenAnticheat.Commands.subcommands;

import hu.ClashRoyale456.wardenAnticheat.Commands.SubCommand;
import hu.ClashRoyale456.wardenAnticheat.Data.PlayerData;
import hu.ClashRoyale456.wardenAnticheat.Data.PlayerDataManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.*;
import java.util.stream.Collectors;

public class ViolationsSubCommand implements SubCommand {

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§cHasználat: " + getUsage());
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage("§c[Warden] A játékos nem található vagy nincs online!");
            return;
        }

        PlayerData data = PlayerDataManager.getData(target);
        if (data == null) {
            sender.sendMessage("§c[Warden] Nem található adat ehhez a játékoshoz!");
            return;
        }

        sender.sendMessage("§6§l=== " + target.getName() + " – Violations ===");

        if (data.getAllViolations().isEmpty()) {
            sender.sendMessage("§7Nincs egyetlen flag sem.");
            return;
        }

        data.getAllViolations().entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .forEach(e -> {
                    long ago = (System.currentTimeMillis() - data.getLastFlagged(e.getKey())) / 1000;
                    sender.sendMessage("§c" + e.getKey() + ": §f" + e.getValue()
                            + " VL §7(utoljára: " + ago + " mp-je)");
                });
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
    public String getPermission() { return "warden.violations"; }
    @Override
    public String getUsage() { return "/warden violations <játékos>"; }
    @Override
    public String getDescription() { return "Részletes violations lista"; }
}