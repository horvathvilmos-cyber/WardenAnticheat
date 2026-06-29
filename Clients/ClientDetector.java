package hu.ClashRoyale456.wardenAnticheat.Clients;

import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPluginMessage;
import hu.ClashRoyale456.wardenAnticheat.Commands.subcommands.AlertsSubCommand;
import hu.ClashRoyale456.wardenAnticheat.WardenAnticheat;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.nio.charset.StandardCharsets;
import java.util.*;

public class ClientDetector extends PacketListenerAbstract {

    private final WardenAnticheat plugin;
    private static final Map<UUID, String> rawBrandMap = new HashMap<>();
    private static final Map<UUID, ClientBrand> clientBrandMap = new HashMap<>();

    public ClientDetector(WardenAnticheat plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() != PacketType.Play.Client.PLUGIN_MESSAGE) return;

        Player player = (Player) event.getPlayer();
        if (player == null) return;

        WrapperPlayClientPluginMessage packet = new WrapperPlayClientPluginMessage(event);
        String channel = packet.getChannelName();

        if (!channel.equals("minecraft:brand") && !channel.equals("MC|Brand")) return;

        byte[] data = packet.getData();
        if (data.length == 0) return;

        String rawBrand;
        try {
            rawBrand = new String(data, 1, data.length - 1, StandardCharsets.UTF_8).trim();
        } catch (Exception e) {
            rawBrand = "Unknown";
        }

        ClientBrand brand = ClientBrand.fromString(rawBrand);

        rawBrandMap.put(player.getUniqueId(), rawBrand);
        clientBrandMap.put(player.getUniqueId(), brand);

        String msg = "§6[Warden] §f" + player.getName()
                + " §ecsatlakozott | Client: §f" + brand.getDisplayName()
                + " §7(raw: " + rawBrand + ")";

        Bukkit.getOnlinePlayers().stream()
                .filter(p -> AlertsSubCommand.alertsEnabled.contains(p.getUniqueId()))
                .forEach(p -> p.sendMessage(msg));

        plugin.getLogger().info("[Warden] " + player.getName()
                + " | Client: " + brand.getDisplayName()
                + " (raw: " + rawBrand + ")");
    }

    public static ClientBrand getClient(Player player) {
        return clientBrandMap.getOrDefault(player.getUniqueId(), ClientBrand.UNKNOWN);
    }

    public static String getRawBrand(Player player) {
        return rawBrandMap.getOrDefault(player.getUniqueId(), "Unknown");
    }

    public static String getClientName(Player player) {
        return getClient(player).getDisplayName();
    }

    public static void removePlayer(UUID uuid) {
        rawBrandMap.remove(uuid);
        clientBrandMap.remove(uuid);
    }
}