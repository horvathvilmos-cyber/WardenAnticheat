package hu.ClashRoyale456.wardenAnticheat.Checks;

import hu.ClashRoyale456.wardenAnticheat.WardenAnticheat;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import java.util.*;

public class AutoClicker extends Check implements Listener {

    private final Map<UUID, List<Long>> clickTimes = new HashMap<>();

    public AutoClicker(WardenAnticheat plugin) {
        super(plugin, "AutoClicker");
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onClick(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player)) return;

        Player player = (Player) e.getDamager();
        if (player.hasPermission("warden.bypass")) return;

        UUID uuid = player.getUniqueId();
        long now = System.currentTimeMillis();

        clickTimes.putIfAbsent(uuid, new ArrayList<>());
        List<Long> clicks = clickTimes.get(uuid);

        clicks.removeIf(t -> now - t > 1000);
        clicks.add(now);

        int maxCps = plugin.getConfig().getInt("Checks.AutoClicker.max-cps", 20);
        int cps = clicks.size();

        if (cps > maxCps) {
            flag(player, "cps: " + cps + " / max: " + maxCps);
        }
    }

    public void removePlayer(UUID uuid) {
        clickTimes.remove(uuid);
    }
}