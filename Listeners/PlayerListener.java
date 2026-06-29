package hu.ClashRoyale456.wardenAnticheat.Listeners;

import hu.ClashRoyale456.wardenAnticheat.Checks.*;
import hu.ClashRoyale456.wardenAnticheat.Clients.ClientDetector;
import hu.ClashRoyale456.wardenAnticheat.Commands.subcommands.AlertsSubCommand;
import hu.ClashRoyale456.wardenAnticheat.Data.PlayerDataManager;
import hu.ClashRoyale456.wardenAnticheat.WardenAnticheat;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.player.*;
import java.util.UUID;

public class PlayerListener implements Listener {

    private final WardenAnticheat plugin;

    public PlayerListener(WardenAnticheat plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        PlayerDataManager.addPlayer(player);

        if (player.hasPermission("warden.alerts")) {
            AlertsSubCommand.alertsEnabled.add(player.getUniqueId());
        }

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!player.isOnline()) return;

            String clientBrand = ClientDetector.getClientName(player);
            String rawBrand = ClientDetector.getRawBrand(player);

            String joinMsg = "§4&lWarden §7» §f" + player.getName()
                    + " §7joined using §f" + clientBrand;

            Bukkit.getOnlinePlayers().stream()
                    .filter(p -> AlertsSubCommand.alertsEnabled.contains(p.getUniqueId()))
                    .forEach(p -> p.sendMessage(joinMsg));

            plugin.getLogger().info("[Warden] " + player.getName()
                    + " joined using " + clientBrand
                    + " (raw: " + rawBrand + ")");

        }, 40L);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        UUID uuid = e.getPlayer().getUniqueId();

        PlayerDataManager.removePlayer(e.getPlayer());
        ClientDetector.removePlayer(uuid);
        AlertsSubCommand.alertsEnabled.remove(uuid);

        plugin.getTriggerBotCheck().removePlayer(uuid);
        plugin.getAutoClickerCheck().removePlayer(uuid);
        plugin.getTimerCheck().removePlayer(uuid);
        plugin.getTimerLimitCheck().removePlayer(uuid);
        plugin.getBaritoneCheck().removePlayer(uuid);
        plugin.getScaffoldCheck().removePlayer(uuid);
        plugin.getAutoTotemCheck().removePlayer(uuid);
        plugin.getFlightCheck().removePlayer(uuid);
        plugin.getTimerCheck().removePlayer(uuid);
    }
}