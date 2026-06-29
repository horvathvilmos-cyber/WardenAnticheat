package hu.ClashRoyale456.wardenAnticheat.Checks;

import hu.ClashRoyale456.wardenAnticheat.WardenAnticheat;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffectType;
import java.util.*;

public class Flight extends Check implements Listener {

    private final Map<UUID, Integer> airTicks = new HashMap<>();
    private final Map<UUID, Double> lastY = new HashMap<>();
    private final Map<UUID, Long> lastDamage = new HashMap<>();
    private final Map<UUID, Long> lastLaunchpad = new HashMap<>();
    private final Map<UUID, Double> maxUpwardVelocity = new HashMap<>();

    public Flight(WardenAnticheat plugin) {
        super(plugin, "Flight");
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDamage(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player)) return;
        Player player = (Player) e.getEntity();
        lastDamage.put(player.getUniqueId(), System.currentTimeMillis());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onMove(PlayerMoveEvent e) {
        Player player = e.getPlayer();
        if (player.hasPermission("warden.bypass")) return;
        if (player.getGameMode() == GameMode.CREATIVE) return;
        if (player.getGameMode() == GameMode.SPECTATOR) return;
        if (player.isFlying() || player.getAllowFlight()) return;
        if (player.isInsideVehicle()) return;
        if (player.hasPotionEffect(PotionEffectType.LEVITATION)) return;
        if (player.hasPotionEffect(PotionEffectType.JUMP_BOOST)) return;

        UUID uuid = player.getUniqueId();
        long now = System.currentTimeMillis();
        Location to = e.getTo();
        Location from = e.getFrom();
        double dy = to.getY() - from.getY();

        if (now - lastDamage.getOrDefault(uuid, 0L) < 2000) return;

        double velocityY = player.getVelocity().getY();
        if (velocityY > 0.8) {
            lastLaunchpad.put(uuid, now);
            maxUpwardVelocity.put(uuid, velocityY);
        }

        if (now - lastLaunchpad.getOrDefault(uuid, 0L) < 4000) {
            airTicks.put(uuid, 0);
            lastY.put(uuid, to.getY());
            return;
        }

        if (!player.isOnGround()) {
            int ticks = airTicks.getOrDefault(uuid, 0) + 1;
            airTicks.put(uuid, ticks);

            double prevY = lastY.getOrDefault(uuid, from.getY());
            double currentY = to.getY();

            if (ticks > 25 && Math.abs(dy) < 0.015) {
                flag(player, "hover | airTicks: " + ticks
                        + " dy: " + String.format("%.4f", dy));
            }

            if (ticks > 10 && dy > 0.08 && ticks > 15) {
                flag(player, "ascent | airTicks: " + ticks
                        + " dy: " + String.format("%.4f", dy));
            }

            if (ticks > 60) {
                flag(player, "nofall | airTicks: " + ticks);
                airTicks.put(uuid, 30);
            }

            lastY.put(uuid, currentY);
        } else {
            airTicks.put(uuid, 0);
            lastY.put(uuid, to.getY());
        }
    }

    public void removePlayer(UUID uuid) {
        airTicks.remove(uuid);
        lastY.remove(uuid);
        lastDamage.remove(uuid);
        lastLaunchpad.remove(uuid);
        maxUpwardVelocity.remove(uuid);
    }
}