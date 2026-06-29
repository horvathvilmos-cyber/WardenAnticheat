package hu.ClashRoyale456.wardenAnticheat.Hooks;

import hu.ClashRoyale456.wardenAnticheat.WardenAnticheat;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public class OCM {

    private final WardenAnticheat plugin;
    private boolean hooked = false;
    private int maxCps = -1;

    public OCM(WardenAnticheat plugin) {
        this.plugin = plugin;
    }

    public boolean setup() {
        if (!plugin.getConfig().getBoolean("hooks.OldCombatMechanics.enableade", false)) return false;

        Plugin ocm = Bukkit.getPluginManager().getPlugin("OldCombatMechanics");
        if (ocm != null && ocm.isEnabled()) {
            hooked = true;
            maxCps = plugin.getConfig().getInt("hooks.OldCombatMechanics.max-cps", -1);
            plugin.getLogger().info("[Warden] OldCombatMechanics hook sikeres! max-cps: " + maxCps);
            return true;
        }

        plugin.getLogger().warning("[Warden] OldCombatMechanics nem található!");
        return false;
    }

    public boolean isHooked() { return hooked; }

    public int getMaxCps() { return maxCps; }
}