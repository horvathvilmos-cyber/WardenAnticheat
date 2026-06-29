package hu.ClashRoyale456.wardenAnticheat.Checks;

import hu.ClashRoyale456.wardenAnticheat.Commands.subcommands.AlertsSubCommand;
import hu.ClashRoyale456.wardenAnticheat.Data.PlayerData;
import hu.ClashRoyale456.wardenAnticheat.Data.PlayerDataManager;
import hu.ClashRoyale456.wardenAnticheat.WardenAnticheat;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public abstract class Check {

    protected final WardenAnticheat plugin;
    protected final String checkName;

    public Check(WardenAnticheat plugin, String checkName) {
        this.plugin = plugin;
        this.checkName = checkName;
    }

    protected void flag(Player player, String detail) {
        PlayerData data = PlayerDataManager.getData(player);
        if (data == null) return;

        if (!plugin.getConfig().getBoolean("Checks." + checkName + ".enableade", true)) return;

        data.addViolation(checkName, 1);
        int vl = data.getViolations(checkName);

        String alertMsg = "§c[Warden] §f" + player.getName()
                + " §cfailed §e" + checkName
                + " §7(VL: " + vl + ") §f" + detail;

        Bukkit.getOnlinePlayers().stream()
                .filter(p -> AlertsSubCommand.alertsEnabled.contains(p.getUniqueId()))
                .forEach(p -> p.sendMessage(alertMsg));

        plugin.getLogger().info(player.getName() + " failed " + checkName
                + " VL:" + vl + " " + detail);

        if (plugin.getMySQL() != null && plugin.getMySQL().isConnected()) {
            plugin.getMySQL().saveViolation(player.getUniqueId(), checkName, vl, detail);
        }

        if (plugin.getDiscordHook() != null) {
            plugin.getDiscordHook().sendAlert(player, checkName, vl);
        }

        if (plugin.getVelocitySupport() != null) {
            plugin.getVelocitySupport().broadcastAlert(player, checkName, vl, detail);
        }

        int threshold = plugin.getConfig().getInt("Checks." + checkName + ".punish-threshold", 10);
        if (plugin.getConfig().getBoolean("Checks." + checkName + ".auto-punish", false)
                && vl >= threshold) {
            punish(player);
            data.resetViolations(checkName);
        }
    }

    private void punish(Player player) {
        var commands = plugin.getConfig().getStringList("Checks." + checkName + ".punish");
        for (String cmd : commands) {
            String finalCmd = cmd.replace("%player%", player.getName());
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalCmd);
        }

        if (plugin.getMySQL() != null && plugin.getMySQL().isConnected()) {
            plugin.getMySQL().savePunishment(
                    player.getUniqueId(),
                    "kick",
                    "Anticheat - " + checkName,
                    checkName
            );
        }
    }
}