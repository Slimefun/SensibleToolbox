package io.github.thebusybiscuit.sensibletoolbox;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import org.apache.commons.lang.Validate;
import org.bstats.bukkit.Metrics;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import com.comphenix.protocol.ProtocolLib;

import io.github.thebusybiscuit.sensibletoolbox.api.AccessControl;
import io.github.thebusybiscuit.sensibletoolbox.api.FriendManager;
import io.github.thebusybiscuit.sensibletoolbox.api.MinecraftVersion;
import io.github.thebusybiscuit.sensibletoolbox.api.RedstoneBehaviour;
import io.github.thebusybiscuit.sensibletoolbox.api.gui.InventoryGUI;
import io.github.thebusybiscuit.sensibletoolbox.api.recipes.RecipeUtil;
import io.github.thebusybiscuit.sensibletoolbox.blocks.AngelicBlock;
import io.github.thebusybiscuit.sensibletoolbox.blocks.BlockUpdateDetector;
import io.github.thebusybiscuit.sensibletoolbox.blocks.Elevator;
import io.github.thebusybiscuit.sensibletoolbox.blocks.EnderBox;
import io.github.thebusybiscuit.sensibletoolbox.blocks.PaintCan;
import io.github.thebusybiscuit.sensibletoolbox.blocks.RedstoneClock;
import io.github.thebusybiscuit.sensibletoolbox.blocks.SoundMuffler;
import io.github.thebusybiscuit.sensibletoolbox.blocks.TrashCan;
import io.github.thebusybiscuit.sensibletoolbox.blocks.machines.AutoBuilder;
import io.github.thebusybiscuit.sensibletoolbox.blocks.machines.AutoFarm;
import io.github.thebusybiscuit.sensibletoolbox.blocks.machines.AutoFarm2;
import io.github.thebusybiscuit.sensibletoolbox.blocks.machines.AutoForester;
import io.github.thebusybiscuit.sensibletoolbox.blocks.machines.BasicSolarCell;
import io.github.thebusybiscuit.sensibletoolbox.blocks.machines.BigStorageUnit;
import io.github.thebusybiscuit.sensibletoolbox.blocks.machines.BioEngine;
import io.github.thebusybiscuit.sensibletoolbox.blocks.machines.DenseSolar;
import io.github.thebusybiscuit.sensibletoolbox.blocks.machines.ElectricalEnergizer;
import io.github.thebusybiscuit.sensibletoolbox.blocks.machines.Fermenter;
import io.github.thebusybiscuit.sensibletoolbox.blocks.machines.FiftyKBatteryBox;
import io.github.thebusybiscuit.sensibletoolbox.blocks.machines.FishingNet;
import io.github.thebusybiscuit.sensibletoolbox.blocks.machines.HeatEngine;
import io.github.thebusybiscuit.sensibletoolbox.blocks.machines.HolographicMonitor;
import io.github.thebusybiscuit.sensibletoolbox.blocks.machines.HyperStorageUnit;
import io.github.thebusybiscuit.sensibletoolbox.blocks.machines.InfernalFarm;
import io.github.thebusybiscuit.sensibletoolbox.blocks.machines.MagmaticEngine;
import io.github.thebusybiscuit.sensibletoolbox.blocks.machines.Masher;
import io.github.thebusybiscuit.sensibletoolbox.blocks.machines.PowerMonitor;
import io.github.thebusybiscuit.sensibletoolbox.blocks.machines.Pump;
import io.github.thebusybiscuit.sensibletoolbox.blocks.machines.SCURelay;
import io.github.thebusybiscuit.sensibletoolbox.blocks.machines.Sawmill;
import io.github.thebusybiscuit.sensibletoolbox.blocks.machines.Smelter;
import io.github.thebusybiscuit.sensibletoolbox.blocks.machines.TenKBatteryBox;
import io.github.thebusybiscuit.sensibletoolbox.blocks.router.ItemRouter;
import io.github.thebusybiscuit.sensibletoolbox.commands.ChargeCommand;
import io.github.thebusybiscuit.sensibletoolbox.commands.DebugCommand;
import io.github.thebusybiscuit.sensibletoolbox.commands.ExamineCommand;
import io.github.thebusybiscuit.sensibletoolbox.commands.FriendCommand;
import io.github.thebusybiscuit.sensibletoolbox.commands.GetcfgCommand;
import io.github.thebusybiscuit.sensibletoolbox.commands.GiveCommand;
import io.github.thebusybiscuit.sensibletoolbox.commands.RecipeCommand;
import io.github.thebusybiscuit.sensibletoolbox.commands.RedrawCommand;
import io.github.thebusybiscuit.sensibletoolbox.commands.SaveCommand;
import io.github.thebusybiscuit.sensibletoolbox.commands.SetcfgCommand;
import io.github.thebusybiscuit.sensibletoolbox.commands.ShowCommand;
import io.github.thebusybiscuit.sensibletoolbox.commands.SoundCommand;
import io.github.thebusybiscuit.sensibletoolbox.commands.UnfriendCommand;
import io.github.thebusybiscuit.sensibletoolbox.commands.ValidateCommand;
import io.github.thebusybiscuit.sensibletoolbox.core.IDTracker;
import io.github.thebusybiscuit.sensibletoolbox.core.STBFriendManager;
import io.github.thebusybiscuit.sensibletoolbox.core.STBItemRegistry;
import io.github.thebusybiscuit.sensibletoolbox.core.enderstorage.EnderStorageManager;
import io.github.thebusybiscuit.sensibletoolbox.core.energy.EnergyNetManager;
import io.github.thebusybiscuit.sensibletoolbox.core.energy.SCURelayConnection;
import io.github.thebusybiscuit.sensibletoolbox.core.gui.STBInventoryGUI;
import io.github.thebusybiscuit.sensibletoolbox.core.storage.LocationManager;
import io.github.thebusybiscuit.sensibletoolbox.items.AdvancedMoistureChecker;
import io.github.thebusybiscuit.sensibletoolbox.items.DiamondCombineHoe;
import io.github.thebusybiscuit.sensibletoolbox.items.EnderBag;
import io.github.thebusybiscuit.sensibletoolbox.items.EnderLeash;
import io.github.thebusybiscuit.sensibletoolbox.items.EnderTuner;
import io.github.thebusybiscuit.sensibletoolbox.items.GoldCombineHoe;
import io.github.thebusybiscuit.sensibletoolbox.items.IronCombineHoe;
import io.github.thebusybiscuit.sensibletoolbox.items.LandMarker;
import io.github.thebusybiscuit.sensibletoolbox.items.MoistureChecker;
import io.github.thebusybiscuit.sensibletoolbox.items.Multimeter;
import io.github.thebusybiscuit.sensibletoolbox.items.PVCell;
import io.github.thebusybiscuit.sensibletoolbox.items.PaintBrush;
import io.github.thebusybiscuit.sensibletoolbox.items.PaintRoller;
import io.github.thebusybiscuit.sensibletoolbox.items.TapeMeasure;
import io.github.thebusybiscuit.sensibletoolbox.items.WateringCan;
import io.github.thebusybiscuit.sensibletoolbox.items.WoodCombineHoe;
import io.github.thebusybiscuit.sensibletoolbox.items.components.CircuitBoard;
import io.github.thebusybiscuit.sensibletoolbox.items.components.EnergizedGoldDust;
import io.github.thebusybiscuit.sensibletoolbox.items.components.EnergizedGoldIngot;
import io.github.thebusybiscuit.sensibletoolbox.items.components.EnergizedIronDust;
import io.github.thebusybiscuit.sensibletoolbox.items.components.EnergizedIronIngot;
import io.github.thebusybiscuit.sensibletoolbox.items.components.EnergizedQuartz;
import io.github.thebusybiscuit.sensibletoolbox.items.components.FishBait;
import io.github.thebusybiscuit.sensibletoolbox.items.components.GoldDust;
import io.github.thebusybiscuit.sensibletoolbox.items.components.InfernalDust;
import io.github.thebusybiscuit.sensibletoolbox.items.components.IntegratedCircuit;
import io.github.thebusybiscuit.sensibletoolbox.items.components.IronDust;
import io.github.thebusybiscuit.sensibletoolbox.items.components.MachineFrame;
import io.github.thebusybiscuit.sensibletoolbox.items.components.QuartzDust;
import io.github.thebusybiscuit.sensibletoolbox.items.components.SiliconWafer;
import io.github.thebusybiscuit.sensibletoolbox.items.components.SimpleCircuit;
import io.github.thebusybiscuit.sensibletoolbox.items.components.SubspaceTransponder;
import io.github.thebusybiscuit.sensibletoolbox.items.components.ToughMachineFrame;
import io.github.thebusybiscuit.sensibletoolbox.items.components.UnlinkedSCURelay;
import io.github.thebusybiscuit.sensibletoolbox.items.energycells.FiftyKEnergyCell;
import io.github.thebusybiscuit.sensibletoolbox.items.energycells.TenKEnergyCell;
import io.github.thebusybiscuit.sensibletoolbox.items.itemroutermodules.AdvancedSenderModule;
import io.github.thebusybiscuit.sensibletoolbox.items.itemroutermodules.BlankModule;
import io.github.thebusybiscuit.sensibletoolbox.items.itemroutermodules.BreakerModule;
import io.github.thebusybiscuit.sensibletoolbox.items.itemroutermodules.DistributorModule;
import io.github.thebusybiscuit.sensibletoolbox.items.itemroutermodules.DropperModule;
import io.github.thebusybiscuit.sensibletoolbox.items.itemroutermodules.HyperSenderModule;
import io.github.thebusybiscuit.sensibletoolbox.items.itemroutermodules.PullerModule;
import io.github.thebusybiscuit.sensibletoolbox.items.itemroutermodules.ReceiverModule;
import io.github.thebusybiscuit.sensibletoolbox.items.itemroutermodules.SenderModule;
import io.github.thebusybiscuit.sensibletoolbox.items.itemroutermodules.SilkyBreakerModule;
import io.github.thebusybiscuit.sensibletoolbox.items.itemroutermodules.SorterModule;
import io.github.thebusybiscuit.sensibletoolbox.items.itemroutermodules.SpeedModule;
import io.github.thebusybiscuit.sensibletoolbox.items.itemroutermodules.StackModule;
import io.github.thebusybiscuit.sensibletoolbox.items.itemroutermodules.VacuumModule;
import io.github.thebusybiscuit.sensibletoolbox.items.multibuilder.MultiBuilder;
import io.github.thebusybiscuit.sensibletoolbox.items.recipebook.AdvancedRecipeBook;
import io.github.thebusybiscuit.sensibletoolbox.items.recipebook.RecipeBook;
import io.github.thebusybiscuit.sensibletoolbox.items.upgrades.EjectorUpgrade;
import io.github.thebusybiscuit.sensibletoolbox.items.upgrades.RegulatorUpgrade;
import io.github.thebusybiscuit.sensibletoolbox.items.upgrades.SpeedUpgrade;
import io.github.thebusybiscuit.sensibletoolbox.items.upgrades.ThoroughnessUpgrade;
import io.github.thebusybiscuit.sensibletoolbox.listeners.AnvilListener;
import io.github.thebusybiscuit.sensibletoolbox.listeners.ElevatorListener;
import io.github.thebusybiscuit.sensibletoolbox.listeners.FurnaceListener;
import io.github.thebusybiscuit.sensibletoolbox.listeners.GeneralListener;
import io.github.thebusybiscuit.sensibletoolbox.listeners.MobListener;
import io.github.thebusybiscuit.sensibletoolbox.listeners.SoundMufflerListener;
import io.github.thebusybiscuit.sensibletoolbox.listeners.TrashCanListener;
import io.github.thebusybiscuit.sensibletoolbox.listeners.WorldListener;
import io.github.thebusybiscuit.sensibletoolbox.slimefun.SlimefunBridge;
import io.github.thebusybiscuit.sensibletoolbox.utils.ItemGlow;
import io.github.thebusybiscuit.sensibletoolbox.utils.STBUtil;
import io.github.thebusybiscuit.slimefun4.libraries.dough.protection.ProtectionManager;
import io.github.thebusybiscuit.slimefun4.libraries.dough.updater.GitHubBuildsUpdater;
import io.github.thebusybiscuit.slimefun4.libraries.dough.updater.PluginUpdater;
import io.github.thebusybiscuit.slimefun4.libraries.dough.versions.PrefixedVersion;
import io.papermc.lib.PaperLib;

