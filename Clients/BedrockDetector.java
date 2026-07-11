package hu.ClashRoyale456.wardenAnticheat.Clients;

import hu.ClashRoyale456.wardenAnticheat.WardenAnticheat;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.geysermc.floodgate.api.FloodgateApi;
import org.geysermc.floodgate.api.player.FloodgatePlayer;

import java.util.*;

public class BedrockDetector {

    private final WardenAnticheat plugin;
    private boolean floodgateEnabled = false;
    private static final Set<UUID> bedrockPlayers = new HashSet<>();

    public BedrockDetector(WardenAnticheat plugin) {
        this.plugin = plugin;
    }

    public boolean setup() {
        if (Bukkit.getPluginManager().getPlugin("floodgate") == null) {
            plugin.getLogger().info("Floodgate nem található, Bedrock detektálás kikapcsolva");
            return false;
        }

        floodgateEnabled = true;
        plugin.getLogger().info("Floodgate hook sikeres, Bedrock detektálás elkezdődött");
        return true;
    }

    public boolean isEnabled () { return floodgateEnabled ;}

    public boolean isBedrock(Player player) {
        if (!floodgateEnabled) return false;
        try {
            return FloodgateApi.getInstance().isFloodgatePlayer(player.getUniqueId());
        } catch (Exception e) {
            return false;
        }
    }

    public FloodgatePlayer getFloodgatePlayer(Player player) {
        if (!floodgateEnabled) return null;
        try {
            return FloodgateApi.getInstance().getPlayer(player.getUniqueId());
        } catch (Exception e) {
            return null;
        }
    }

    public String getBedrocVersion(Player player) {
        FloodgatePlayer fp = getFloodgatePlayer(player);
        if (fp == null) return "Unknow";
        return fp.getDeviceOs().toString();
    }

    public String getDaviceType(Player player) {
        FloodgatePlayer fp = getFloodgatePlayer(player);
        if (fp == null) return "Unknow";
        return fp.getXuid();
    }

    public static void addBedrock(UUID uuid) { bedrockPlayers.add(uuid); }
    public static void removeBedrock(UUID uuid) {bedrockPlayers.remove(uuid); }
    public static boolean isCacheBedrock(UUID uuid) { return bedrockPlayers.contains(uuid); }
}
