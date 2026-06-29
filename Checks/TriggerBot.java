package hu.ClashRoyale456.wardenAnticheat.Checks;

import hu.ClashRoyale456.wardenAnticheat.WardenAnticheat;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.util.RayTraceResult;

import java.util.*;

public class TriggerBot extends Check implements Listener {

    private final Map<UUID, List<Long>> hitTimes = new HashMap<>();
    private final Map<UUID, List<Long>> reactionTimes = new HashMap<>();
    private final Map<UUID, Long> entityOnCrosshair = new HashMap<>();

    public TriggerBot(WardenAnticheat plugin) {
        super(plugin, "TriggerBot");
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onHit(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player)) return;
        if (!(e.getEntity() instanceof LivingEntity)) return;

        Player player = (Player) e.getDamager();
        if (player.hasPermission("warden.bypass")) return;

        UUID uuid = player.getUniqueId();
        long now = System.currentTimeMillis();

        hitTimes.putIfAbsent(uuid, new ArrayList<>());
        List<Long> hits = hitTimes.get(uuid);
        hits.removeIf(t -> now - t > 5000);
        hits.add(now);

        if (entityOnCrosshair.containsKey(uuid)) {
            long crosshairTime = entityOnCrosshair.get(uuid);
            long reactionTime = now - crosshairTime;

            reactionTimes.putIfAbsent(uuid, new ArrayList<>());
            List<Long> reactions = reactionTimes.get(uuid);
            reactions.removeIf(t -> now - t > 10000);
            reactions.add(reactionTime);

            if (reactions.size() >= 5) {
                long avgReaction = (long) reactions.stream()
                        .mapToLong(Long::longValue)
                        .average()
                        .orElse(0);

                double variance = calculateVariance(reactions);

                if (avgReaction < 150 && variance < 20 && reactionTime < 150) {
                    flag(player, "reactionAvg: " + avgReaction
                            + "ms variance: " + String.format("%.1f", variance)
                            + " current: " + reactionTime + "ms");
                }
            }

            entityOnCrosshair.remove(uuid);
        }

        if (hits.size() >= 6) {
            List<Long> intervals = new ArrayList<>();
            for (int i = 1; i < hits.size(); i++) {
                intervals.add(hits.get(i) - hits.get(i - 1));
            }

            double variance = calculateVariance(intervals);
            long avgInterval = (long) intervals.stream()
                    .mapToLong(Long::longValue)
                    .average()
                    .orElse(0);

            if (variance < 15 && avgInterval < 200 && avgInterval > 0) {
                flag(player, "pattern | avgInterval: " + avgInterval
                        + "ms variance: " + String.format("%.1f", variance));
                hits.clear();
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onMove(org.bukkit.event.player.PlayerMoveEvent e) {
        Player player = e.getPlayer();
        if (player.hasPermission("warden.bypass")) return;

        RayTraceResult result = player.getWorld().rayTraceEntities(
                player.getEyeLocation(),
                player.getEyeLocation().getDirection(),
                6.0,
                entity -> entity != player && entity instanceof LivingEntity
        );

        if (result != null && result.getHitEntity() != null) {
            entityOnCrosshair.putIfAbsent(player.getUniqueId(), System.currentTimeMillis());
        } else {
            entityOnCrosshair.remove(player.getUniqueId());
        }
    }

    private double calculateVariance(List<Long> values) {
        if (values.isEmpty()) return 0;
        double mean = values.stream().mapToLong(Long::longValue).average().orElse(0);
        return values.stream()
                .mapToDouble(v -> Math.pow(v - mean, 2))
                .average()
                .orElse(0);
    }

    public void removePlayer(UUID uuid) {
        hitTimes.remove(uuid);
        reactionTimes.remove(uuid);
        entityOnCrosshair.remove(uuid);
    }
}