package hu.ClashRoyale456.wardenAnticheat.Hooks;

import hu.ClashRoyale456.wardenAnticheat.WardenAnticheat;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public class GrimAC {

    private final WardenAnticheat plugin;
    private boolean hooked = false;

    public GrimAC(WardenAnticheat plugin) {
        this.plugin = plugin;
    }

    public boolean setup() {
        if (!plugin.getConfig().getBoolean("hooks.GrimAC.enableade", false)) return false;

        Plugin grim = Bukkit.getPluginManager().getPlugin("GrimAC");
        if (grim != null && grim.isEnabled()) {
            hooked = true;
            plugin.getLogger().info("[Warden] GrimAC hook sikeres!");
            return true;
        }

        plugin.getLogger().warning("[Warden] GrimAC nem található!");
        return false;
    }

    public boolean isHooked() { return hooked; }
}