package hu.ClashRoyale456.wardenAnticheat.Hooks;

import hu.ClashRoyale456.wardenAnticheat.Clients.ClientDetector;
import hu.ClashRoyale456.wardenAnticheat.WardenAnticheat;
import org.bukkit.entity.Player;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class Discord {

    private final WardenAnticheat plugin;

    public Discord(WardenAnticheat plugin) {
        this.plugin = plugin;
    }

    public boolean isEnabled() {
        return plugin.getConfig().getBoolean("hooks.Discord.enableade", false);
    }

    public void sendAlert(Player player, String checkName, int vl) {
        if (!isEnabled()) return;

        String webhook = plugin.getConfig().getString("hooks.Discord.webhook", "");
        if (webhook.isEmpty()) return;

        String template = plugin.getConfig().getString(
                "hooks.Discord.Message",
                "**Warden**>> %player% failed %cheat% (%client%, %cnumber%)"
        );

        String message = template
                .replace("%player%", player.getName())
                .replace("%cheat%", checkName)
                .replace("%client%", ClientDetector.getClientName(player))
                .replace("%cnumber%", String.valueOf(vl));

        new Thread(() -> sendWebhook(webhook, message)).start();
    }

    private void sendWebhook(String webhookUrl, String message) {
        try {
            URL url = new URL(webhookUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            String json = "{\"content\": \"" + message.replace("\"", "\\\"") + "\"}";
            try (OutputStream os = conn.getOutputStream()) {
                os.write(json.getBytes(StandardCharsets.UTF_8));
            }

            conn.getResponseCode();
            conn.disconnect();
        } catch (Exception e) {
            plugin.getLogger().warning("[Warden] Discord webhook hiba: " + e.getMessage());
        }
    }
}
