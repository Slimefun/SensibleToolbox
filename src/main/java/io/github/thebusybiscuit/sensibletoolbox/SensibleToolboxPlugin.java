package io.github.thebusybiscuit.sensibletoolbox;

/*
 * This file is part of SensibleToolbox
 * 
 * SensibleToolbox is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * SensibleToolbox is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with SensibleToolbox. If not, see <http://www.gnu.org/licenses/>.
 */

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.logging.Level;

import org.apache.commons.lang.Validate;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import com.comphenix.protocol.ProtocolLibrary;

import io.github.thebusybiscuit.cscorelib2.protection.ProtectionManager;
import io.github.thebusybiscuit.cscorelib2.updater.GitHubBuildsUpdater;
import io.github.thebusybiscuit.cscorelib2.updater.Updater;
import io.github.thebusybiscuit.sensibletoolbox.api.AccessControl;
import io.github.thebusybiscuit.sensibletoolbox.api.FriendManager;
import io.github.thebusybiscuit.sensibletoolbox.api.RedstoneBehaviour;
import io.github.thebusybiscuit.sensibletoolbox.api.gui.InventoryGUI;
import io.github.thebusybiscuit.sensibletoolbox.api.recipes.RecipeUtil;
import io.github.thebusybiscuit.sensibletoolbox.blocks.AngelicBlock;
import io.github.thebusybiscuit.sensibletoolbox.blocks.BlockUpdateDetector;
import io.github.thebusybiscuit.sensibletoolbox.blocks.Elevator;
import io.github.thebusybiscuit.sensibletoolbox.blocks.EnderBox;
import io.github.thebusybiscuit.sensibletoolbox.blocks.ItemRouter;
import io.github.thebusybiscuit.sensibletoolbox.blocks.PaintCan;
import io.github.thebusybiscuit.sensibletoolbox.blocks.RedstoneClock;
import io.github.thebusybiscuit.sensibletoolbox.blocks.SoundMuffler;
import io.github.thebusybiscuit.sensibletoolbox.blocks.TrashCan;
import io.github.thebusybiscuit.sensibletoolbox.blocks.machines.AdvancedFarm;
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
import io.github.thebusybiscuit.sensibletoolbox.commands.ChargeCommand;
import io.github.thebusybiscuit.sensibletoolbox.commands.DebugCommand;
import io.github.thebusybiscuit.sensibletoolbox.commands.ExamineCommand;
import io.github.thebusybiscuit.sensibletoolbox.commands.FriendCommand;
import io.github.thebusybiscuit.sensibletoolbox.commands.GetcfgCommand;
import io.github.thebusybiscuit.sensibletoolbox.commands.GiveCommand;
import io.github.thebusybiscuit.sensibletoolbox.commands.RecipeCommand;
import io.github.thebusybiscuit.sensibletoolbox.commands.RedrawCommand;
import io.github.thebusybiscuit.sensibletoolbox.commands.RenameCommand;
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
import io.github.thebusybiscuit.sensibletoolbox.core.gui.STBInventoryGUI;
import io.github.thebusybiscuit.sensibletoolbox.core.storage.LocationManager;
import io.github.thebusybiscuit.sensibletoolbox.items.AdvancedMoistureChecker;
import io.github.thebusybiscuit.sensibletoolbox.items.AdvancedRecipeBook;
import io.github.thebusybiscuit.sensibletoolbox.items.DiamondCombineHoe;
import io.github.thebusybiscuit.sensibletoolbox.items.EnderBag;
import io.github.thebusybiscuit.sensibletoolbox.items.EnderLeash;
import io.github.thebusybiscuit.sensibletoolbox.items.EnderTuner;
import io.github.thebusybiscuit.sensibletoolbox.items.GoldCombineHoe;
import io.github.thebusybiscuit.sensibletoolbox.items.IronCombineHoe;
import io.github.thebusybiscuit.sensibletoolbox.items.LandMarker;
import io.github.thebusybiscuit.sensibletoolbox.items.MoistureChecker;
import io.github.thebusybiscuit.sensibletoolbox.items.MultiBuilder;
import io.github.thebusybiscuit.sensibletoolbox.items.Multimeter;
import io.github.thebusybiscuit.sensibletoolbox.items.PVCell;
import io.github.thebusybiscuit.sensibletoolbox.items.PaintBrush;
import io.github.thebusybiscuit.sensibletoolbox.items.PaintRoller;
import io.github.thebusybiscuit.sensibletoolbox.items.RecipeBook;
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
import io.github.thebusybiscuit.sensibletoolbox.items.machineupgrades.EjectorUpgrade;
import io.github.thebusybiscuit.sensibletoolbox.items.machineupgrades.RegulatorUpgrade;
import io.github.thebusybiscuit.sensibletoolbox.items.machineupgrades.SpeedUpgrade;
import io.github.thebusybiscuit.sensibletoolbox.items.machineupgrades.ThoroughnessUpgrade;
import io.github.thebusybiscuit.sensibletoolbox.listeners.AnvilListener;
import io.github.thebusybiscuit.sensibletoolbox.listeners.ElevatorListener;
import io.github.thebusybiscuit.sensibletoolbox.listeners.FurnaceListener;
import io.github.thebusybiscuit.sensibletoolbox.listeners.GeneralListener;
import io.github.thebusybiscuit.sensibletoolbox.listeners.MobListener;
import io.github.thebusybiscuit.sensibletoolbox.listeners.PlayerUUIDTracker;
import io.github.thebusybiscuit.sensibletoolbox.listeners.SoundMufflerListener;
import io.github.thebusybiscuit.sensibletoolbox.listeners.TrashCanListener;
import io.github.thebusybiscuit.sensibletoolbox.listeners.WorldListener;
import io.github.thebusybiscuit.sensibletoolbox.slimefun.SlimefunBridge;
import io.github.thebusybiscuit.sensibletoolbox.util.ItemGlow;
import io.github.thebusybiscuit.sensibletoolbox.util.STBUtil;
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
    private final CommandManager cmds = new CommandManager(this);
    private ConfigurationManager configManager;
    private boolean protocolLibEnabled = false;
    private SoundMufflerListener soundMufflerListener;
    private PlayerUUIDTracker uuidTracker;
    private boolean enabled = false;
    private boolean holographicDisplays = false;
    private BukkitTask energyTask = null;
    private EnderStorageManager enderStorageManager;
    private STBItemRegistry itemRegistry;
    private STBFriendManager friendManager;
    private EnergyNetManager enetManager;
    private ConfigCache configCache;
    private IDTracker scuRelayIDTracker;
    private ProtectionManager protectionManager;

    @Override
    public void onEnable() {
        instance = this;

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

        scuRelayIDTracker = new IDTracker(this, "scu_relay_id");

        STBInventoryGUI.buildStockTextures();

        itemRegistry = new STBItemRegistry(this, "item_data");
        registerItems();

        friendManager = new STBFriendManager(this);
        enetManager = new EnergyNetManager(this);

        registerEventListeners();
        registerCommands();

        try {
            LocationManager.getManager().load();
        }
        catch (Exception e) {
            getLogger().log(Level.SEVERE, "An Error occured while loading Locations...", e);
            setEnabled(false);
            return;
        }

        MessagePager.setPageCmd("/stb page [#|n|p]");
        MessagePager.setDefaultPageSize(getConfig().getInt("pager.lines", 0));

        // do all the recipe setup on a delayed task to ensure we pick up
        // custom recipes from any plugins that may have loaded after us
        Bukkit.getScheduler().runTask(this, () -> {
            RecipeUtil.findVanillaFurnaceMaterials();
            RecipeUtil.setupRecipes();
            RecipeBook.buildRecipes();

            protectionManager = new ProtectionManager(getServer());
        });

        Bukkit.getScheduler().runTaskTimer(this, LocationManager.getManager()::tick, 1L, 1L);
        Bukkit.getScheduler().runTaskTimer(this, getEnderStorageManager()::tick, 1L, 300L);
        Bukkit.getScheduler().runTaskTimer(this, friendManager::save, 60L, 300L);

        scheduleEnergyNetTicker();

        if (Bukkit.getPluginManager().isPluginEnabled("Slimefun")) {
            new SlimefunBridge(this);
        }

        if (getConfig().getBoolean("options.auto-update") && getDescription().getVersion().startsWith("DEV - ")) {
            Updater updater = new GitHubBuildsUpdater(this, getFile(), "Slimefun/SensibleToolbox/master");
            updater.start();
        }

        enabled = true;
    }

    @Override
    public void onDisable() {
        if (!enabled) {
            return;
        }

        for (Player p : Bukkit.getOnlinePlayers()) {
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

        Bukkit.getScheduler().cancelTasks(this);

        instance = null;
    }

    public void registerItems() {
        String CONFIG_NODE = "items_enabled";
        String PERMISSION_NODE = "stb";

        itemRegistry.registerItem(new AngelicBlock(), this, CONFIG_NODE, PERMISSION_NODE);
        itemRegistry.registerItem(new EnderLeash(), this, CONFIG_NODE, PERMISSION_NODE);
        itemRegistry.registerItem(new RedstoneClock(), this, CONFIG_NODE, PERMISSION_NODE);
        itemRegistry.registerItem(new BlockUpdateDetector(), this, CONFIG_NODE, PERMISSION_NODE);
        itemRegistry.registerItem(new EnderBag(), this, CONFIG_NODE, PERMISSION_NODE);
        itemRegistry.registerItem(new WateringCan(), this, CONFIG_NODE, PERMISSION_NODE);
        itemRegistry.registerItem(new MoistureChecker(), this, CONFIG_NODE, PERMISSION_NODE);
        itemRegistry.registerItem(new AdvancedMoistureChecker(), this, CONFIG_NODE, PERMISSION_NODE);
        itemRegistry.registerItem(new WoodCombineHoe(), this, CONFIG_NODE, PERMISSION_NODE);
        itemRegistry.registerItem(new IronCombineHoe(), this, CONFIG_NODE, PERMISSION_NODE);
        itemRegistry.registerItem(new GoldCombineHoe(), this, CONFIG_NODE, PERMISSION_NODE);
        itemRegistry.registerItem(new DiamondCombineHoe(), this, CONFIG_NODE, PERMISSION_NODE);
        itemRegistry.registerItem(new TrashCan(), this, CONFIG_NODE, PERMISSION_NODE);
        itemRegistry.registerItem(new PaintBrush(), this, CONFIG_NODE, PERMISSION_NODE);
        itemRegistry.registerItem(new PaintRoller(), this, CONFIG_NODE, PERMISSION_NODE);
        itemRegistry.registerItem(new PaintCan(), this, CONFIG_NODE, PERMISSION_NODE);
        itemRegistry.registerItem(new Elevator(), this, CONFIG_NODE, PERMISSION_NODE);
        itemRegistry.registerItem(new TapeMeasure(), this, CONFIG_NODE, PERMISSION_NODE);
        itemRegistry.registerItem(new CircuitBoard(), this, CONFIG_NODE, PERMISSION_NODE);
        itemRegistry.registerItem(new SimpleCircuit(), this, CONFIG_NODE, PERMISSION_NODE);
        itemRegistry.registerItem(new MultiBuilder(), this, CONFIG_NODE, PERMISSION_NODE);
        itemRegistry.registerItem(new MachineFrame(), this, CONFIG_NODE, PERMISSION_NODE);
        itemRegistry.registerItem(new Smelter(), this, CONFIG_NODE, PERMISSION_NODE);
        itemRegistry.registerItem(new Masher(), this, CONFIG_NODE, PERMISSION_NODE);
        itemRegistry.registerItem(new Sawmill(), this, CONFIG_NODE, PERMISSION_NODE);
        itemRegistry.registerItem(new IronDust(), this, CONFIG_NODE, PERMISSION_NODE);
        itemRegistry.registerItem(new GoldDust(), this, CONFIG_NODE, PERMISSION_NODE);
        itemRegistry.registerItem(new ItemRouter(), this, CONFIG_NODE, PERMISSION_NODE);
        itemRegistry.registerItem(new BlankModule(), this, CONFIG_NODE, PERMISSION_NODE);
        itemRegistry.registerItem(new PullerModule(), this, CONFIG_NODE, PERMISSION_NODE);
        itemRegistry.registerItem(new DropperModule(), this, CONFIG_NODE, PERMISSION_NODE);
        itemRegistry.registerItem(new SenderModule(), this, CONFIG_NODE, PERMISSION_NODE);
        itemRegistry.registerItem(new DistributorModule(), this, CONFIG_NODE, PERMISSION_NODE);
        itemRegistry.registerItem(new AdvancedSenderModule(), this, CONFIG_NODE, PERMISSION_NODE);
        itemRegistry.registerItem(new HyperSenderModule(), this, CONFIG_NODE, PERMISSION_NODE);
        itemRegistry.registerItem(new ReceiverModule(), this, CONFIG_NODE, PERMISSION_NODE);
        itemRegistry.registerItem(new SorterModule(), this, CONFIG_NODE, PERMISSION_NODE);
        itemRegistry.registerItem(new VacuumModule(), this, CONFIG_NODE, PERMISSION_NODE);
        itemRegistry.registerItem(new BreakerModule(), this, CONFIG_NODE, PERMISSION_NODE);
        itemRegistry.registerItem(new StackModule(), this, CONFIG_NODE, PERMISSION_NODE);
        itemRegistry.registerItem(new SpeedModule(), this, CONFIG_NODE, PERMISSION_NODE);
        itemRegistry.registerItem(new TenKEnergyCell(), this, CONFIG_NODE, PERMISSION_NODE);
        itemRegistry.registerItem(new FiftyKEnergyCell(), this, CONFIG_NODE, PERMISSION_NODE);
        itemRegistry.registerItem(new TenKBatteryBox(), this, CONFIG_NODE, PERMISSION_NODE);
        itemRegistry.registerItem(new FiftyKBatteryBox(), this, CONFIG_NODE, PERMISSION_NODE);
        itemRegistry.registerItem(new SpeedUpgrade(), this, CONFIG_NODE, PERMISSION_NODE);
        itemRegistry.registerItem(new EjectorUpgrade(), this, CONFIG_NODE, PERMISSION_NODE);
        itemRegistry.registerItem(new RegulatorUpgrade(), this, CONFIG_NODE, PERMISSION_NODE);
        itemRegistry.registerItem(new ThoroughnessUpgrade(), this, CONFIG_NODE, PERMISSION_NODE);
        itemRegistry.registerItem(new HeatEngine(), this, CONFIG_NODE, PERMISSION_NODE);
        itemRegistry.registerItem(new BasicSolarCell(), this, CONFIG_NODE, PERMISSION_NODE);
        itemRegistry.registerItem(new DenseSolar(), this, CONFIG_NODE, PERMISSION_NODE);
        itemRegistry.registerItem(new RecipeBook(), this, CONFIG_NODE, PERMISSION_NODE);
        itemRegistry.registerItem(new AdvancedRecipeBook(), this, CONFIG_NODE, PERMISSION_NODE);
        itemRegistry.registerItem(new Multimeter(), this, CONFIG_NODE, PERMISSION_NODE);
        itemRegistry.registerItem(new BigStorageUnit(), this, CONFIG_NODE, PERMISSION_NODE);
        itemRegistry.registerItem(new HyperStorageUnit(), this, CONFIG_NODE, PERMISSION_NODE);
        itemRegistry.registerItem(new Pump(), this, CONFIG_NODE, PERMISSION_NODE);
        itemRegistry.registerItem(new EnderTuner(), this, CONFIG_NODE, PERMISSION_NODE);
        itemRegistry.registerItem(new EnderBox(), this, CONFIG_NODE, PERMISSION_NODE);
        itemRegistry.registerItem(new InfernalDust(), this, CONFIG_NODE, PERMISSION_NODE);
        itemRegistry.registerItem(new EnergizedIronDust(), this, CONFIG_NODE, PERMISSION_NODE);
        itemRegistry.registerItem(new EnergizedGoldDust(), this, CONFIG_NODE, PERMISSION_NODE);
        itemRegistry.registerItem(new EnergizedIronIngot(), this, CONFIG_NODE, PERMISSION_NODE);
        itemRegistry.registerItem(new EnergizedGoldIngot(), this, CONFIG_NODE, PERMISSION_NODE);
        itemRegistry.registerItem(new ToughMachineFrame(), this, CONFIG_NODE, PERMISSION_NODE);
        itemRegistry.registerItem(new QuartzDust(), this, CONFIG_NODE, PERMISSION_NODE);
        itemRegistry.registerItem(new SiliconWafer(), this, CONFIG_NODE, PERMISSION_NODE);
        itemRegistry.registerItem(new IntegratedCircuit(), this, CONFIG_NODE, PERMISSION_NODE);
        itemRegistry.registerItem(new LandMarker(), this, CONFIG_NODE, PERMISSION_NODE);
        itemRegistry.registerItem(new PVCell(), this, CONFIG_NODE, PERMISSION_NODE);
        itemRegistry.registerItem(new AutoBuilder(), this, CONFIG_NODE, PERMISSION_NODE);
        itemRegistry.registerItem(new UnlinkedSCURelay(), this, CONFIG_NODE, PERMISSION_NODE);
        itemRegistry.registerItem(new SCURelay(), this, CONFIG_NODE, PERMISSION_NODE);
        itemRegistry.registerItem(new SilkyBreakerModule(), this, CONFIG_NODE, PERMISSION_NODE);
        itemRegistry.registerItem(new SubspaceTransponder(), this, CONFIG_NODE, PERMISSION_NODE);
        itemRegistry.registerItem(new BioEngine(), this, CONFIG_NODE, PERMISSION_NODE);
        itemRegistry.registerItem(new MagmaticEngine(), this, CONFIG_NODE, PERMISSION_NODE);
        itemRegistry.registerItem(new EnergizedQuartz(), this, CONFIG_NODE, PERMISSION_NODE);
        itemRegistry.registerItem(new ElectricalEnergizer(), this, CONFIG_NODE, PERMISSION_NODE);
        itemRegistry.registerItem(new PowerMonitor(), this, CONFIG_NODE, PERMISSION_NODE);
        itemRegistry.registerItem(new Fermenter(), this, CONFIG_NODE, PERMISSION_NODE);
        itemRegistry.registerItem(new FishBait(), this, CONFIG_NODE, PERMISSION_NODE);
        itemRegistry.registerItem(new FishingNet(), this, CONFIG_NODE, PERMISSION_NODE);
        itemRegistry.registerItem(new AutoFarm(), this, CONFIG_NODE, PERMISSION_NODE);
        itemRegistry.registerItem(new AutoForester(), this, CONFIG_NODE, PERMISSION_NODE);
        itemRegistry.registerItem(new AdvancedFarm(), this, CONFIG_NODE, PERMISSION_NODE);
        itemRegistry.registerItem(new InfernalFarm(), this, CONFIG_NODE, PERMISSION_NODE);
        itemRegistry.registerItem(new AutoFarm2(), this, CONFIG_NODE, PERMISSION_NODE);

        if (isProtocolLibEnabled()) {
            itemRegistry.registerItem(new SoundMuffler(), this, CONFIG_NODE, PERMISSION_NODE);
        }
        if (isHolographicDisplaysEnabled()) {
            itemRegistry.registerItem(new HolographicMonitor(), this, CONFIG_NODE, PERMISSION_NODE);
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
        uuidTracker = new PlayerUUIDTracker(this);
        pm.registerEvents(uuidTracker, this);

        if (isProtocolLibEnabled()) {
            soundMufflerListener = new SoundMufflerListener(this);
            soundMufflerListener.start();
        }

        enderStorageManager = new EnderStorageManager(this);
        pm.registerEvents(enderStorageManager, this);
    }

    private void setupProtocolLib() {
        Plugin pLib = getServer().getPluginManager().getPlugin("ProtocolLib");

        if (pLib != null && pLib.isEnabled() && pLib instanceof ProtocolLibrary) {
            protocolLibEnabled = true;
            Debugger.getInstance().debug("Hooked ProtocolLib v" + pLib.getDescription().getVersion());
        }
        if (protocolLibEnabled) {
            if (getConfig().getBoolean("options.glowing_items")) ItemGlow.init(this);
        }
        else {
            LogUtils.warning("ProtocolLib not detected - some functionality is reduced:");
            LogUtils.warning("  No glowing items, Reduced particle effects, Sound Muffler item disabled");
        }
    }

    public boolean isProtocolLibEnabled() {
        return protocolLibEnabled;
    }

    public boolean isHolographicDisplaysEnabled() {
        return holographicDisplays;
    }

    private void registerCommands() {
        cmds.registerCommand(new SaveCommand());
        cmds.registerCommand(new RenameCommand());
        cmds.registerCommand(new GiveCommand());
        cmds.registerCommand(new ShowCommand());
        cmds.registerCommand(new ChargeCommand());
        cmds.registerCommand(new GetcfgCommand());
        cmds.registerCommand(new SetcfgCommand());
        cmds.registerCommand(new DebugCommand());
        cmds.registerCommand(new SoundCommand());
        cmds.registerCommand(new RecipeCommand());
        cmds.registerCommand(new ExamineCommand());
        cmds.registerCommand(new RedrawCommand());
        cmds.registerCommand(new FriendCommand());
        cmds.registerCommand(new UnfriendCommand());
        cmds.registerCommand(new ValidateCommand());
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
            return cmds.dispatch(sender, command, label, args);
        }
        catch (DHUtilsException e) {
            MiscUtil.errorMessage(sender, e.getMessage());
            return true;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        return cmds.onTabComplete(sender, command, label, args);
    }

    @Override
    public <T> T onConfigurationValidate(ConfigurationManager configurationManager, String key, T oldVal, T newVal) {
        if (key.equals("save_interval")) {
            Validate.isTrue((Integer) newVal > 0, "save_interval must be > 0");
        }
        else if (key.equals("energy.tick_rate")) {
            Validate.isTrue((Integer) newVal > 0, "energy.tick_rate must be > 0");
        }
        else if (key.startsWith("gui.texture.")) {
            STBUtil.parseMaterialSpec(newVal.toString());
        }
        else if (key.equals("default_access")) {
            getEnumValue(newVal.toString().toUpperCase(), AccessControl.class);
        }
        else if (key.equals("default_redstone")) {
            getEnumValue(newVal.toString().toUpperCase(), RedstoneBehaviour.class);
        }
        return newVal;
    }

    @SuppressWarnings({ "unchecked" })
    private <T> T getEnumValue(String value, Class<T> c) {
        try {
            Method m = c.getMethod("valueOf", String.class);
            // noinspection unchecked
            return (T) m.invoke(null, value);
        }
        catch (Exception e) {
            if (!(e instanceof InvocationTargetException) || !(e.getCause() instanceof IllegalArgumentException)) {
                e.printStackTrace();
                throw new DHUtilsException(e.getMessage());
            }
            else {
                throw new DHUtilsException("Unknown value: " + value);
            }
        }
    }

    @Override
    public <T> void onConfigurationChanged(ConfigurationManager configurationManager, String key, T oldVal, T newVal) {
        if (key.equals("debug_level")) {
            Debugger dbg = Debugger.getInstance();
            dbg.setLevel((Integer) newVal);

            if (dbg.getLevel() > 0) {
                dbg.setTarget(getServer().getConsoleSender());
            }
            else {
                dbg.setTarget(null);
            }
        }
        else if (key.equals("save_interval")) {
            LocationManager.getManager().setSaveInterval((Integer) newVal);
        }
        else if (key.equals("energy.tick_rate")) {
            scheduleEnergyNetTicker();
        }
        else if (key.startsWith("gui.texture.")) {
            STBInventoryGUI.buildStockTextures();
        }
        else if (key.equals("default_access")) {
            getConfigCache().setDefaultAccess(AccessControl.valueOf(newVal.toString().toUpperCase()));
        }
        else if (key.equals("default_redstone")) {
            getConfigCache().setDefaultRedstone(RedstoneBehaviour.valueOf(newVal.toString().toUpperCase()));
        }
        else if (key.equals("particle_effects")) {
            getConfigCache().setParticleLevel((Integer) newVal);
        }
        else if (key.equals("noisy_machines")) {
            getConfigCache().setNoisyMachines((Boolean) newVal);
        }
        else if (key.equals("creative_ender_access")) {
            getConfigCache().setCreativeEnderAccess((Boolean) newVal);
        }
    }

    private void scheduleEnergyNetTicker() {
        if (energyTask != null) {
            energyTask.cancel();
        }

        enetManager.setTickRate(getConfig().getLong("energy.tick_rate", EnergyNetManager.DEFAULT_TICK_RATE));
        energyTask = Bukkit.getScheduler().runTaskTimer(this, enetManager::tick, 1L, enetManager.getTickRate());
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

    public IDTracker getScuRelayIDTracker() {
        return scuRelayIDTracker;
    }

    public SoundMufflerListener getSoundMufflerListener() {
        return soundMufflerListener;
    }

    public PlayerUUIDTracker getUuidTracker() {
        return uuidTracker;
    }

    public ProtectionManager getProtectionManager() {
        return protectionManager;
    }

    public boolean isGlowingEnabled() {
        return isProtocolLibEnabled() && getConfig().getBoolean("options.glowing_items");
    }
}
