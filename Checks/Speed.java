package hu.ClashRoyale456.wardenAnticheat.Checks;

import hu.ClashRoyale456.wardenAnticheat.WardenAnticheat;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffectType;

public class Speed extends Check implements Listener {

    private static final double MAX_SPEED = 0.35;

    public Speed(WardenAnticheat plugin) {
        super(plugin, "Speed");
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onMove(PlayerMoveEvent e) {
        Player player = e.getPlayer();
        if (player.hasPermission("warden.bypass")) return;
        if (player.isFlying() || player.isInsideVehicle()) return;
        if (player.hasPotionEffect(PotionEffectType.SPEED)) return;

        double dx = e.getTo().getX() - e.getFrom().getX();
        double dz = e.getTo().getZ() - e.getFrom().getZ();
        double speed = Math.sqrt(dx * dx + dz * dz);

        if (speed > MAX_SPEED) {
            flag(player, "speed: " + String.format("%.3f", speed) + " / max: " + MAX_SPEED);
        }
    }
}