import me.desht.dhutils.DHUtilsException;
import me.desht.dhutils.Debugger;
import me.desht.dhutils.MiscUtil;
import me.desht.dhutils.commands.CommandManager;
import me.desht.dhutils.configuration.ConfigurationListener;
import me.desht.dhutils.configuration.ConfigurationManager;
import me.desht.dhutils.text.LogUtils;
import me.desht.dhutils.text.MessagePager;

public class SensibleToolboxPlugin extends JavaPlugin implements ConfigurationListener {

    private static SensibleToolboxPlugin instance = null;
    private final CommandManager commandManager = new CommandManager(this);
    private MinecraftVersion minecraftVersion = MinecraftVersion.UNKNOWN;

    private ConfigurationManager configManager;
    private boolean slimefunEnabled = false;
    private boolean protocolLibEnabled = false;
    private SoundMufflerListener soundMufflerListener;
    private boolean enabled = false;
    private boolean holographicDisplays = false;
    private BukkitTask energyTask = null;
    private EnderStorageManager enderStorageManager;
    private STBItemRegistry itemRegistry;
    private STBFriendManager friendManager;
    private EnergyNetManager enetManager;
    private ConfigCache configCache;
    private IDTracker<SCURelayConnection> scuRelayIDTracker;
    private ProtectionManager protectionManager;

