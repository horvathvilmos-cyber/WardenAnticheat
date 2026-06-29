package hu.ClashRoyale456.wardenAnticheat.Checks;

import hu.ClashRoyale456.wardenAnticheat.WardenAnticheat;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.player.PlayerMoveEvent;
import java.util.*;

public class Timer extends Check implements Listener {

    private final Map<UUID, Long> lastMove = new HashMap<>();
    private final Map<UUID, Integer> tickCount = new HashMap<>();

    public Timer(WardenAnticheat plugin) {
        super(plugin, "Timer");
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onMove(PlayerMoveEvent e) {
        Player player = e.getPlayer();
        if (player.hasPermission("warden.bypass")) return;

        UUID uuid = player.getUniqueId();
        long now = System.currentTimeMillis();

        if (lastMove.containsKey(uuid)) {
            long diff = now - lastMove.get(uuid);

            if (diff < 40) {
                int count = tickCount.getOrDefault(uuid, 0) + 1;
                tickCount.put(uuid, count);

                if (count > 5) {
                    flag(player, "tick: " + diff + "ms");
                    tickCount.put(uuid, 0);
                }
            } else {
                tickCount.put(uuid, 0);
            }
        }

        lastMove.put(uuid, now);
    }

    public void removePlayer(UUID uuid) {
        lastMove.remove(uuid);
        tickCount.remove(uuid);
    }
}