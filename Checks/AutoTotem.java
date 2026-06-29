package hu.ClashRoyale456.wardenAnticheat.Checks;

import hu.ClashRoyale456.wardenAnticheat.WardenAnticheat;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import java.util.*;

public class AutoTotem extends Check implements Listener {

    private final Map<UUID, Long> totemUsed = new HashMap<>();
    private final Map<UUID, Long> totemReequipped = new HashMap<>();

    public AutoTotem(WardenAnticheat plugin) {
        super(plugin, "AutoTotem");
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onTotemUse(EntityResurrectEvent e) {
        if (!(e.getEntity() instanceof Player)) return;
        Player player = (Player) e.getEntity();
        if (player.hasPermission("warden.bypass")) return;

        totemUsed.put(player.getUniqueId(), System.currentTimeMillis());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;
        Player player = (Player) e.getWhoClicked();
        if (player.hasPermission("warden.bypass")) return;

        UUID uuid = player.getUniqueId();
        if (!totemUsed.containsKey(uuid)) return;

        ItemStack current = e.getCurrentItem();
        ItemStack cursor = e.getCursor();

        boolean placedTotem = (current != null && current.getType() == Material.TOTEM_OF_UNDYING)
                || (cursor != null && cursor.getType() == Material.TOTEM_OF_UNDYING);

        if (!placedTotem) return;

        long timeSinceUse = System.currentTimeMillis() - totemUsed.get(uuid);
        totemReequipped.put(uuid, System.currentTimeMillis());

        if (timeSinceUse < 200) {
            flag(player, "reequip: " + timeSinceUse + "ms");
        }

        totemUsed.remove(uuid);
    }

    public void removePlayer(UUID uuid) {
        totemUsed.remove(uuid);
        totemReequipped.remove(uuid);
    }
}