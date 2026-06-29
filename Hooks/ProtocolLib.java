package hu.ClashRoyale456.wardenAnticheat.Hooks;

import hu.ClashRoyale456.wardenAnticheat.WardenAnticheat;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public class ProtocolLib {

    private final WardenAnticheat plugin;
    private boolean hooked = false;

    public ProtocolLib(WardenAnticheat plugin) {
        this.plugin = plugin;
    }

    public boolean setup() {
        if (!plugin.getConfig().getBoolean("hooks.ProtocolLib.enableade", false)) return false;

        Plugin protocolLib = Bukkit.getPluginManager().getPlugin("ProtocolLib");
        if (protocolLib != null && protocolLib.isEnabled()) {
            hooked = true;
            plugin.getLogger().info("[Warden] ProtocolLib hook sikeres!");
            return true;
        }

        plugin.getLogger().warning("[Warden] ProtocolLib nem található!");
        return false;
    }

    public boolean isHooked() { return hooked; }
}