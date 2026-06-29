package hu.ClashRoyale456.wardenAnticheat.Checks;

import hu.ClashRoyale456.wardenAnticheat.WardenAnticheat;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.player.PlayerMoveEvent;
import java.util.*;

public class TimerLimit extends Check implements Listener {

    private final Map<UUID, List<Long>> moveTimes = new HashMap<>();
    private final Map<UUID, Long> lastViolation = new HashMap<>();

    public TimerLimit(WardenAnticheat plugin) {
        super(plugin, "TimerLimit");
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onMove(PlayerMoveEvent e) {
        Player player = e.getPlayer();
        if (player.hasPermission("warden.bypass")) return;

        if (e.getFrom().getX() == e.getTo().getX()
                && e.getFrom().getY() == e.getTo().getY()
                && e.getFrom().getZ() == e.getTo().getZ()) return;

        UUID uuid = player.getUniqueId();
        long now = System.currentTimeMillis();

        moveTimes.putIfAbsent(uuid, new ArrayList<>());
        List<Long> times = moveTimes.get(uuid);

        times.removeIf(t -> now - t > 1000);
        times.add(now);

        double tps = Math.min(20.0, getTPS());
        double tpsRatio = tps / 20.0;
        int maxMoves = (int) (20 * tpsRatio) + 4; // alap 20/s + 4 buffer

        if (times.size() > maxMoves) {
            // Cooldown hogy ne spammeljen
            long lastVl = lastViolation.getOrDefault(uuid, 0L);
            if (now - lastVl < 500) return;

            flag(player, "moves/s: " + times.size()
                    + " max: " + maxMoves
                    + " tps: " + String.format("%.1f", tps));

            lastViolation.put(uuid, now);
            times.clear();
        }
    }

    private double getTPS() {
        try {
            Object server = Bukkit.getServer().getClass().getMethod("getServer").invoke(Bukkit.getServer());
            double[] recentTps = (double[]) server.getClass().getField("recentTps").get(server);
            return recentTps[0];
        } catch (Exception e) {
            return 20.0;
        }
    }

    public void removePlayer(UUID uuid) {
        moveTimes.remove(uuid);
        lastViolation.remove(uuid);
    }
}