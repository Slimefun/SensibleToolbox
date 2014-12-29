package me.mrCookieSlime.sensibletoolbox;

/*
    This file is part of SensibleToolbox

    SensibleToolbox is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    SensibleToolbox is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with SensibleToolbox.  If not, see <http://www.gnu.org/licenses/>.
 */

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Random;

import me.desht.sensibletoolbox.dhutils.ConfigurationListener;
import me.desht.sensibletoolbox.dhutils.ConfigurationManager;
import me.desht.sensibletoolbox.dhutils.DHUtilsException;
import me.desht.sensibletoolbox.dhutils.DHValidate;
import me.desht.sensibletoolbox.dhutils.Debugger;
import me.desht.sensibletoolbox.dhutils.ItemGlow;
import me.desht.sensibletoolbox.dhutils.LogUtils;
import me.desht.sensibletoolbox.dhutils.MessagePager;
import me.desht.sensibletoolbox.dhutils.MiscUtil;
import me.desht.sensibletoolbox.dhutils.commands.CommandManager;
import me.mrCookieSlime.CSCoreLib.PluginStatistics.PluginStatistics;
import me.mrCookieSlime.sensibletoolbox.api.AccessControl;
import me.mrCookieSlime.sensibletoolbox.api.FriendManager;
import me.mrCookieSlime.sensibletoolbox.api.RedstoneBehaviour;
import me.mrCookieSlime.sensibletoolbox.api.gui.InventoryGUI;
import me.mrCookieSlime.sensibletoolbox.api.recipes.RecipeUtil;
import me.mrCookieSlime.sensibletoolbox.api.util.BlockProtection;
import me.mrCookieSlime.sensibletoolbox.api.util.STBUtil;
import me.mrCookieSlime.sensibletoolbox.blocks.AngelicBlock;
import me.mrCookieSlime.sensibletoolbox.blocks.BlockUpdateDetector;
import me.mrCookieSlime.sensibletoolbox.blocks.Elevator;
import me.mrCookieSlime.sensibletoolbox.blocks.EnderBox;
import me.mrCookieSlime.sensibletoolbox.blocks.ItemRouter;
import me.mrCookieSlime.sensibletoolbox.blocks.PaintCan;
import me.mrCookieSlime.sensibletoolbox.blocks.RedstoneClock;
import me.mrCookieSlime.sensibletoolbox.blocks.SoundMuffler;
import me.mrCookieSlime.sensibletoolbox.blocks.TrashCan;
import me.mrCookieSlime.sensibletoolbox.blocks.machines.AutoBuilder;
import me.mrCookieSlime.sensibletoolbox.blocks.machines.BasicSolarCell;
import me.mrCookieSlime.sensibletoolbox.blocks.machines.BigStorageUnit;
import me.mrCookieSlime.sensibletoolbox.blocks.machines.DenseSolar;
import me.mrCookieSlime.sensibletoolbox.blocks.machines.FiftyKBatteryBox;
import me.mrCookieSlime.sensibletoolbox.blocks.machines.HeatEngine;
import me.mrCookieSlime.sensibletoolbox.blocks.machines.HyperStorageUnit;
import me.mrCookieSlime.sensibletoolbox.blocks.machines.Masher;
import me.mrCookieSlime.sensibletoolbox.blocks.machines.Pump;
import me.mrCookieSlime.sensibletoolbox.blocks.machines.SCURelay;
import me.mrCookieSlime.sensibletoolbox.blocks.machines.Sawmill;
import me.mrCookieSlime.sensibletoolbox.blocks.machines.Smelter;
import me.mrCookieSlime.sensibletoolbox.blocks.machines.TenKBatteryBox;
import me.mrCookieSlime.sensibletoolbox.commands.ChargeCommand;
import me.mrCookieSlime.sensibletoolbox.commands.DebugCommand;
import me.mrCookieSlime.sensibletoolbox.commands.ExamineCommand;
import me.mrCookieSlime.sensibletoolbox.commands.FriendCommand;
import me.mrCookieSlime.sensibletoolbox.commands.GetcfgCommand;
import me.mrCookieSlime.sensibletoolbox.commands.GiveCommand;
import me.mrCookieSlime.sensibletoolbox.commands.ParticleCommand;
import me.mrCookieSlime.sensibletoolbox.commands.RecipeCommand;
import me.mrCookieSlime.sensibletoolbox.commands.RedrawCommand;
import me.mrCookieSlime.sensibletoolbox.commands.RenameCommand;
import me.mrCookieSlime.sensibletoolbox.commands.SaveCommand;
import me.mrCookieSlime.sensibletoolbox.commands.SetcfgCommand;
import me.mrCookieSlime.sensibletoolbox.commands.ShowCommand;
import me.mrCookieSlime.sensibletoolbox.commands.SoundCommand;
import me.mrCookieSlime.sensibletoolbox.commands.UnfriendCommand;
import me.mrCookieSlime.sensibletoolbox.commands.ValidateCommand;
import me.mrCookieSlime.sensibletoolbox.core.IDTracker;
import me.mrCookieSlime.sensibletoolbox.core.STBFriendManager;
import me.mrCookieSlime.sensibletoolbox.core.STBItemRegistry;
import me.mrCookieSlime.sensibletoolbox.core.enderstorage.EnderStorageManager;
import me.mrCookieSlime.sensibletoolbox.core.energy.EnergyNetManager;
import me.mrCookieSlime.sensibletoolbox.core.gui.STBInventoryGUI;
import me.mrCookieSlime.sensibletoolbox.core.storage.LocationManager;
import me.mrCookieSlime.sensibletoolbox.items.AdvancedMoistureChecker;
import me.mrCookieSlime.sensibletoolbox.items.AdvancedRecipeBook;
import me.mrCookieSlime.sensibletoolbox.items.BagOfHolding;
import me.mrCookieSlime.sensibletoolbox.items.DiamondCombineHoe;
import me.mrCookieSlime.sensibletoolbox.items.EnderBag;
import me.mrCookieSlime.sensibletoolbox.items.EnderLeash;
import me.mrCookieSlime.sensibletoolbox.items.EnderTuner;
import me.mrCookieSlime.sensibletoolbox.items.GoldCombineHoe;
import me.mrCookieSlime.sensibletoolbox.items.IronCombineHoe;
import me.mrCookieSlime.sensibletoolbox.items.LandMarker;
import me.mrCookieSlime.sensibletoolbox.items.MoistureChecker;
import me.mrCookieSlime.sensibletoolbox.items.MultiBuilder;
import me.mrCookieSlime.sensibletoolbox.items.Multimeter;
import me.mrCookieSlime.sensibletoolbox.items.PVCell;
import me.mrCookieSlime.sensibletoolbox.items.PaintBrush;
import me.mrCookieSlime.sensibletoolbox.items.PaintRoller;
import me.mrCookieSlime.sensibletoolbox.items.RecipeBook;
import me.mrCookieSlime.sensibletoolbox.items.TapeMeasure;
import me.mrCookieSlime.sensibletoolbox.items.WateringCan;
import me.mrCookieSlime.sensibletoolbox.items.WoodCombineHoe;
import me.mrCookieSlime.sensibletoolbox.items.components.CircuitBoard;
import me.mrCookieSlime.sensibletoolbox.items.components.EnergizedGoldDust;
import me.mrCookieSlime.sensibletoolbox.items.components.EnergizedGoldIngot;
import me.mrCookieSlime.sensibletoolbox.items.components.EnergizedIronDust;
import me.mrCookieSlime.sensibletoolbox.items.components.EnergizedIronIngot;
import me.mrCookieSlime.sensibletoolbox.items.components.GoldDust;
import me.mrCookieSlime.sensibletoolbox.items.components.InfernalDust;
import me.mrCookieSlime.sensibletoolbox.items.components.IntegratedCircuit;
import me.mrCookieSlime.sensibletoolbox.items.components.IronDust;
import me.mrCookieSlime.sensibletoolbox.items.components.MachineFrame;
import me.mrCookieSlime.sensibletoolbox.items.components.QuartzDust;
import me.mrCookieSlime.sensibletoolbox.items.components.SiliconWafer;
import me.mrCookieSlime.sensibletoolbox.items.components.SimpleCircuit;
import me.mrCookieSlime.sensibletoolbox.items.components.SubspaceTransponder;
import me.mrCookieSlime.sensibletoolbox.items.components.ToughMachineFrame;
import me.mrCookieSlime.sensibletoolbox.items.components.UnlinkedSCURelay;
import me.mrCookieSlime.sensibletoolbox.items.energycells.FiftyKEnergyCell;
import me.mrCookieSlime.sensibletoolbox.items.energycells.TenKEnergyCell;
import me.mrCookieSlime.sensibletoolbox.items.itemroutermodules.AdvancedSenderModule;
import me.mrCookieSlime.sensibletoolbox.items.itemroutermodules.BlankModule;
import me.mrCookieSlime.sensibletoolbox.items.itemroutermodules.BreakerModule;
import me.mrCookieSlime.sensibletoolbox.items.itemroutermodules.DistributorModule;
import me.mrCookieSlime.sensibletoolbox.items.itemroutermodules.DropperModule;
import me.mrCookieSlime.sensibletoolbox.items.itemroutermodules.HyperSenderModule;
import me.mrCookieSlime.sensibletoolbox.items.itemroutermodules.PullerModule;
import me.mrCookieSlime.sensibletoolbox.items.itemroutermodules.ReceiverModule;
import me.mrCookieSlime.sensibletoolbox.items.itemroutermodules.SenderModule;
import me.mrCookieSlime.sensibletoolbox.items.itemroutermodules.SilkyBreakerModule;
import me.mrCookieSlime.sensibletoolbox.items.itemroutermodules.SorterModule;
import me.mrCookieSlime.sensibletoolbox.items.itemroutermodules.SpeedModule;
import me.mrCookieSlime.sensibletoolbox.items.itemroutermodules.StackModule;
import me.mrCookieSlime.sensibletoolbox.items.itemroutermodules.VacuumModule;
import me.mrCookieSlime.sensibletoolbox.items.machineupgrades.EjectorUpgrade;
import me.mrCookieSlime.sensibletoolbox.items.machineupgrades.RegulatorUpgrade;
import me.mrCookieSlime.sensibletoolbox.items.machineupgrades.SpeedUpgrade;
import me.mrCookieSlime.sensibletoolbox.items.machineupgrades.ThoroughnessUpgrade;
import me.mrCookieSlime.sensibletoolbox.listeners.AnvilListener;
import me.mrCookieSlime.sensibletoolbox.listeners.ElevatorListener;
import me.mrCookieSlime.sensibletoolbox.listeners.FurnaceListener;
import me.mrCookieSlime.sensibletoolbox.listeners.GeneralListener;
import me.mrCookieSlime.sensibletoolbox.listeners.LandslideListener;
import me.mrCookieSlime.sensibletoolbox.listeners.MobListener;
import me.mrCookieSlime.sensibletoolbox.listeners.PlayerUUIDTracker;
import me.mrCookieSlime.sensibletoolbox.listeners.SoundMufflerListener;
import me.mrCookieSlime.sensibletoolbox.listeners.TrashCanListener;
import me.mrCookieSlime.sensibletoolbox.listeners.WorldListener;
import me.mrCookieSlime.sensibletoolbox.util.SlimefunManager;
import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import com.comphenix.protocol.ProtocolLibrary;
import com.griefcraft.lwc.LWC;
import com.griefcraft.lwc.LWCPlugin;
import com.onarandombox.MultiverseCore.MultiverseCore;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

