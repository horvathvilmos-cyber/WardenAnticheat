package hu.ClashRoyale456.wardenAnticheat.Checks;

import hu.ClashRoyale456.wardenAnticheat.WardenAnticheat;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.block.BlockPlaceEvent;
import java.util.*;

public class Scaffold extends Check implements Listener {

    private final Map<UUID, Long> lastPlace = new HashMap<>();
    private final Map<UUID, Integer> suspiciousCount = new HashMap<>();

    public Scaffold(WardenAnticheat plugin) {
        super(plugin, "Scaffold");
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent e) {
        Player player = e.getPlayer();
        if (player.hasPermission("warden.bypass")) return;

        Block placed = e.getBlockPlaced();
        Location playerLoc = player.getLocation();
        UUID uuid = player.getUniqueId();
        long now = System.currentTimeMillis();

        boolean isUnderPlayer =
                placed.getLocation().getBlockX() == playerLoc.getBlockX() &&
                        placed.getLocation().getBlockZ() == playerLoc.getBlockZ() &&
                        placed.getLocation().getBlockY() == playerLoc.getBlockY() - 1;

        float pitch = playerLoc.getPitch();
        boolean lookingDown = pitch > 50;

        boolean isMoving = player.getVelocity().getX() != 0 || player.getVelocity().getZ() != 0;

        BlockFace face = e.getBlock().getFace(placed);
        boolean isSideOrBottom = face == BlockFace.NORTH || face == BlockFace.SOUTH
                || face == BlockFace.EAST || face == BlockFace.WEST
                || face == BlockFace.DOWN;

        long timeSinceLast = now - lastPlace.getOrDefault(uuid, 0L);
        boolean tooFast = timeSinceLast < 80;

        if (isUnderPlayer && lookingDown && isMoving && tooFast) {
            int count = suspiciousCount.getOrDefault(uuid, 0) + 1;
            suspiciousCount.put(uuid, count);

            if (count >= 3) {
                flag(player, "pitch: " + String.format("%.1f", pitch)
                        + " delay: " + timeSinceLast + "ms"
                        + " count: " + count);
                suspiciousCount.put(uuid, 0);
            }
        }
        else if (isUnderPlayer && tooFast && !lookingDown) {
            int count = suspiciousCount.getOrDefault(uuid, 0) + 1;
            suspiciousCount.put(uuid, count);

            if (count >= 5) {
                flag(player, "tower | delay: " + timeSinceLast + "ms count: " + count);
                suspiciousCount.put(uuid, 0);
            }
        } else {
            suspiciousCount.computeIfPresent(uuid, (k, v) -> Math.max(0, v - 1));
        }

        lastPlace.put(uuid, now);
    }

    public void removePlayer(UUID uuid) {
        lastPlace.remove(uuid);
        suspiciousCount.remove(uuid);
    }
}