package hu.ClashRoyale456.wardenAnticheat.Hooks;

import hu.ClashRoyale456.wardenAnticheat.WardenAnticheat;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public class Vulcan {

    private final WardenAnticheat plugin;
    private boolean hooked = false;

    public Vulcan(WardenAnticheat plugin) {
        this.plugin = plugin;
    }

    public boolean setup() {
        if (!plugin.getConfig().getBoolean("hooks.Vulcan.enableade", false)) return false;

        Plugin vulcan = Bukkit.getPluginManager().getPlugin("Vulcan");
        if (vulcan != null && vulcan.isEnabled()) {
            hooked = true;
            plugin.getLogger().info("[Warden] Vulcan hook sikeres!");
            return true;
        }

        plugin.getLogger().warning("[Warden] Vulcan nem található!");
        return false;
    }

    public boolean isHooked() { return hooked; }
}