public class SensibleToolboxPlugin extends JavaPlugin implements ConfigurationListener {

    private static SensibleToolboxPlugin instance = null;
    private final CommandManager cmds = new CommandManager(this);
    private ConfigurationManager configManager;
    private boolean protocolLibEnabled = false;
    private SoundMufflerListener soundMufflerListener;
    private PlayerUUIDTracker uuidTracker;
    private boolean inited = false;
    private LandslideListener landslideListener = null;
    private boolean holoAPIenabled = false;
    private BukkitTask energyTask = null;
    private LWC lwc = null;
    private EnderStorageManager enderStorageManager;
    private STBItemRegistry itemRegistry;
    private STBFriendManager friendManager;
    private Random random;
    private EnergyNetManager enetManager;
    private WorldGuardPlugin worldGuardPlugin = null;
    private PreciousStones preciousStonesPlugin = null;
    private BlockProtection blockProtection;
    private ConfigCache configCache;
    private MultiverseCore multiverseCore = null;
    private IDTracker scuRelayIDTracker;

    public static SensibleToolboxPlugin getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;

        LogUtils.init(this);

        random = new Random();

        configManager = new ConfigurationManager(this, this);

        configCache = new ConfigCache(this);
        configCache.processConfig();

        MiscUtil.init(this);
        MiscUtil.setColouredConsole(getConfig().getBoolean("coloured_console"));

