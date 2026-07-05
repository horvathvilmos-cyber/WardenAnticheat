package hu.ClashRoyale456.wardenAnticheat.Commands.subcommands;

import hu.ClashRoyale456.wardenAnticheat.Clients.ClientDetector;
import hu.ClashRoyale456.wardenAnticheat.Commands.SubCommand;
import hu.ClashRoyale456.wardenAnticheat.Data.PlayerData;
import hu.ClashRoyale456.wardenAnticheat.Data.PlayerDataManager;
import hu.ClashRoyale456.wardenAnticheat.WardenAnticheat;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.*;
import java.util.stream.Collectors;

public class PlayerInfoSubCommand implements SubCommand {

    private final WardenAnticheat plugin;

    public PlayerInfoSubCommand(WardenAnticheat plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§c[Warden] Ezt csak játékos használhatja!");
            return;
        }

        if (args.length < 2) {
            sender.sendMessage("§cHasználat: " + getUsage());
            return;
        }

        Player viewer = (Player) sender;
        Player target = Bukkit.getPlayer(args[1]);

        if (target == null) {
            sender.sendMessage("§c[Warden] A játékos nem található vagy nincs online!");
            return;
        }

        openGUI(viewer, target);
    }

    private void openGUI(Player viewer, Player target) {
        PlayerData data = PlayerDataManager.getData(target);
        if (data == null) {
            viewer.sendMessage("§c[Warden] Nem található adat ehhez a játékoshoz!");
            return;
        }

        Inventory gui = Bukkit.createInventory(null, 54,
                "§4Warden §8» §f" + target.getName());

        // Keret
        fillBorder(gui);

        // Játékos feje
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
        skullMeta.setOwningPlayer(target);
        skullMeta.setDisplayName("§6§l" + target.getName());
        skullMeta.setLore(Arrays.asList(
                "§7UUID: §f" + target.getUniqueId(),
                "§7Gamemode: §f" + target.getGameMode(),
                "§7Ping: §f" + target.getPing() + "ms"
        ));
        skull.setItemMeta(skullMeta);
        gui.setItem(4, skull);

        // Client info
        long onlineMins = (System.currentTimeMillis() - data.getJoinTime()) / 60000;
        ItemStack clientItem = createItem(Material.COMPASS,
                "§b§lClient Info",
                Arrays.asList(
                        "§7Brand: §f" + ClientDetector.getClientName(target),
                        "§7Raw: §f" + ClientDetector.getRawBrand(target),
                        "§7Online: §f" + onlineMins + " perc"
                )
        );
        gui.setItem(20, clientItem);

        // Violations összesítő
        int totalVl = data.getTotalViolations();
        Material vlMaterial = totalVl == 0 ? Material.LIME_DYE
                : totalVl < 10 ? Material.YELLOW_DYE
                  : Material.RED_DYE;

        List<String> vlLore = new ArrayList<>();
        vlLore.add("§7Összes VL: §c" + totalVl);
        vlLore.add("§8§m--------------------");
        if (data.getAllViolations().isEmpty()) {
            vlLore.add("§7Nincs flag.");
        } else {
            data.getAllViolations().forEach((check, vl) ->
                    vlLore.add("§c" + check + ": §f" + vl + " VL"));
        }
        ItemStack vlItem = createItem(vlMaterial, "§c§lViolations", vlLore);
        gui.setItem(22, vlItem);

        // Szerver info
        ItemStack serverItem = createItem(Material.PAPER,
                "§a§lSzerver Info",
                Arrays.asList(
                        "§7World: §f" + target.getWorld().getName(),
                        "§7TPS: §f" + getTPS(),
                        "§7Health: §f" + String.format("%.1f", target.getHealth()) + "❤"
                )
        );
        gui.setItem(24, serverItem);

        // Violations részletes lista
        int slot = 28;
        for (Map.Entry<String, Integer> entry : data.getAllViolations().entrySet()) {
            if (slot > 43) break;

            long ago = (System.currentTimeMillis() - data.getLastFlagged(entry.getKey())) / 1000;
            Material mat = entry.getValue() >= 10 ? Material.RED_CONCRETE
                    : entry.getValue() >= 5 ? Material.ORANGE_CONCRETE
                      : Material.YELLOW_CONCRETE;

            ItemStack checkItem = createItem(mat,
                    "§e" + entry.getKey(),
                    Arrays.asList(
                            "§7VL: §c" + entry.getValue(),
                            "§7Utoljára: §f" + ago + " mp-je"
                    )
            );
            gui.setItem(slot, checkItem);
            slot++;
        }

        // Kick gomb
        gui.setItem(48, createItem(Material.BARRIER,
                "§c§lKirúgás",
                Collections.singletonList("§7Kattints a játékos kirúgásához!")
        ));

        // Violations reset gomb
        gui.setItem(49, createItem(Material.BUCKET,
                "§6§lViolations Reset",
                Collections.singletonList("§7Kattints a violations törléséhez!")
        ));

        // Bezárás gomb
        gui.setItem(50, createItem(Material.DARK_OAK_BUTTON,
                "§8Bezárás",
                Collections.singletonList("§7Kattints a bezáráshoz!")
        ));

        viewer.openInventory(gui);
    }

    private void fillBorder(Inventory gui) {
        ItemStack glass = createItem(Material.GRAY_STAINED_GLASS_PANE, " ", null);
        for (int i = 0; i < 9; i++) gui.setItem(i, glass);
        for (int i = 45; i < 54; i++) gui.setItem(i, glass);
        for (int i = 9; i < 45; i += 9) gui.setItem(i, glass);
        for (int i = 17; i < 45; i += 9) gui.setItem(i, glass);
    }

    private ItemStack createItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;
        meta.setDisplayName(name);
        if (lore != null) meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private String getTPS() {
        try {
            Object server = Bukkit.getServer().getClass()
                    .getMethod("getServer").invoke(Bukkit.getServer());
            double[] recentTps = (double[]) server.getClass()
                    .getField("recentTps").get(server);
            return String.format("%.1f", Math.min(20.0, recentTps[0]));
        } catch (Exception e) {
            return "20.0";
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 2)
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(n -> n.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        return Collections.emptyList();
    }

    @Override
    public String getPermission() { return "warden.playerinfo"; }
    @Override
    public String getUsage() { return "/warden playerinfo <játékos>"; }
    @Override
    public String getDescription() { return "Játékos adatainak megtekintése GUI-ban"; }
}