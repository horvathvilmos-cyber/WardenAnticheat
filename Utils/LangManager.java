package hu.ClashRoyale456.wardenAnticheat.Utils;

import hu.ClashRoyale456.wardenAnticheat.WardenAnticheat;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class LangManager {

    private final WardenAnticheat plugin;
    private FileConfiguration lang;
    private File langFile;

    public LangManager(WardenAnticheat plugin) {
        this.plugin = plugin;
        setup();
    }

    private void setup() {
        langFile = new File(plugin.getDataFolder(), "lang.yml");
        if (!langFile.exists()) {
            plugin.saveResource("lang.yml", false);
        }
        lang = YamlConfiguration.loadConfiguration(langFile);

        InputStream defaultStream = plugin.getResource("lang.yml");
        if (defaultStream != null) {
            YamlConfiguration defaultLang = YamlConfiguration.loadConfiguration(
                    new InputStreamReader(defaultStream, StandardCharsets.UTF_8));
            lang.setDefaults(defaultLang);
            lang.options().copyDefaults(true);
            save();
        }
    }

    public void reload() {
        lang = YamlConfiguration.loadConfiguration(langFile);
        InputStream defaultStream = plugin.getResource("lang.yml");
        if (defaultStream != null) {
            YamlConfiguration defaultLang = YamlConfiguration.loadConfiguration(
                    new InputStreamReader(defaultStream, StandardCharsets.UTF_8));
            lang.setDefaults(defaultLang);
            lang.options().copyDefaults(true);
            save();
        }
    }

    private void save() {
        try {
            lang.save(langFile);
        } catch (IOException e) {
            plugin.getLogger().warning("[Warden] Lang fájl mentési hiba: " + e.getMessage());
        }
    }

    public String get(String path, String... replacements) {
        String prefix = translate(lang.getString("prefix", "&4Warden &8» &r"));
        String message = lang.getString(path, "&cHiányzó üzenet: " + path);
        message = message.replace("%prefix%", prefix);

        for (int i = 0; i + 1 < replacements.length; i += 2) {
            message = message.replace(replacements[i], replacements[i + 1]);
        }

        return translate(message);
    }

    private String translate(String message) {
        return ChatColor.translateAlternateColorCodes('&',
                message.replace("\\n", "\n"));
    }

    public FileConfiguration getConfig() { return lang; }
}