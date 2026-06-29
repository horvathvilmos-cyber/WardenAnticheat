package hu.ClashRoyale456.wardenAnticheat.Data;

import org.bukkit.entity.Player;
import java.util.*;

public class PlayerDataManager {

    private static final Map<UUID, PlayerData> dataMap = new HashMap<>();

    public static void addPlayer(Player player) {
        dataMap.put(player.getUniqueId(), new PlayerData(player));
    }

    public static void removePlayer(Player player) {
        dataMap.remove(player.getUniqueId());
    }

    public static PlayerData getData(Player player) {
        return dataMap.get(player.getUniqueId());
    }

    public static boolean hasData(Player player) {
        return dataMap.containsKey(player.getUniqueId());
    }
}