    @Override
    public void onEnable() {
        instance = this;

        // We wanna ensure that the Server uses a compatible version of Minecraft
        if (isVersionUnsupported()) {
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        LogUtils.init(this);
        new Metrics(this, 6354);

        configManager = new ConfigurationManager(this, this);
        configCache = new ConfigCache(this);
        configCache.processConfig();

        MiscUtil.setColoredConsole(getConfig().getBoolean("colored_console"));

        LogUtils.setLogLevel(getConfig().getString("log_level", "INFO"));

        Debugger.getInstance().setPrefix("[STB] ");
        Debugger.getInstance().setLevel(getConfig().getInt("debug_level"));

        if (getConfig().getInt("debug_level") > 0) {
            Debugger.getInstance().setTarget(getServer().getConsoleSender());
        }

        // try to hook other plugins
        holographicDisplays = getServer().getPluginManager().isPluginEnabled("HolographicDisplays");
        setupProtocolLib();

        scuRelayIDTracker = new IDTracker<>(this, "scu_relay_id");

        STBInventoryGUI.buildStockTextures();

        itemRegistry = new STBItemRegistry(this, "item_data");
        registerItems();

        friendManager = new STBFriendManager(this);
        enetManager = new EnergyNetManager(this);

        registerEventListeners();
        registerCommands();

        try {
            LocationManager.getManager().load();
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "An Error occured while loading Locations...", e);
            setEnabled(false);
            return;
        }

        MessagePager.setPageCmd("/stb page [#|n|p]");
        MessagePager.setDefaultPageSize(getConfig().getInt("pager.lines", 0));

        // do all the recipe setup on a delayed task to ensure we pick up
        // custom recipes from any plugins that may have loaded after us
        getServer().getScheduler().runTask(this, () -> {
            RecipeUtil.findVanillaFurnaceMaterials();
            RecipeUtil.setupRecipes();
            RecipeBook.buildRecipes();

            protectionManager = new ProtectionManager(getServer());
        });

        getServer().getScheduler().runTaskTimer(this, LocationManager.getManager()::tick, 1L, 1L);
        getServer().getScheduler().runTaskTimer(this, getEnderStorageManager()::tick, 1L, 300L);
        getServer().getScheduler().runTaskTimer(this, friendManager::save, 60L, 300L);

        scheduleEnergyNetTicker();

        if (getServer().getPluginManager().isPluginEnabled("Slimefun")) {
            slimefunEnabled = true;
            new SlimefunBridge(this);
        }

        if (getConfig().getBoolean("options.auto-update") && getDescription().getVersion().startsWith("DEV - ")) {
            PluginUpdater<PrefixedVersion> updater = new GitHubBuildsUpdater(this, getFile(), "Slimefun/SensibleToolbox/master");
            updater.start();
        }

        enabled = true;
    }

