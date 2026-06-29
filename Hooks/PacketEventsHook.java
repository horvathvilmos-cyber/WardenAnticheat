package hu.ClashRoyale456.wardenAnticheat.Hooks;

import com.github.retrooper.packetevents.PacketEvents;
import hu.ClashRoyale456.wardenAnticheat.Clients.ClientDetector;
import hu.ClashRoyale456.wardenAnticheat.WardenAnticheat;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;

public class PacketEventsHook {

    private final WardenAnticheat plugin;

    public PacketEventsHook(WardenAnticheat plugin) {
        this.plugin = plugin;
    }

    public boolean setup() {
        try {
            PacketEvents.setAPI(SpigotPacketEventsBuilder.build(plugin));
            PacketEvents.getAPI().getSettings()
                    .reEncodeByDefault(false)
                    .checkForUpdates(false);
            PacketEvents.getAPI().load();
            PacketEvents.getAPI().getEventManager()
                    .registerListener(new ClientDetector(plugin));
            PacketEvents.getAPI().init();

            plugin.getLogger().info("[Warden] PacketEvents hook sikeres!");
            return true;
        } catch (Exception e) {
            plugin.getLogger().warning("[Warden] PacketEvents hook hiba: " + e.getMessage());
            return false;
        }
    }

    public void disable() {
        try {
            PacketEvents.getAPI().terminate();
        } catch (Exception e) {
            plugin.getLogger().warning("[Warden] PacketEvents leállítási hiba: " + e.getMessage());
        }
    }
}