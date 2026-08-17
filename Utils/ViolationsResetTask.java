package hu.ClashRoyale456.wardenAnticheat.Utils;

import hu.ClashRoyale456.wardenAnticheat.Data.PlayerDataManager;
import hu.ClashRoyale456.wardenAnticheat.WardenAnticheat;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class ViolationsResetTask extends BukkitRunnable {

    private final WardenAnticheat plugin;

    public ViolationsResetTask(WardenAnticheat plugin) {
        this.plugin = plugin;

    }

    @Override
    public void run() {
        if (!plugin.getConfig().getBoolean("violations.auto-reset.enabled", true)) return;

        int count = 0;
        for (Player player : Bukkit.getOnlinePlayers()) {
            var data = PlayerDataManager.getData(player);
            if (data != null && data.getTotalViolations() > 0) {
                data.resetViolations();
            }
        }

        if (count > 0) {
            plugin.getLogger().info("Warden » " + count + "játékos violations resetelve.");
        }
    }

    public static void start(WardenAnticheat plugin) {
        if (!plugin.getConfig().getBoolean("violations.auto-reset.enabled", true)) return;

        int intervalMinutes = plugin.getConfig()
                .getInt("violations.auto-reset.interval", 10);
        long intervalTicks = intervalMinutes * 60L * 20L;

        new ViolationsResetTask(plugin).runTaskTimer(plugin, intervalTicks, intervalTicks);
        plugin.getLogger().info("Warden » Violations auto-reset: " + intervalMinutes + " percenként");
    }
}