    /**
     * This method checks for the {@link MinecraftVersion} of the {@link Server}.
     * If the version is unsupported, a warning will be printed to the console.
     *
     * @return Whether the {@link MinecraftVersion} is unsupported
     */
    private boolean isVersionUnsupported() {
        int majorVersion = PaperLib.getMinecraftVersion();

        if (majorVersion > 0) {
            for (MinecraftVersion supportedVersion : MinecraftVersion.values()) {
                if (supportedVersion.isMinecraftVersion(majorVersion)) {
                    minecraftVersion = supportedVersion;
                    return false;
                }
            }

            // Looks like you are using an unsupported Minecraft Version
            getLogger().log(Level.SEVERE, "#############################################");
            getLogger().log(Level.SEVERE, "### SensibleToolbox was not installed correctly!");
            getLogger().log(Level.SEVERE, "### You are using the wrong version of Minecraft!");
            getLogger().log(Level.SEVERE, "###");
            getLogger().log(Level.SEVERE, "### You are using Minecraft v1.{0}", majorVersion);
            getLogger().log(Level.SEVERE, "### but SensibleToolbox v{0} requires you to be using", getDescription().getVersion());
            getLogger().log(Level.SEVERE, "### Minecraft {0}", String.join(" / ", getSupportedVersions()));
            getLogger().log(Level.SEVERE, "#############################################");
            return true;
        }

        getLogger().log(Level.WARNING, "We could not determine the version of Minecraft you were using (1.{0})", majorVersion);
        return false;
    }

