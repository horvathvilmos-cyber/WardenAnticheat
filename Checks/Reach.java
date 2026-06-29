package hu.ClashRoyale456.wardenAnticheat.Checks;

import hu.ClashRoyale456.wardenAnticheat.WardenAnticheat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class Reach extends Check implements Listener {

    public Reach(WardenAnticheat plugin) {
        super(plugin, "Reach");
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onHit(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player)) return;
        if (!(e.getEntity() instanceof LivingEntity)) return;

        Player player = (Player) e.getDamager();
        if (player.hasPermission("warden.bypass")) return;

        Entity target = e.getEntity();

        double distance = player.getEyeLocation().distance(target.getLocation().add(0, target.getHeight() / 2, 0));
        double maxReach = plugin.getConfig().getDouble("Checks.Reach.max-reach", 3.1);

        if (distance > maxReach) {
            flag(player, "reach: " + String.format("%.2f", distance) + " / max: " + maxReach);
        }
    }
}