        LogUtils.setLogLevel(getConfig().getString("log_level", "INFO"));

        Debugger.getInstance().setPrefix("[STB] ");
        Debugger.getInstance().setLevel(getConfig().getInt("debug_level"));
        if (getConfig().getInt("debug_level") > 0) Debugger.getInstance().setTarget(getServer().getConsoleSender());

        // try to hook other plugins
        setupHoloAPI();
        setupProtocolLib();
        setupLandslide();
        setupLWC();
        setupWorldGuard();
        setupPreciousStones();
        setupMultiverse();

        scuRelayIDTracker = new IDTracker(this, "scu_relay_id");

        blockProtection = new BlockProtection(this);

        STBInventoryGUI.buildStockTextures();

        itemRegistry = new STBItemRegistry();
        registerItems();

        friendManager = new STBFriendManager(this);
        enetManager = new EnergyNetManager(this);

        registerEventListeners();
        registerCommands();

        try {
            LocationManager.getManager().load();
        } catch (Exception e) {
            e.printStackTrace();
            setEnabled(false);
            return;
        }

        MessagePager.setPageCmd("/stb page [#|n|p]");
        MessagePager.setDefaultPageSize(getConfig().getInt("pager.lines", 0));

        // do all the recipe setup on a delayed task to ensure we pick up
        // custom recipes from any plugins that may have loaded after us
        Bukkit.getScheduler().runTask(this, new Runnable() {
            @Override
            public void run() {
                RecipeUtil.findVanillaFurnaceMaterials();
                RecipeUtil.setupRecipes();
                RecipeBook.buildRecipes();
            }
        });