    private @Nonnull Collection<String> getSupportedVersions() {
        List<String> list = new ArrayList<>();

        for (MinecraftVersion version : MinecraftVersion.values()) {
            if (version != MinecraftVersion.UNKNOWN) {
                list.add(version.getName());
            }
        }

        return list;
    }

    @Override
    public void onDisable() {
        if (!enabled) {
            return;
        }

        for (Player p : getServer().getOnlinePlayers()) {
            // Any open inventory GUI's must be closed -
            // if they stay open after server reload, event dispatch will probably not work,
            // allowing fake items to be removed from them - not a good thing
            InventoryGUI gui = STBInventoryGUI.getOpenGUI(p);

            if (gui != null) {
                gui.hide(p);
                p.closeInventory();
            }
        }

        if (soundMufflerListener != null) {
            soundMufflerListener.clear();
        }

        LocationManager.getManager().save();
        LocationManager.getManager().shutdown();

        friendManager.save();

        getServer().getScheduler().cancelTasks(this);

        instance = null;
    }

    public void registerItems() {
        String configPrefix = "items_enabled";
        String permissionNode = "stb";

        itemRegistry.registerItem(new AngelicBlock(), this, configPrefix, permissionNode);
        itemRegistry.registerItem(new EnderLeash(), this, configPrefix, permissionNode);
        itemRegistry.registerItem(new RedstoneClock(), this, configPrefix, permissionNode);
        itemRegistry.registerItem(new BlockUpdateDetector(), this, configPrefix, permissionNode);
        itemRegistry.registerItem(new EnderBag(), this, configPrefix, permissionNode);
        itemRegistry.registerItem(new WateringCan(), this, configPrefix, permissionNode);
        itemRegistry.registerItem(new MoistureChecker(), this, configPrefix, permissionNode);
        itemRegistry.registerItem(new AdvancedMoistureChecker(), this, configPrefix, permissionNode);
        itemRegistry.registerItem(new WoodCombineHoe(), this, configPrefix, permissionNode);
        itemRegistry.registerItem(new IronCombineHoe(), this, configPrefix, permissionNode);
        itemRegistry.registerItem(new GoldCombineHoe(), this, configPrefix, permissionNode);
        itemRegistry.registerItem(new DiamondCombineHoe(), this, configPrefix, permissionNode);
        itemRegistry.registerItem(new TrashCan(), this, configPrefix, permissionNode);
        itemRegistry.registerItem(new PaintBrush(), this, configPrefix, permissionNode);
        itemRegistry.registerItem(new PaintRoller(), this, configPrefix, permissionNode);
        itemRegistry.registerItem(new PaintCan(), this, configPrefix, permissionNode);
        itemRegistry.registerItem(new Elevator(), this, configPrefix, permissionNode);
        itemRegistry.registerItem(new TapeMeasure(), this, configPrefix, permissionNode);
        itemRegistry.registerItem(new CircuitBoard(), this, configPrefix, permissionNode);
        itemRegistry.registerItem(new SimpleCircuit(), this, configPrefix, permissionNode);
        itemRegistry.registerItem(new MultiBuilder(), this, configPrefix, permissionNode);
        itemRegistry.registerItem(new MachineFrame(), this, configPrefix, permissionNode);
        itemRegistry.registerItem(new Smelter(), this, configPrefix, permissionNode);
        itemRegistry.registerItem(new Masher(), this, configPrefix, permissionNode);
        itemRegistry.registerItem(new Sawmill(), this, configPrefix, permissionNode);
        itemRegistry.registerItem(new IronDust(), this, configPrefix, permissionNode);
        itemRegistry.registerItem(new GoldDust(), this, configPrefix, permissionNode);
        itemRegistry.registerItem(new ItemRouter(), this, configPrefix, permissionNode);
        itemRegistry.registerItem(new BlankModule(), this, configPrefix, permissionNode);
        itemRegistry.registerItem(new PullerModule(), this, configPrefix, permissionNode);
        itemRegistry.registerItem(new DropperModule(), this, configPrefix, permissionNode);
        itemRegistry.registerItem(new SenderModule(), this, configPrefix, permissionNode);
        itemRegistry.registerItem(new DistributorModule(), this, configPrefix, permissionNode);
        itemRegistry.registerItem(new AdvancedSenderModule(), this, configPrefix, permissionNode);
        itemRegistry.registerItem(new HyperSenderModule(), this, configPrefix, permissionNode);
        itemRegistry.registerItem(new ReceiverModule(), this, configPrefix, permissionNode);
        itemRegistry.registerItem(new SorterModule(), this, configPrefix, permissionNode);
        itemRegistry.registerItem(new VacuumModule(), this, configPrefix, permissionNode);
        itemRegistry.registerItem(new BreakerModule(), this, configPrefix, permissionNode);
        itemRegistry.registerItem(new StackModule(), this, configPrefix, permissionNode);
        itemRegistry.registerItem(new SpeedModule(), this, configPrefix, permissionNode);
        itemRegistry.registerItem(new TenKEnergyCell(), this, configPrefix, permissionNode);
        itemRegistry.registerItem(new FiftyKEnergyCell(), this, configPrefix, permissionNode);
        itemRegistry.registerItem(new TenKBatteryBox(), this, configPrefix, permissionNode);
        itemRegistry.registerItem(new FiftyKBatteryBox(), this, configPrefix, permissionNode);
        itemRegistry.registerItem(new SpeedUpgrade(), this, configPrefix, permissionNode);
        itemRegistry.registerItem(new EjectorUpgrade(), this, configPrefix, permissionNode);
        itemRegistry.registerItem(new RegulatorUpgrade(), this, configPrefix, permissionNode);
        itemRegistry.registerItem(new ThoroughnessUpgrade(), this, configPrefix, permissionNode);
        itemRegistry.registerItem(new HeatEngine(), this, configPrefix, permissionNode);
        itemRegistry.registerItem(new BasicSolarCell(), this, configPrefix, permissionNode);
        itemRegistry.registerItem(new DenseSolar(), this, configPrefix, permissionNode);
        itemRegistry.registerItem(new RecipeBook(), this, configPrefix, permissionNode);
        itemRegistry.registerItem(new AdvancedRecipeBook(), this, configPrefix, permissionNode);
        itemRegistry.registerItem(new Multimeter(), this, configPrefix, permissionNode);
        itemRegistry.registerItem(new BigStorageUnit(), this, configPrefix, permissionNode);
        itemRegistry.registerItem(new HyperStorageUnit(), this, configPrefix, permissionNode);
        itemRegistry.registerItem(new Pump(), this, configPrefix, permissionNode);
        itemRegistry.registerItem(new EnderTuner(), this, configPrefix, permissionNode);
        itemRegistry.registerItem(new EnderBox(), this, configPrefix, permissionNode);
        itemRegistry.registerItem(new InfernalDust(), this, configPrefix, permissionNode);
        itemRegistry.registerItem(new EnergizedIronDust(), this, configPrefix, permissionNode);
        itemRegistry.registerItem(new EnergizedGoldDust(), this, configPrefix, permissionNode);
        itemRegistry.registerItem(new EnergizedIronIngot(), this, configPrefix, permissionNode);
        itemRegistry.registerItem(new EnergizedGoldIngot(), this, configPrefix, permissionNode);
        itemRegistry.registerItem(new ToughMachineFrame(), this, configPrefix, permissionNode);
        itemRegistry.registerItem(new QuartzDust(), this, configPrefix, permissionNode);
        itemRegistry.registerItem(new SiliconWafer(), this, configPrefix, permissionNode);
        itemRegistry.registerItem(new IntegratedCircuit(), this, configPrefix, permissionNode);
        itemRegistry.registerItem(new LandMarker(), this, configPrefix, permissionNode);
        itemRegistry.registerItem(new PVCell(), this, configPrefix, permissionNode);
        itemRegistry.registerItem(new AutoBuilder(), this, configPrefix, permissionNode);
        itemRegistry.registerItem(new UnlinkedSCURelay(), this, configPrefix, permissionNode);
        itemRegistry.registerItem(new SCURelay(), this, configPrefix, permissionNode);
        itemRegistry.registerItem(new SilkyBreakerModule(), this, configPrefix, permissionNode);
        itemRegistry.registerItem(new SubspaceTransponder(), this, configPrefix, permissionNode);
        itemRegistry.registerItem(new BioEngine(), this, configPrefix, permissionNode);
        itemRegistry.registerItem(new MagmaticEngine(), this, configPrefix, permissionNode);
        itemRegistry.registerItem(new EnergizedQuartz(), this, configPrefix, permissionNode);
        itemRegistry.registerItem(new ElectricalEnergizer(), this, configPrefix, permissionNode);
        itemRegistry.registerItem(new PowerMonitor(), this, configPrefix, permissionNode);
        itemRegistry.registerItem(new Fermenter(), this, configPrefix, permissionNode);
        itemRegistry.registerItem(new FishBait(), this, configPrefix, permissionNode);
        itemRegistry.registerItem(new FishingNet(), this, configPrefix, permissionNode);
        itemRegistry.registerItem(new AutoFarm(), this, configPrefix, permissionNode);
        itemRegistry.registerItem(new AutoForester(), this, configPrefix, permissionNode);
        itemRegistry.registerItem(new InfernalFarm(), this, configPrefix, permissionNode);
        itemRegistry.registerItem(new AutoFarm2(), this, configPrefix, permissionNode);

        if (isProtocolLibEnabled()) {
            itemRegistry.registerItem(new SoundMuffler(), this, configPrefix, permissionNode);
        }

        if (isHolographicDisplaysEnabled()) {
            itemRegistry.registerItem(new HolographicMonitor(), this, configPrefix, permissionNode);
        }
    }

