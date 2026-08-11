package hu.ClashRoyale456.wardenAnticheat.Checks;

import hu.ClashRoyale456.wardenAnticheat.Clients.ClientDetector;
import hu.ClashRoyale456.wardenAnticheat.WardenAnticheat;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class AutoTotem extends Check implements Listener {

    private final Map<UUID, Long> totemPopTime = new HashMap<>();
    private final Map<UUID, List<Long>> reequipTimes = new HashMap<>();
    private final Map<UUID, List<Double>> stdDevHistory = new HashMap<>();
    private final Map<UUID, Boolean> inventoryOpen = new HashMap<>();
    private final Map<UUID, Long> lastSlotChange = new HashMap<>();
    private final Map<UUID, String> lastSlotPacket = new HashMap<>();
    private final Map<UUID, Integer> duplicatePacketCount = new HashMap<>();

    public AutoTotem(WardenAnticheat plugin) {
        super(plugin, "AutoTotem");
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onTotemPop(EntityResurrectEvent e) {
        if (!(e.getEntity() instanceof Player)) return;
        Player player = (Player) e.getEntity();
        if (player.hasPermission("warden.bypass")) return;

        totemPopTime.put(player.getUniqueId(), System.currentTimeMillis());
    }


    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;
        Player player = (Player) e.getWhoClicked();
        if (player.hasPermission("warden.bypass")) return;

        UUID uuid = player.getUniqueId();
        if (!totemPopTime.containsKey(uuid)) return;

        ItemStack current = e.getCurrentItem();
        ItemStack cursor = e.getCursor();

        boolean isTotem = (current != null && current.getType() == Material.TOTEM_OF_UNDYING)
                || (cursor != null && cursor.getType() == Material.TOTEM_OF_UNDYING);

        boolean isOffhand = e.getSlotType() == InventoryType.SlotType.QUICKBAR
                || e.getRawSlot() == 45;

        if (!isTotem) return;

        long popTime = totemPopTime.get(uuid);
        long now = System.currentTimeMillis();
        long reequipTime = now - popTime;

        String rawBrand = ClientDetector.getRawBrand(player).toLowerCase();
        if (rawBrand.contains("autototem")) {
            flag(player, "BadPackets-B | brand: " + rawBrand);
            return;
        }

        // --- AutoTotem Type A: túl gyors reequip (<100ms) ---
        if (reequipTime < 100) {
            flag(player, "Type-A | reequip: " + reequipTime + "ms");
            totemPopTime.remove(uuid);
            return;
        }

        reequipTimes.putIfAbsent(uuid, new ArrayList<>());
        List<Long> times = reequipTimes.get(uuid);
        times.add(reequipTime);
        if (times.size() > 10) times.remove(0);

        if (times.size() >= 5) {
            double stdDev = calculateStdDev(times);

            if (stdDev < 15 && reequipTime < 300) {
                flag(player, "Type-B | stdDev: " + String.format("%.2f", stdDev)
                        + " avg: " + String.format("%.1f", calculateMean(times)) + "ms");
            }
            stdDevHistory.putIfAbsent(uuid, new ArrayList<>());
            List<Double> stdDevs = stdDevHistory.get(uuid);
            stdDevs.add(stdDev);
            if (stdDevs.size() > 5) stdDevs.remove(0);

            if (stdDevs.size() >= 4) {
                double stdDevOfStdDevs = calculateStdDevDouble(stdDevs);
                if (stdDevOfStdDevs < 5 && stdDev < 20) {
                    flag(player, "Type-C | stdDev of stdDevs: "
                            + String.format("%.2f", stdDevOfStdDevs));
                }
            }
        }

        if (lastSlotChange.containsKey(uuid)) {
            long slotDiff = now - lastSlotChange.get(uuid);
            if (slotDiff > 0 && slotDiff < 50 && reequipTime < 150) {
                flag(player, "Type-D | slotDiff: " + slotDiff
                        + "ms reequip: " + reequipTime + "ms");
            }
        }
        lastSlotChange.put(uuid, now);

        if (times.size() >= 6) {
            long min = times.stream().mapToLong(Long::longValue).min().orElse(0);
            double mean = calculateMean(times);
            if (min < mean * 0.3 && min < 120) {
                flag(player, "Type-E | outlier: " + min
                        + "ms mean: " + String.format("%.1f", mean) + "ms");
            }
        }

        if (Boolean.TRUE.equals(inventoryOpen.get(uuid)) && isOffhand) {
            flag(player, "Type-F | inventory-open reequip: " + reequipTime + "ms");
        }

        totemPopTime.remove(uuid);
    }

    private double calculateMean(List<Long> values) {
        return values.stream().mapToLong(Long::longValue).average().orElse(0);
    }

    private double calculateStdDev(List<Long> values) {
        double mean = calculateMean(values);
        return Math.sqrt(values.stream()
                .mapToDouble(v -> Math.pow(v - mean, 2))
                .average()
                .orElse(0));
    }

    private double calculateStdDevDouble(List<Double> values) {
        double mean = values.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        return Math.sqrt(values.stream()
                .mapToDouble(v -> Math.pow(v - mean, 2))
                .average()
                .orElse(0));
    }

    public void removePlayer(UUID uuid) {
        totemPopTime.remove(uuid);
        reequipTimes.remove(uuid);
        stdDevHistory.remove(uuid);
        inventoryOpen.remove(uuid);
        lastSlotChange.remove(uuid);
        lastSlotPacket.remove(uuid);
        duplicatePacketCount.remove(uuid);
    }
}