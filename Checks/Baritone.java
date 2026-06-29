package hu.ClashRoyale456.wardenAnticheat.Checks;

import hu.ClashRoyale456.wardenAnticheat.Clients.ClientDetector;
import hu.ClashRoyale456.wardenAnticheat.WardenAnticheat;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.player.PlayerMoveEvent;
import java.util.*;

public class Baritone extends Check implements Listener {

    private final Map<UUID, List<Float>> yawHistory = new HashMap<>();
    private final Map<UUID, List<Float>> pitchHistory = new HashMap<>();
    private final Map<UUID, Integer> suspiciousCount = new HashMap<>();

    public Baritone(WardenAnticheat plugin) {
        super(plugin, "Baritone");
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onMove(PlayerMoveEvent e) {
        Player player = e.getPlayer();
        if (player.hasPermission("warden.bypass")) return;

        // If moving
        if (e.getFrom().getX() == e.getTo().getX()
                && e.getFrom().getZ() == e.getTo().getZ()) return;

        UUID uuid = player.getUniqueId();
        float yaw = e.getTo().getYaw();
        float pitch = e.getTo().getPitch();

        yawHistory.putIfAbsent(uuid, new ArrayList<>());
        pitchHistory.putIfAbsent(uuid, new ArrayList<>());

        List<Float> yaws = yawHistory.get(uuid);
        List<Float> pitches = pitchHistory.get(uuid);

        yaws.add(yaw);
        pitches.add(pitch);

        if (yaws.size() > 40) yaws.remove(0);
        if (pitches.size() > 40) pitches.remove(0);

        if (yaws.size() < 20) return;

        long snappedYaws = yaws.stream()
                .filter(y -> y % 45 == 0)
                .count();

        double pitchVariance = calculateVariance(pitches);

        String clientName = ClientDetector.getClientName(player).toLowerCase();
        boolean isFabric = clientName.contains("fabric") || clientName.contains("lunar");

        int count = suspiciousCount.getOrDefault(uuid, 0);

        if (snappedYaws > 15 && pitchVariance < 0.5) {
            count++;
            suspiciousCount.put(uuid, count);

            if (count >= 3) {
                flag(player, "snappedYaws: " + snappedYaws
                        + " pitchVariance: " + String.format("%.3f", pitchVariance)
                        + (isFabric ? " [Fabric]" : ""));
                suspiciousCount.put(uuid, 0);
            }
        } else {
            suspiciousCount.put(uuid, Math.max(0, count - 1));
        }
    }

    private double calculateVariance(List<Float> values) {
        double mean = values.stream().mapToDouble(Float::doubleValue).average().orElse(0);
        return values.stream()
                .mapToDouble(v -> Math.pow(v - mean, 2))
                .average()
                .orElse(0);
    }

    public void removePlayer(UUID uuid) {
        yawHistory.remove(uuid);
        pitchHistory.remove(uuid);
        suspiciousCount.remove(uuid);
    }
}