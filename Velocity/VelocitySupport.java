package hu.ClashRoyale456.wardenAnticheat.Velocity;

import hu.ClashRoyale456.wardenAnticheat.Clients.ClientDetector;
import hu.ClashRoyale456.wardenAnticheat.Commands.subcommands.AlertsSubCommand;
import hu.ClashRoyale456.wardenAnticheat.Data.PlayerDataManager;
import hu.ClashRoyale456.wardenAnticheat.WardenAnticheat;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.io.*;
import java.util.UUID;

public class VelocitySupport implements PluginMessageListener {

    private final WardenAnticheat plugin;
    public static final String CHANNEL = "warden:proxy";

    public VelocitySupport(WardenAnticheat plugin) {
        this.plugin = plugin;
    }

    public boolean setup() {
        if (!plugin.getConfig().getBoolean("Velocity.enabled", false)) return false;

        plugin.getServer().getMessenger().registerIncomingPluginChannel(plugin, CHANNEL, this);
        plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, CHANNEL);
        plugin.getServer().getMessenger().registerIncomingPluginChannel(plugin, "BungeeCord", this);
        plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, "BungeeCord");

        plugin.getLogger().info("[Warden] Velocity support bekapcsolva!");
        return true;
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (!channel.equals(CHANNEL)) return;

        try {
            DataInputStream in = new DataInputStream(new ByteArrayInputStream(message));
            String subChannel = in.readUTF();

            switch (subChannel) {
                case "PlayerIP": {
                    String playerName = in.readUTF();
                    String ip = in.readUTF();
                    plugin.getLogger().info("[Warden] " + playerName + " IP: " + ip);
                    break;
                }

                case "GetViolations": {
                    String targetName = in.readUTF();
                    Player target = plugin.getServer().getPlayer(targetName);
                    if (target == null) return;

                    var data = PlayerDataManager.getData(target);
                    if (data == null) return;

                    sendToProxy(player, "Violations", targetName,
                            String.valueOf(data.getTotalViolations()));
                    break;
                }

                case "BanPlayer": {
                    String targetName = in.readUTF();
                    String reason = in.readUTF();
                    Player target = plugin.getServer().getPlayer(targetName);
                    if (target != null) {
                        target.kickPlayer("§c[Warden] Bannolva\n§f" + reason);
                    }
                    break;
                }

                case "Alert": {
                    String serverName = in.readUTF();
                    String alertMsg = in.readUTF();

                    plugin.getServer().getOnlinePlayers().stream()
                            .filter(p -> AlertsSubCommand.alertsEnabled.contains(p.getUniqueId()))
                            .forEach(p -> p.sendMessage(
                                    "§8[§6" + serverName + "§8] " + alertMsg));
                    break;
                }
            }
        } catch (IOException e) {
            plugin.getLogger().warning("[Warden] Velocity üzenet hiba: " + e.getMessage());
        }
    }

    public void sendToProxy(Player player, String... data) {
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(bytes);
            for (String d : data) out.writeUTF(d);
            player.sendPluginMessage(plugin, CHANNEL, bytes.toByteArray());
        } catch (IOException e) {
            plugin.getLogger().warning("[Warden] Proxy üzenet küldési hiba: " + e.getMessage());
        }
    }

    public void broadcastAlert(Player player, String checkName, int vl, String detail) {
        if (!plugin.getConfig().getBoolean("Velocity.enabled", false)) return;

        String alertMsg = "§c[Warden] §f" + player.getName()
                + " §cfailed §e" + checkName
                + " §7(VL: " + vl + ") §f" + detail;

        sendToProxy(player, "Alert",
                plugin.getServer().getName(),
                alertMsg);
    }

    public void disable() {
        plugin.getServer().getMessenger().unregisterIncomingPluginChannel(plugin, CHANNEL);
        plugin.getServer().getMessenger().unregisterOutgoingPluginChannel(plugin, CHANNEL);
    }
}
