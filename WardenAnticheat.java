package hu.ClashRoyale456.wardenAnticheat;

import hu.ClashRoyale456.wardenAnticheat.Checks.*;
import hu.ClashRoyale456.wardenAnticheat.Commands.WardenCommand;
import hu.ClashRoyale456.wardenAnticheat.Database.MySQL;
import hu.ClashRoyale456.wardenAnticheat.Hooks.*;
import hu.ClashRoyale456.wardenAnticheat.Listeners.PlayerListener;
import hu.ClashRoyale456.wardenAnticheat.Velocity.VelocitySupport;
import org.bukkit.plugin.java.JavaPlugin;

public final class WardenAnticheat extends JavaPlugin {

    private static WardenAnticheat instance;

    // Check példányok
    private TriggerBot triggerBotCheck;
    private AutoClicker autoClickerCheck;
    private Timer timerCheck;
    private TimerLimit timerLimitCheck;
    private Baritone baritoneCheck;
    private Scaffold scaffoldCheck;
    private AutoTotem autoTotemCheck;
    private Flight flightCheck;
    private Speed speedCheck;
    private Reach reachCheck;
    private KillAura killAuraCheck;

    // Hook példányok
    private Discord discordHook;
    private GrimAC grimACHook;
    private Vulcan vulcanHook;
    private PacketEventsHook packetEventsHook;
    private OCM ocmHook;
    private ProtocolLib protocolLibHook;
    private PlaceholderAPIHook placeholderAPIHook;

    // Database és Proxy
    private MySQL mySQL;
    private VelocitySupport velocitySupport;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        // MySQL
        mySQL = new MySQL(this);
        if (getConfig().getBoolean("MySQL.enabled", false)) {
            mySQL.connect();
        }

        // Hookok
        packetEventsHook = new PacketEventsHook(this);
        packetEventsHook.setup();

        discordHook = new Discord(this);
        grimACHook = new GrimAC(this);
        grimACHook.setup();
        vulcanHook = new Vulcan(this);
        vulcanHook.setup();
        ocmHook = new OCM(this);
        ocmHook.setup();
        protocolLibHook = new ProtocolLib(this);
        protocolLibHook.setup();
        placeholderAPIHook = new PlaceholderAPIHook(this);
        placeholderAPIHook.setup();

        // Velocity
        velocitySupport = new VelocitySupport(this);
        velocitySupport.setup();

        // Commands
        WardenCommand handler = new WardenCommand(this);
        getCommand("warden").setExecutor(handler);
        getCommand("warden").setTabCompleter(handler);

        // Check példányok létrehozása
        reachCheck       = new Reach(this);
        speedCheck       = new Speed(this);
        flightCheck      = new Flight(this);
        killAuraCheck    = new KillAura(this);
        autoClickerCheck = new AutoClicker(this);
        timerCheck       = new Timer(this);
        scaffoldCheck    = new Scaffold(this);
        baritoneCheck    = new Baritone(this);
        autoTotemCheck   = new AutoTotem(this);
        timerLimitCheck  = new TimerLimit(this);
        triggerBotCheck  = new TriggerBot(this);

        // Checks regisztrálása
        getServer().getPluginManager().registerEvents(reachCheck, this);
        getServer().getPluginManager().registerEvents(speedCheck, this);
        getServer().getPluginManager().registerEvents(flightCheck, this);
        getServer().getPluginManager().registerEvents(killAuraCheck, this);
        getServer().getPluginManager().registerEvents(autoClickerCheck, this);
        getServer().getPluginManager().registerEvents(timerCheck, this);
        getServer().getPluginManager().registerEvents(scaffoldCheck, this);
        getServer().getPluginManager().registerEvents(baritoneCheck, this);
        getServer().getPluginManager().registerEvents(autoTotemCheck, this);
        getServer().getPluginManager().registerEvents(timerLimitCheck, this);
        getServer().getPluginManager().registerEvents(triggerBotCheck, this);

        // Listener
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);

        getLogger().info("The WardenAC is now protecting your server!");
    }

    @Override
    public void onDisable() {
        if (packetEventsHook != null) packetEventsHook.disable();
        if (mySQL != null && mySQL.isConnected()) mySQL.disconnect();
        if (velocitySupport != null) velocitySupport.disable();
        getLogger().info("WardenAC leállítva.");
    }

    // Getterek
    public static WardenAnticheat getInstance() { return instance; }
    public MySQL getMySQL() { return mySQL; }
    public VelocitySupport getVelocitySupport() { return velocitySupport; }
    public Discord getDiscordHook() { return discordHook; }
    public GrimAC getGrimACHook() { return grimACHook; }
    public Vulcan getVulcanHook() { return vulcanHook; }
    public OCM getOcmHook() { return ocmHook; }
    public ProtocolLib getProtocolLibHook() { return protocolLibHook; }
    public PlaceholderAPIHook getPlaceholderAPIHook() { return placeholderAPIHook; }
    public TriggerBot getTriggerBotCheck() { return triggerBotCheck; }
    public AutoClicker getAutoClickerCheck() { return autoClickerCheck; }
    public Timer getTimerCheck() { return timerCheck; }
    public TimerLimit getTimerLimitCheck() { return timerLimitCheck; }
    public Baritone getBaritoneCheck() { return baritoneCheck; }
    public Scaffold getScaffoldCheck() { return scaffoldCheck; }
    public AutoTotem getAutoTotemCheck() { return autoTotemCheck; }
    public Flight getFlightCheck() { return flightCheck; }
    public Speed getSpeedCheck() { return speedCheck; }
    public Reach getReachCheck() { return reachCheck; }
    public KillAura getKillAuraCheck() { return killAuraCheck; }
}