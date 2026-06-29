package hu.ClashRoyale456.wardenAnticheat.Database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import hu.ClashRoyale456.wardenAnticheat.WardenAnticheat;
import org.bukkit.entity.Player;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MySQL {

    private final WardenAnticheat plugin;
    private HikariDataSource dataSource;
    private String tablePrefix;

    public MySQL(WardenAnticheat plugin) {
        this.plugin = plugin;
        this.tablePrefix = plugin.getConfig().getString("MySQL.table-prefix", "warden_");
    }

    public boolean connect() {
        try {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl("jdbc:mysql://"
                    + plugin.getConfig().getString("MySQL.host", "localhost")
                    + ":" + plugin.getConfig().getInt("MySQL.port", 3306)
                    + "/" + plugin.getConfig().getString("MySQL.database", "warden")
                    + "?useSSL=false&autoReconnect=true&characterEncoding=utf8");
            config.setUsername(plugin.getConfig().getString("MySQL.username", "root"));
            config.setPassword(plugin.getConfig().getString("MySQL.password", ""));
            config.setMaximumPoolSize(plugin.getConfig().getInt("MySQL.pool-size", 10));
            config.setMinimumIdle(2);
            config.setConnectionTimeout(30000);
            config.setIdleTimeout(600000);
            config.setMaxLifetime(1800000);
            config.setPoolName("WardenPool");

            // Driver
            config.setDriverClassName("com.mysql.cj.jdbc.Driver");

            dataSource = new HikariDataSource(config);

            createTables();
            plugin.getLogger().info("[Warden] MySQL kapcsolat sikeres!");
            return true;
        } catch (Exception e) {
            plugin.getLogger().severe("[Warden] MySQL kapcsolat sikertelen: " + e.getMessage());
            return false;
        }
    }

    private void createTables() throws SQLException {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            // Játékosok tábla
            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS " + tablePrefix + "players (" +
                            "uuid VARCHAR(36) NOT NULL PRIMARY KEY," +
                            "name VARCHAR(16) NOT NULL," +
                            "client_brand VARCHAR(64) DEFAULT 'Unknown'," +
                            "first_join BIGINT NOT NULL," +
                            "last_join BIGINT NOT NULL" +
                            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;"
            );

            // Violations tábla
            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS " + tablePrefix + "violations (" +
                            "id INT AUTO_INCREMENT PRIMARY KEY," +
                            "uuid VARCHAR(36) NOT NULL," +
                            "check_name VARCHAR(32) NOT NULL," +
                            "violations INT DEFAULT 0," +
                            "last_flagged BIGINT NOT NULL," +
                            "detail VARCHAR(255)," +
                            "FOREIGN KEY (uuid) REFERENCES " + tablePrefix + "players(uuid)" +
                            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;"
            );

            // Punishments tábla
            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS " + tablePrefix + "punishments (" +
                            "id INT AUTO_INCREMENT PRIMARY KEY," +
                            "uuid VARCHAR(36) NOT NULL," +
                            "type VARCHAR(16) NOT NULL," +
                            "reason VARCHAR(255)," +
                            "check_name VARCHAR(32)," +
                            "timestamp BIGINT NOT NULL," +
                            "FOREIGN KEY (uuid) REFERENCES " + tablePrefix + "players(uuid)" +
                            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;"
            );
        }
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public boolean isConnected() {
        return dataSource != null && !dataSource.isClosed();
    }

    public void disconnect() {
        if (isConnected()) {
            dataSource.close();
            plugin.getLogger().info("[Warden] MySQL kapcsolat lezárva.");
        }
    }

    // Játékos mentése/frissítése
    public void savePlayer(Player player, String clientBrand) {
        if (!isConnected()) return;

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection conn = getConnection()) {
                PreparedStatement stmt = conn.prepareStatement(
                        "INSERT INTO " + tablePrefix + "players (uuid, name, client_brand, first_join, last_join) " +
                                "VALUES (?, ?, ?, ?, ?) " +
                                "ON DUPLICATE KEY UPDATE name=?, client_brand=?, last_join=?"
                );
                long now = System.currentTimeMillis();
                stmt.setString(1, player.getUniqueId().toString());
                stmt.setString(2, player.getName());
                stmt.setString(3, clientBrand);
                stmt.setLong(4, now);
                stmt.setLong(5, now);
                stmt.setString(6, player.getName());
                stmt.setString(7, clientBrand);
                stmt.setLong(8, now);
                stmt.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().warning("[Warden] MySQL mentési hiba: " + e.getMessage());
            }
        });
    }

    // Violation mentése
    public void saveViolation(UUID uuid, String checkName, int vl, String detail) {
        if (!isConnected()) return;

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection conn = getConnection()) {
                PreparedStatement stmt = conn.prepareStatement(
                        "INSERT INTO " + tablePrefix + "violations (uuid, check_name, violations, last_flagged, detail) " +
                                "VALUES (?, ?, ?, ?, ?) " +
                                "ON DUPLICATE KEY UPDATE violations=?, last_flagged=?, detail=?"
                );
                long now = System.currentTimeMillis();
                stmt.setString(1, uuid.toString());
                stmt.setString(2, checkName);
                stmt.setInt(3, vl);
                stmt.setLong(4, now);
                stmt.setString(5, detail);
                stmt.setInt(6, vl);
                stmt.setLong(7, now);
                stmt.setString(8, detail);
                stmt.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().warning("[Warden] MySQL violation mentési hiba: " + e.getMessage());
            }
        });
    }

    // Punishment mentése
    public void savePunishment(UUID uuid, String type, String reason, String checkName) {
        if (!isConnected()) return;

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection conn = getConnection()) {
                PreparedStatement stmt = conn.prepareStatement(
                        "INSERT INTO " + tablePrefix + "punishments (uuid, type, reason, check_name, timestamp) " +
                                "VALUES (?, ?, ?, ?, ?)"
                );
                stmt.setString(1, uuid.toString());
                stmt.setString(2, type);
                stmt.setString(3, reason);
                stmt.setString(4, checkName);
                stmt.setLong(5, System.currentTimeMillis());
                stmt.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().warning("[Warden] MySQL punishment mentési hiba: " + e.getMessage());
            }
        });
    }

    // Violations lekérése offline játékoshoz
    public Map<String, Integer> getViolations(UUID uuid) {
        Map<String, Integer> violations = new HashMap<>();
        if (!isConnected()) return violations;

        try (Connection conn = getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT check_name, violations FROM " + tablePrefix + "violations WHERE uuid=?"
            );
            stmt.setString(1, uuid.toString());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                violations.put(rs.getString("check_name"), rs.getInt("violations"));
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("[Warden] MySQL lekérési hiba: " + e.getMessage());
        }
        return violations;
    }
}