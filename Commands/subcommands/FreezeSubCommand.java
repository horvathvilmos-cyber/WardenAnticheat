package hu.ClashRoyale456.wardenAnticheat.Commands.subcommands;

import hu.ClashRoyale456.wardenAnticheat.Commands.SubCommand;
import hu.ClashRoyale456.wardenAnticheat.WardenAnticheat;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.*;
import java.util.stream.Collectors;

public class FreezeSubCommand implements SubCommand, Listener {

    private final WardenAnticheat plugin;
    private static final Set<UUID> frozenPlayers = new HashSet<>();

    public FreezeSubCommand(WardenAnticheat plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(plugin.getLangManager().get("commands.usage"));
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(plugin.getLangManager().get("commands.player-not-found"));
            return;
        }

        UUID uuid = target.getUniqueId();

        if (frozenPlayers.contains(uuid)) {
            frozenPlayers.remove(uuid);
            target.sendMessage("§a[Warden] Feloldottak a befagyasztás alól.");
            sender.sendMessage("§a[Warden] §f" + target.getName() + " §afeloldva.");
            plugin.getLogger().info("[Warden] " + target.getName() + " unfrozen by " + sender.getName());
        } else {
            frozenPlayers.add(uuid);
            target.sendMessage("§c[Warden] Befagyasztottak! Várj egy staff döntésére.");
            target.sendMessage("§7Ha azt hiszed ez tévedés, írj a staffnak.");
            sender.sendMessage("§a[Warden] §f" + target.getName() + " §abefagyasztva.");
            plugin.getLogger().info("[Warden] " + target.getName() + " frozen by " + sender.getName());

            Bukkit.getScheduler().runTaskTimer(plugin, task -> {
                if (!frozenPlayers.contains(uuid) || !target.isOnline()) {
                    task.cancel();
                    return;
                }
                target.sendMessage("§c[Warden] §lBE VAGY FAGYASZTVA! §r§cVárj egy staff döntésére!");
            }, 100L, 100L);
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        Player player = e.getPlayer();
        if (!frozenPlayers.contains(player.getUniqueId())) return;

        // Csak pozíció változást blokkoljuk, fejforgatást engedjük
        if (e.getFrom().getX() != e.getTo().getX()
                || e.getFrom().getY() != e.getTo().getY()
                || e.getFrom().getZ() != e.getTo().getZ()) {
            e.setTo(e.getFrom().setDirection(e.getTo().getDirection()));
            player.sendMessage("§c[Warden] Nem tudsz mozogni amíg be vagy fagyasztva!");
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        if (!frozenPlayers.contains(player.getUniqueId())) return;

        frozenPlayers.remove(player.getUniqueId());

        Bukkit.getOnlinePlayers().stream()
                .filter(p -> p.hasPermission("warden.freeze"))
                .forEach(p -> p.sendMessage("§c[Warden] §f" + player.getName()
                        + " §ckilépett miközben be volt fagyasztva!"));

        plugin.getLogger().info("[Warden] " + player.getName()
                + " disconnected while frozen!");
    }

    public static boolean isFrozen(UUID uuid) { return frozenPlayers.contains(uuid); }

    public static void removePlayer(UUID uuid) { frozenPlayers.remove(uuid); }

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
    public String getPermission() { return "warden.freeze"; }
    @Override
    public String getUsage() { return "/warden freeze <játékos>"; }
    @Override
    public String getDescription() { return "Játékos befagyasztása"; }
}