    private void registerEventListeners() {
        PluginManager pm = this.getServer().getPluginManager();
        pm.registerEvents(new GeneralListener(this), this);
        pm.registerEvents(new FurnaceListener(this), this);
        pm.registerEvents(new MobListener(this), this);
        pm.registerEvents(new WorldListener(this), this);
        pm.registerEvents(new TrashCanListener(this), this);
        pm.registerEvents(new ElevatorListener(this), this);
        pm.registerEvents(new AnvilListener(this), this);

        if (isProtocolLibEnabled()) {
            soundMufflerListener = new SoundMufflerListener(this);
            soundMufflerListener.start();
        }

        enderStorageManager = new EnderStorageManager(this);
        pm.registerEvents(enderStorageManager, this);
    }

    private void setupProtocolLib() {
        Plugin pLib = getServer().getPluginManager().getPlugin("ProtocolLib");

        if (pLib instanceof ProtocolLib && pLib.isEnabled()) {
            protocolLibEnabled = true;
            Debugger.getInstance().debug("Hooked ProtocolLib v" + pLib.getDescription().getVersion());
        }

        if (protocolLibEnabled) {
            if (getConfig().getBoolean("options.glowing_items")) {
                ItemGlow.init(this);
            }
        } else {
            LogUtils.warning("ProtocolLib not detected - some functionality is reduced:");
            LogUtils.warning("  No glowing items, Reduced particle effects, Sound Muffler item disabled");
        }
    }

