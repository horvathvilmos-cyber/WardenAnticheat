package hu.ClashRoyale456.wardenAnticheat.Hooks;

import hu.ClashRoyale456.wardenAnticheat.Clients.ClientDetector;
import hu.ClashRoyale456.wardenAnticheat.Data.PlayerData;
import hu.ClashRoyale456.wardenAnticheat.Data.PlayerDataManager;
import hu.ClashRoyale456.wardenAnticheat.WardenAnticheat;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class WardenExpansion extends PlaceholderExpansion {

    private final WardenAnticheat plugin;

    public WardenExpansion(WardenAnticheat plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() { return "warden"; }

    @Override
    public @NotNull String getAuthor() { return "ClashRoyale456"; }

    @Override
    public @NotNull String getVersion() { return plugin.getDescription().getVersion(); }

    @Override
    public boolean persist() { return true; }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String identifier) {
        if (player == null) return "";

        PlayerData data = PlayerDataManager.getData(player);

        switch (identifier.toLowerCase()) {
            case "client-brand":
                return ClientDetector.getClientName(player);

            case "client-brand-raw":
                return ClientDetector.getRawBrand(player);

            case "player-uuid":
                return player.getUniqueId().toString();

            case "player-checks":
                if (data == null) return "0";
                return String.valueOf(data.getTotalViolations());

            case "player-version":
                return getPlayerVersion(player);

            case "fast-math":
                return detectFastMath(player);

            case "player-violations":
                if (data == null) return "None";
                if (data.getAllViolations().isEmpty()) return "None";
                StringBuilder sb = new StringBuilder();
                data.getAllViolations().forEach((check, vl) ->
                        sb.append(check).append(":").append(vl).append(" "));
                return sb.toString().trim();

            case "player-top-check":
                if (data == null) return "None";
                return data.getAllViolations().entrySet().stream()
                        .max(java.util.Map.Entry.comparingByValue())
                        .map(java.util.Map.Entry::getKey)
                        .orElse("None");

            case "player-online":
                if (data == null) return "0";
                long mins = (System.currentTimeMillis() - data.getJoinTime()) / 60000;
                return mins + " perc";

            case "player-ping":
                return player.getPing() + "ms";

            default:
                return null;
        }
    }

    private String getPlayerVersion(Player player) {
        try {
            return player.getClientBrandName() != null
                    ? player.getClientBrandName()
                    : "Unknown";
        } catch (Exception e) {
            return "Unknown";
        }
    }

    private String detectFastMath(Player player) {
        String brand = ClientDetector.getRawBrand(player).toLowerCase();
        if (brand.contains("lunar") || brand.contains("forge") || brand.contains("labymod")) {
            return "Possible";
        }
        return "Unknown";
    }
}