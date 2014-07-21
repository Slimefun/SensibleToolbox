package me.desht.sensibletoolbox;

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

import com.comphenix.protocol.ProtocolLibrary;
import com.griefcraft.lwc.LWC;
import com.griefcraft.lwc.LWCPlugin;
import me.desht.dhutils.*;
import me.desht.dhutils.commands.CommandManager;
import me.desht.dhutils.nms.NMSHelper;
import me.desht.sensibletoolbox.api.FriendManager;
import me.desht.sensibletoolbox.api.gui.InventoryGUI;
import me.desht.sensibletoolbox.api.recipes.RecipeUtil;
import me.desht.sensibletoolbox.api.util.STBUtil;
import me.desht.sensibletoolbox.api.util.SunlightLevels;
import me.desht.sensibletoolbox.commands.*;
import me.desht.sensibletoolbox.core.STBFriendManager;
import me.desht.sensibletoolbox.core.STBItemRegistry;
import me.desht.sensibletoolbox.core.enderstorage.EnderStorageManager;
import me.desht.sensibletoolbox.core.energy.EnergyNetManager;
import me.desht.sensibletoolbox.core.gui.STBInventoryGUI;
import me.desht.sensibletoolbox.core.storage.LocationManager;
import me.desht.sensibletoolbox.items.RecipeBook;
import me.desht.sensibletoolbox.listeners.*;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.mcstats.MetricsLite;

import java.io.IOException;
import java.util.List;
import java.util.Random;

public class SensibleToolboxPlugin extends JavaPlugin implements ConfigurationListener {

    private static SensibleToolboxPlugin instance = null;
    private final CommandManager cmds = new CommandManager(this);
    private ConfigurationManager configManager;
    private boolean protocolLibEnabled = false;
    private boolean isNMSenabled = false;
    private SoundMufflerListener soundMufflerListener;
    private FloodlightListener floodlightListener;
    private PlayerUUIDTracker uuidTracker;
    private boolean inited = false;
    private LandslideListener landslideListener = null;
    private boolean holoAPIenabled = false;
    private BukkitTask energyTask = null;
    private LWC lwc = null;
    private EnderStorageManager enderStorageManager;
    private SunlightLevels sunlightLevels;
    private STBItemRegistry itemRegistry;
    private STBFriendManager friendManager;
    private Random random;

    public static SensibleToolboxPlugin getInstance() {
        return instance;
    }

    @Override
    public void onLoad() {
        RecipeUtil.findVanillaFurnaceMaterials();
    }

    @Override
    public void onEnable() {
        instance = this;

        LogUtils.init(this);

        random = new Random();

        configManager = new ConfigurationManager(this, this);

        MiscUtil.init(this);
        MiscUtil.setColouredConsole(getConfig().getBoolean("coloured_console"));

        Debugger.getInstance().setPrefix("[STB] ");
        Debugger.getInstance().setLevel(getConfig().getInt("debug_level"));
        if (getConfig().getInt("debug_level") > 0) {
            Debugger.getInstance().setTarget(getServer().getConsoleSender());
        }

        setupNMS();
        if (!isNMSenabled) {
            LogUtils.warning("Unable to initialize NMS abstraction API - looks like this version of CraftBukkit isn't supported.");
            LogUtils.warning("Sensible Toolbox will continue to run with reduced functionality:");
            LogUtils.warning("  Floodlight will not cast light (but will still prevent mob spawns");
        }

        LogUtils.setLogLevel(getConfig().getString("log_level", "INFO"));

        setupHoloAPI();

        setupProtocolLib();
        if (protocolLibEnabled) {
            ItemGlow.init(this);
        } else {
            LogUtils.warning("ProtocolLib not detected - some functionality is reduced:");
            LogUtils.warning("  No glowing items, Reduced particle effects, Sound Muffler item disabled");
        }

        setupLandslide();
        setupLWC();

        STBInventoryGUI.buildStockTextures();

        itemRegistry = new STBItemRegistry(this);
        itemRegistry.registerItems();
        friendManager = new STBFriendManager(this);
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
        // custom recipes from any other plugins
        Bukkit.getScheduler().runTask(this, new Runnable() {
            @Override
            public void run() {
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

        setupMetrics();

        sunlightLevels = new SunlightLevels();
        Bukkit.getScheduler().runTaskTimer(this, sunlightLevels, 1L, 20L);

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

    public boolean isNMSenabled() {
        return isNMSenabled;
    }

    public SoundMufflerListener getSoundMufflerListener() {
        return soundMufflerListener;
    }

    public FloodlightListener getFloodlightListener() {
        return floodlightListener;
    }

    public PlayerUUIDTracker getUuidTracker() {
        return uuidTracker;
    }

    private void setupMetrics() {
        try {
            MetricsLite metrics = new MetricsLite(this);
            metrics.start();
        } catch (IOException e) {
            LogUtils.warning("Couldn't submit metrics stats: " + e.getMessage());
        }
    }

    private void setupNMS() {
        try {
            NMSHelper.init(this);
            isNMSenabled = true;
        } catch (Exception e) {
            e.printStackTrace();
            // do nothing
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
        floodlightListener = new FloodlightListener(this);
        pm.registerEvents(floodlightListener, this);
        enderStorageManager = new EnderStorageManager(this);
        pm.registerEvents(enderStorageManager, this);
    }

    private void setupProtocolLib() {
        Plugin pLib = getServer().getPluginManager().getPlugin("ProtocolLib");
        if (pLib != null && pLib.isEnabled() && pLib instanceof ProtocolLibrary) {
            protocolLibEnabled = true;
            Debugger.getInstance().debug("Hooked ProtocolLib v" + pLib.getDescription().getVersion());
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
            holoAPIenabled = true;
            Debugger.getInstance().debug("Hooked HoloAPI v" + plugin.getDescription().getVersion());
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
        }
        return newVal;
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
        }
    }

    private void scheduleEnergyNetTicker() {
        if (energyTask != null) {
            energyTask.cancel();
        }
        EnergyNetManager.setTickRate(getConfig().getLong("energy.tick_rate", EnergyNetManager.DEFAULT_TICK_RATE));
        energyTask = Bukkit.getScheduler().runTaskTimer(this, new Runnable() {
            @Override
            public void run() {
                EnergyNetManager.tick();
            }
        }, 1L, EnergyNetManager.getTickRate());
    }

    public ConfigurationManager getConfigManager() {
        return configManager;
    }

    public EnderStorageManager getEnderStorageManager() {
        return enderStorageManager;
    }

    public SunlightLevels getSunlightLevels() {
        return sunlightLevels;
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
}