        Bukkit.getScheduler().runTaskTimer(this, new Runnable() {
            @Override
            public void run() {
                LocationManager.getManager().tick();
            }
        }, 1L, 1L);

        Bukkit.getScheduler().runTaskTimer(this, new Runnable() {
            @Override
            public void run() {
                getEnderStorageManager().tick();
            }
        }, 1L, 300L);

        Bukkit.getScheduler().runTaskTimer(this, new Runnable() {
            @Override
            public void run() {
                friendManager.save();
            }
        }, 60L, 300L);

        scheduleEnergyNetTicker();

        PluginStatistics.collect(this);
        
        SlimefunManager.initiate();

        inited = true;
    }

    public void onDisable() {
        if (!inited) {
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

    public LWC getLWC() {
        return lwc;
    }

    public SoundMufflerListener getSoundMufflerListener() {
        return soundMufflerListener;
    }

    public PlayerUUIDTracker getUuidTracker() {
        return uuidTracker;
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

    private void setupWorldGuard() {
        Plugin plugin = getServer().getPluginManager().getPlugin("WorldGuard");

        if (plugin != null && plugin.isEnabled() && plugin instanceof WorldGuardPlugin) {
            Debugger.getInstance().debug("Hooked WorldGuard v" + plugin.getDescription().getVersion());
            worldGuardPlugin = (WorldGuardPlugin) plugin;
        }
    }

    private void setupPreciousStones() {
        Plugin plugin = getServer().getPluginManager().getPlugin("PreciousStones");

        if (plugin != null && plugin.isEnabled() && plugin instanceof PreciousStones) {
            Debugger.getInstance().debug("Hooked PreciousStones v" + plugin.getDescription().getVersion());
            preciousStonesPlugin = (PreciousStones) plugin;
        }
    }

    private void setupProtocolLib() {
        Plugin pLib = getServer().getPluginManager().getPlugin("ProtocolLib");
        if (pLib != null && pLib.isEnabled() && pLib instanceof ProtocolLibrary) {
            protocolLibEnabled = true;
            Debugger.getInstance().debug("Hooked ProtocolLib v" + pLib.getDescription().getVersion());
        }
        if (protocolLibEnabled) {
            ItemGlow.init(this);
        } else {
            LogUtils.warning("ProtocolLib not detected - some functionality is reduced:");
            LogUtils.warning("  No glowing items, Reduced particle effects, Sound Muffler item disabled");
        }
    }

    private void setupMultiverse() {
        Plugin mvPlugin = getServer().getPluginManager().getPlugin("Multiverse-Core");
        if (mvPlugin != null && mvPlugin.isEnabled() && mvPlugin instanceof MultiverseCore) {
            multiverseCore = (MultiverseCore) mvPlugin;
            Debugger.getInstance().debug("Hooked Multiverse-Core v" + mvPlugin.getDescription().getVersion());
        }
    }

    private void setupLWC() {
        Plugin lwcPlugin = getServer().getPluginManager().getPlugin("LWC");
        if (lwcPlugin != null && lwcPlugin.isEnabled() && lwcPlugin instanceof LWCPlugin) {
            lwc = ((LWCPlugin) lwcPlugin).getLWC();
            Debugger.getInstance().debug("Hooked LWC v" + lwcPlugin.getDescription().getVersion());
        }
    }

    private void setupLandslide() {
        Plugin plugin = getServer().getPluginManager().getPlugin("Landslide");
        if (plugin != null && plugin.isEnabled()) {
            landslideListener = new LandslideListener(this);
            Debugger.getInstance().debug("Hooked Landslide v" + plugin.getDescription().getVersion());
        }
    }

    private void setupHoloAPI() {
        Plugin plugin = getServer().getPluginManager().getPlugin("HoloAPI");
        if (plugin != null && plugin.isEnabled()) {
            if (!plugin.getDescription().getVersion().startsWith("1.")) {
                LogUtils.warning("Found HoloAPI " + plugin.getDescription().getVersion() +
                        ", but only HoloAPI v1.x is supported by SensibleToolbox at this time - disabling HoloAPI support");
            } else {
                holoAPIenabled = true;
                Debugger.getInstance().debug("Hooked HoloAPI v" + plugin.getDescription().getVersion());
            }
        }
    }

    public boolean isProtocolLibEnabled() {
        return protocolLibEnabled;
    }

    public LandslideListener getLandslideListener() {
        return landslideListener;
    }

    public boolean isHoloAPIenabled() {
        return holoAPIenabled;
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
        cmds.registerCommand(new ParticleCommand());
        cmds.registerCommand(new SoundCommand());
        cmds.registerCommand(new RecipeCommand());
        cmds.registerCommand(new ExamineCommand());
        cmds.registerCommand(new RedrawCommand());
        cmds.registerCommand(new FriendCommand());
        cmds.registerCommand(new UnfriendCommand());
        cmds.registerCommand(new ValidateCommand());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        try {
            return cmds.dispatch(sender, command, label, args);
        } catch (DHUtilsException e) {
            MiscUtil.errorMessage(sender, e.getMessage());
            return true;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        return cmds.onTabComplete(sender, command, label, args);
    }

    @Override
    public Object onConfigurationValidate(ConfigurationManager configurationManager, String key, Object oldVal, Object newVal) {
        if (key.equals("save_interval")) {
            DHValidate.isTrue((Integer) newVal > 0, "save_interval must be > 0");
        } else if (key.equals("energy.tick_rate")) {
            DHValidate.isTrue((Integer) newVal > 0, "energy.tick_rate must be > 0");
        } else if (key.startsWith("gui.texture.")) {
            STBUtil.parseMaterialSpec(newVal.toString());
        } else if (key.equals("inventory_protection")) {
            getEnumValue(newVal.toString().toUpperCase(), BlockProtection.InvProtectionType.class);
        } else if (key.equals("block_protection")) {
            getEnumValue(newVal.toString().toUpperCase(), BlockProtection.BlockProtectionType.class);
        } else if (key.equals("default_access")) {
            getEnumValue(newVal.toString().toUpperCase(), AccessControl.class);
        } else if (key.equals("default_redstone")) {
            getEnumValue(newVal.toString().toUpperCase(), RedstoneBehaviour.class);
        }
        return newVal;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
	private <T extends Enum> T getEnumValue(String value, Class<T> c) {
        try {
            Method m = c.getMethod("valueOf", String.class);
            //noinspection unchecked
            return (T) m.invoke(null, value);
        } catch (Exception e) {
            if (!(e instanceof InvocationTargetException) || !(e.getCause() instanceof IllegalArgumentException)) {
                e.printStackTrace();
                throw new DHUtilsException(e.getMessage());
            } else {
                throw new DHUtilsException("Unknown value: " + value);
            }
        }
    }

    @Override
    public void onConfigurationChanged(ConfigurationManager configurationManager, String key, Object oldVal, Object newVal) {
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
        } else if (key.equals("inventory_protection")) {
            blockProtection.setInvProtectionType(BlockProtection.InvProtectionType.valueOf(newVal.toString().toUpperCase()));
        } else if (key.equals("block_protection")) {
            blockProtection.setBlockProtectionType(BlockProtection.BlockProtectionType.valueOf(newVal.toString().toUpperCase()));
        } else if (key.equals("default_access")) {
            getConfigCache().setDefaultAccess(AccessControl.valueOf(newVal.toString().toUpperCase()));
        } else if (key.equals("default_redstone")) {
            getConfigCache().setDefaultRedstone(RedstoneBehaviour.valueOf(newVal.toString().toUpperCase()));
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
        energyTask = Bukkit.getScheduler().runTaskTimer(this, new Runnable() {
            @Override
            public void run() {
                enetManager.tick();
            }
        }, 1L, enetManager.getTickRate());
    }

    public void registerItems() {
        final String CONFIG_NODE = "items_enabled";
        final String PERMISSION_NODE = "stb";

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
        itemRegistry.registerItem(new BagOfHolding(), this, CONFIG_NODE, PERMISSION_NODE);
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
        if (isProtocolLibEnabled()) itemRegistry.registerItem(new SoundMuffler(), this, CONFIG_NODE, PERMISSION_NODE);

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

    public Random getRandom() {
        return random;
    }

    public EnergyNetManager getEnergyNetManager() {
        return enetManager;
    }

    public boolean isWorldGuardAvailable() {
        return worldGuardPlugin != null && worldGuardPlugin.isEnabled();
    }

    public boolean isPreciousStonesAvailable() {
        return preciousStonesPlugin != null && preciousStonesPlugin.isEnabled();
    }

    public BlockProtection getBlockProtection() {
        return blockProtection;
    }

    public ConfigCache getConfigCache() {
        return configCache;
    }

    public MultiverseCore getMultiverseCore() {
        return multiverseCore;
    }

    public IDTracker getScuRelayIDTracker() {
        return scuRelayIDTracker;
    }
}
