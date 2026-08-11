package hu.ClashRoyale456.wardenAnticheat.Utils;

import hu.ClashRoyale456.wardenAnticheat.WardenAnticheat;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import hu.ClashRoyale456.wardenAnticheat.Commands.subcommands.ReloadSubCommand;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Set;

public class ConfigManager {

    private final WardenAnticheat plugin;

    public ConfigManager(WardenAnticheat plugin) {
        this.plugin = plugin;
    }

    public void updateConfig() {
        File configFile = new File(plugin.getDataFolder(), "config.yml");

        if (!configFile.exists()) {
            plugin.saveDefaultConfig();
            return;
        }

        FileConfiguration currentConfig = YamlConfiguration.loadConfiguration(configFile);

        InputStream defaultStream = plugin.getResource("config.yml");
        if (defaultStream == null) return;

        YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(
                new InputStreamReader(defaultStream, StandardCharsets.UTF_8));

        double currentVersion = currentConfig.getDouble("config-version", 1.0);
        double defaultVersion = defaultConfig.getDouble("config-version", 1.0);

        if (currentVersion >= defaultVersion) return;

        plugin.getLogger().info("[Warden] Config frissítés: "
                + currentVersion + " → " + defaultVersion);

        int added = addMissingKeys(defaultConfig, currentConfig, "");

        currentConfig.set("config-version", defaultVersion);

        try {
            currentConfig.save(configFile);
            plugin.getLogger().info("[Warden] Config frissítve! " + added + " új kulcs hozzáadva.");
        } catch (IOException e) {
            plugin.getLogger().warning("[Warden] Config mentési hiba: " + e.getMessage());
        }

        plugin.reloadConfig();
    }

    private int addMissingKeys(FileConfiguration source, FileConfiguration target, String path) {
        int count = 0;
        Set<String> keys = source.getConfigurationSection(path.isEmpty() ? "" : path) != null
                ? source.getConfigurationSection(path.isEmpty() ? "" : path).getKeys(false)
                : Set.of();

        for (String key : keys) {
            String fullPath = path.isEmpty() ? key : path + "." + key;

            if (source.isConfigurationSection(fullPath)) {
                count += addMissingKeys(source, target, fullPath);
            } else {
                // Ha a kulcs hiányzik a jelenlegi configból, hozzáadjuk
                if (!target.contains(fullPath)) {
                    target.set(fullPath, source.get(fullPath));
                    plugin.getLogger().info("[Warden] Új config kulcs: " + fullPath);
                    count++;
                }
            }
        }
        return count;
    }
}