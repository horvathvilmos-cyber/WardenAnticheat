package hu.ClashRoyale456.wardenAnticheat.Commands.subcommands;

import hu.ClashRoyale456.wardenAnticheat.Commands.SubCommand;
import hu.ClashRoyale456.wardenAnticheat.WardenAnticheat;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class SpectateSubCommand implements SubCommand {

    private final WardenAnticheat plugin;
    private static final Map<UUID, GameMode> originalGameMode = new HashMap<>();
    private static final Map<UUID, UUID> spectating = new HashMap<>();

    public SpectateSubCommand(WardenAnticheat plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getLangManager().get("commands.player-only"));
            return;
        }

        Player viewer = (Player) sender;

        if (spectating.containsKey(viewer.getUniqueId())) {
            stopSpectating(viewer);
            return;
        }

        if (args.length < 2) {
            sender.sendMessage(plugin.getLangManager().get("commands.usage"));
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(plugin.getLangManager().get("commands.player-not-found"));
            return;
        }

        if (target.equals(viewer)) {
            viewer.sendMessage("§c[Warden] Nem spectate-elheted saját magad!");
            return;
        }

        startSpectating(viewer, target);
    }

    private void startSpectating(Player viewer, Player target) {
        originalGameMode.put(viewer.getUniqueId(), viewer.getGameMode());
        spectating.put(viewer.getUniqueId(), target.getUniqueId());

        viewer.setGameMode(GameMode.SPECTATOR);

        viewer.teleport(target.getLocation());

        viewer.setSpectatorTarget(target);

        viewer.sendMessage("§a[Warden] Most §f" + target.getName()
                + " §aspectate-eled. §7Írd: §e/warden spectate §7a kilépéshez.");
        plugin.getLogger().info("[Warden] " + viewer.getName()
                + " spectating " + target.getName());
    }

    public static void stopSpectating(Player viewer) {
        UUID targetUUID = spectating.remove(viewer.getUniqueId());

        GameMode original = originalGameMode.remove(viewer.getUniqueId());
        if (original != null) {
            viewer.setGameMode(original);
        }

        viewer.setSpectatorTarget(null);

        String targetName = targetUUID != null
                ? (Bukkit.getPlayer(targetUUID) != null
                ? Bukkit.getPlayer(targetUUID).getName()
                : "Unknown")
                : "Unknown";

        viewer.sendMessage("§c[Warden] Leállt a spectate: §f" + targetName);
    }

    public static boolean isSpectating(UUID uuid) {
        return spectating.containsKey(uuid);
    }

    public static UUID getSpectatingTarget(UUID uuid) {
        return spectating.get(uuid);
    }

    public static void removePlayer(UUID uuid) {
        spectating.remove(uuid);
        originalGameMode.remove(uuid);
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
    public String getPermission() { return "warden.spectate"; }
    @Override
    public String getUsage() { return "/warden spectate <játékos>"; }
    @Override
    public String getDescription() { return "Játékos spectate-elése"; }
}