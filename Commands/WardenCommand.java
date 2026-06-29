package hu.ClashRoyale456.wardenAnticheat.Commands;

import hu.ClashRoyale456.wardenAnticheat.Commands.subcommands.*;
import hu.ClashRoyale456.wardenAnticheat.WardenAnticheat;
import org.bukkit.command.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public class WardenCommand implements CommandExecutor, TabCompleter {

    private final Map<String, SubCommand> subCommands = new LinkedHashMap<>();

    public WardenCommand(WardenAnticheat plugin) {
        subCommands.put("help",       new HelpSubCommand());
        subCommands.put("playerinfo", new PlayerInfoSubCommand());
        subCommands.put("violations", new ViolationsSubCommand());
        subCommands.put("alerts",     new AlertsSubCommand());
        subCommands.put("kick",       new KickSubCommand());
        subCommands.put("ban",        new BanSubCommand());
        subCommands.put("reload",     new ReloadSubCommand(plugin));
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            sender.sendMessage("§6[Warden] §eHasználat: /warden help");
            return true;
        }

        SubCommand sub = subCommands.get(args[0].toLowerCase());
        if (sub == null) {
            sender.sendMessage("§c[Warden] Ismeretlen parancs! Próbáld: §e/warden help");
            return true;
        }

        if (!sender.hasPermission(sub.getPermission())) {
            sender.sendMessage("§c[Warden] Nincs jogosultságod ehhez!");
            return true;
        }

        sub.execute(sender, args);
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                                @NotNull String label, @NotNull String[] args) {
        if (args.length == 1)
            return subCommands.keySet().stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());

        SubCommand sub = subCommands.get(args[0].toLowerCase());
        if (sub != null) return sub.tabComplete(sender, args);

        return Collections.emptyList();
    }
}