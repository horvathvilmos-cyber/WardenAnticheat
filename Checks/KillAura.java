package hu.ClashRoyale456.wardenAnticheat.Checks;

import hu.ClashRoyale456.wardenAnticheat.WardenAnticheat;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class KillAura extends Check implements Listener {

    public KillAura(WardenAnticheat plugin) {
        super(plugin, "KillAura");
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onHit(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player)) return;
        if (!(e.getEntity() instanceof Player)) return;

        Player attacker = (Player) e.getDamager();
        Player victim = (Player) e.getEntity();
        if (attacker.hasPermission("warden.bypass")) return;

        double yaw = Math.toRadians(attacker.getEyeLocation().getYaw());
        double dx = victim.getLocation().getX() - attacker.getLocation().getX();
        double dz = victim.getLocation().getZ() - attacker.getLocation().getZ();

        double angle = Math.toDegrees(Math.atan2(dz, dx));
        double playerAngle = Math.toDegrees(-yaw) - 90;
        double diff = Math.abs(angle - playerAngle) % 360;
        if (diff > 180) diff = 360 - diff;

        if (diff > 90) {
            flag(attacker, "angle: " + String.format("%.1f", diff) + "°");
        }
    }
}