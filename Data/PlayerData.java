package hu.ClashRoyale456.wardenAnticheat.Data;

import org.bukkit.entity.Player;
import java.util.*;

public class PlayerData {

    private final Player player;
    private final Map<String, Integer> violations = new HashMap<>();
    private final Map<String, Long> lastFlagged = new HashMap<>();
    private final long joinTime;

    public PlayerData(Player player) {
        this.player = player;
        this.joinTime = System.currentTimeMillis();
    }

    public void addViolation(String checkName, int amount) {
        violations.merge(checkName, amount, Integer::sum);
        lastFlagged.put(checkName, System.currentTimeMillis());
    }

    public int getViolations(String checkName) {
        return violations.getOrDefault(checkName, 0);
    }

    public Map<String, Integer> getAllViolations() {
        return Collections.unmodifiableMap(violations);
    }

    public int getTotalViolations() {
        return violations.values().stream().mapToInt(Integer::intValue).sum();
    }

    public long getLastFlagged(String checkName) {
        return lastFlagged.getOrDefault(checkName, 0L);
    }

    public Player getPlayer() { return player; }
    public long getJoinTime() { return joinTime; }

    public void resetViolations() { violations.clear(); lastFlagged.clear(); }
    public void resetViolations(String checkName) {
        violations.remove(checkName);
        lastFlagged.remove(checkName);
    }
}