    public boolean isSlimefunEnabled() {
        return slimefunEnabled;
    }

    public boolean isProtocolLibEnabled() {
        return protocolLibEnabled;
    }

    public boolean isHolographicDisplaysEnabled() {
        return holographicDisplays;
    }

    private void registerCommands() {
        commandManager.registerCommand(new SaveCommand());
        commandManager.registerCommand(new GiveCommand());
        commandManager.registerCommand(new ShowCommand());
        commandManager.registerCommand(new ChargeCommand());
        commandManager.registerCommand(new GetcfgCommand());
        commandManager.registerCommand(new SetcfgCommand());
        commandManager.registerCommand(new DebugCommand());
        commandManager.registerCommand(new SoundCommand());
        commandManager.registerCommand(new RecipeCommand());
        commandManager.registerCommand(new ExamineCommand());
        commandManager.registerCommand(new RedrawCommand());
        commandManager.registerCommand(new FriendCommand());
        commandManager.registerCommand(new UnfriendCommand());
        commandManager.registerCommand(new ValidateCommand());
    }

    /**
     * This returns the main instance of the {@link SensibleToolboxPlugin}.
     * 
     * @return Our instance of {@link SensibleToolboxPlugin}
     */
    public static SensibleToolboxPlugin getInstance() {
        return instance;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        try {
            return commandManager.dispatch(sender, command, label, args);
        } catch (DHUtilsException e) {
            MiscUtil.errorMessage(sender, e.getMessage());
            return true;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        return commandManager.onTabComplete(sender, command, label, args);
    }

    @Override
    public <T> T onConfigurationValidate(ConfigurationManager configurationManager, String key, T oldVal, T newVal) {
        if (key.equals("save_interval")) {
            Validate.isTrue((Integer) newVal > 0, "save_interval must be > 0");
        } else if (key.equals("energy.tick_rate")) {
            Validate.isTrue((Integer) newVal > 0, "energy.tick_rate must be > 0");
        } else if (key.startsWith("gui.texture.")) {
            STBUtil.parseMaterialSpec(newVal.toString());
        } else if (key.equals("default_access")) {
            getEnumValue(newVal.toString().toUpperCase(Locale.ROOT), AccessControl.class);
        } else if (key.equals("default_redstone")) {
            getEnumValue(newVal.toString().toUpperCase(Locale.ROOT), RedstoneBehaviour.class);
        }
        return newVal;
    }

    @ParametersAreNonnullByDefault
    private <T extends Enum<T>> T getEnumValue(String value, Class<T> c) {
        try {
            return Enum.valueOf(c, value);
        } catch (IllegalArgumentException x) {
            throw new DHUtilsException("Unknown value for Type (" + c.getSimpleName() + "): " + value);
        }
    }

    @Override
    public <T> void onConfigurationChanged(ConfigurationManager configurationManager, String key, T oldVal, T newVal) {
        if (key.equals("debug_level")) {
            Debugger dbg = Debugger.getInstance();
            dbg.setLevel((Integer) newVal);

            if (dbg.getLevel() > 0) {
                dbg.setTarget(getServer().getConsoleSender());
            } else {
                dbg.setTarget(null);
            }
        } else if (key.equals("save_interval")) {
            LocationManager.getManager().setSaveInterval((Integer) newVal);
        } else if (key.equals("energy.tick_rate")) {
            scheduleEnergyNetTicker();
        } else if (key.startsWith("gui.texture.")) {
            STBInventoryGUI.buildStockTextures();
        } else if (key.equals("default_access")) {
            getConfigCache().setDefaultAccess(AccessControl.valueOf(newVal.toString().toUpperCase(Locale.ROOT)));
        } else if (key.equals("default_redstone")) {
            getConfigCache().setDefaultRedstone(RedstoneBehaviour.valueOf(newVal.toString().toUpperCase(Locale.ROOT)));
        } else if (key.equals("particle_effects")) {
            getConfigCache().setParticleLevel((Integer) newVal);
        } else if (key.equals("noisy_machines")) {
            getConfigCache().setNoisyMachines((Boolean) newVal);
        } else if (key.equals("creative_ender_access")) {
            getConfigCache().setCreativeEnderAccess((Boolean) newVal);
        }
    }

    private void scheduleEnergyNetTicker() {
        if (energyTask != null) {
            energyTask.cancel();
        }

        enetManager.setTickRate(getConfig().getLong("energy.tick_rate", EnergyNetManager.DEFAULT_TICK_RATE));
        energyTask = getServer().getScheduler().runTaskTimer(this, enetManager::tick, 1L, enetManager.getTickRate());
    }

    public ConfigurationManager getConfigManager() {
        return configManager;
    }

    public EnderStorageManager getEnderStorageManager() {
        return enderStorageManager;
    }

    public STBItemRegistry getItemRegistry() {
        return itemRegistry;
    }

    public FriendManager getFriendManager() {
        return friendManager;
    }

    public EnergyNetManager getEnergyNetManager() {
        return enetManager;
    }

    public ConfigCache getConfigCache() {
        return configCache;
    }

    public IDTracker<SCURelayConnection> getScuRelayIDTracker() {
        return scuRelayIDTracker;
    }

    public SoundMufflerListener getSoundMufflerListener() {
        return soundMufflerListener;
    }

    public ProtectionManager getProtectionManager() {
        return protectionManager;
    }

    /**
     * This returns the currently installed version of Minecraft.
     *
     * @return The current version of Minecraft
     */
    public static MinecraftVersion getMinecraftVersion() {
        return instance.minecraftVersion;
    }

    public boolean isGlowingEnabled() {
        return isProtocolLibEnabled() && getConfig().getBoolean("options.glowing_items");
    }
}
