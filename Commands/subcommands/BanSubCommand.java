package hu.ClashRoyale456.wardenAnticheat.Commands.subcommands;

import hu.ClashRoyale456.wardenAnticheat.Commands.SubCommand;
import org.bukkit.Bukkit;
import org.bukkit.BanList;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.*;
import java.util.stream.Collectors;

public class BanSubCommand implements SubCommand {

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

        String reason = args.length >= 3
                ? String.join(" ", Arrays.copyOfRange(args, 2, args.length))
                : "Anticheat - Warden";

        Bukkit.getBanList(BanList.Type.NAME)
                .addBan(target.getName(), reason, null, "Warden");
        target.kickPlayer("§c[Warden] Bannolva\n§f" + reason);
        Bukkit.broadcastMessage("§c[Warden] §f" + target.getName() + " §cbannolva: §f" + reason);
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
    public String getPermission() { return "warden.ban"; }
    @Override
    public String getUsage() { return "/warden ban <játékos> [ok]"; }
    @Override
    public String getDescription() { return "Játékos bannolása"; }
}