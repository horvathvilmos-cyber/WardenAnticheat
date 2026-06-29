package hu.ClashRoyale456.wardenAnticheat.Hooks;

import hu.ClashRoyale456.wardenAnticheat.WardenAnticheat;
import org.bukkit.Bukkit;

public class PlaceholderAPIHook {

    private final WardenAnticheat plugin;
    private boolean hooked = false;

    public PlaceholderAPIHook(WardenAnticheat plugin) {
        this.plugin = plugin;
    }

    public boolean setup() {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") == null) {
            plugin.getLogger().warning("[Warden] PlaceholderAPI nem található!");
            return false;
        }

        new WardenExpansion(plugin).register();
        hooked = true;
        plugin.getLogger().info("[Warden] PlaceholderAPI hook sikeres!");
        return true;
    }

    public boolean isHooked() { return hooked; }
}