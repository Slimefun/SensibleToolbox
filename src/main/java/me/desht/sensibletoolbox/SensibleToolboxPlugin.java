package me.desht.sensibletoolbox;

/*
    This file is part of SensibleToolbox

    Foobar is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Foobar is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with SensibleToolbox.  If not, see <http://www.gnu.org/licenses/>.
 */

import com.comphenix.protocol.ProtocolLibrary;
import me.desht.dhutils.*;
import me.desht.dhutils.commands.CommandManager;
import me.desht.sensibletoolbox.commands.RenameCommand;
import me.desht.sensibletoolbox.commands.SaveCommand;
import me.desht.sensibletoolbox.items.AngelicBlock;
import me.desht.sensibletoolbox.items.BlockUpdateDetector;
import me.desht.sensibletoolbox.items.EnderLeash;
import me.desht.sensibletoolbox.items.RedstoneClock;
import me.desht.sensibletoolbox.listeners.BlockListener;
import me.desht.sensibletoolbox.listeners.PlayerListener;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.UUID;

public class SensibleToolboxPlugin extends JavaPlugin implements ConfigurationListener {

	public static final UUID UNIQUE_ID = UUID.fromString("60884913-70bb-48b3-a81a-54952dec2e31");
	private static SensibleToolboxPlugin instance = null;
	private LocationManager locationManager;
	private ConfigurationManager configManager;
	private final CommandManager cmds = new CommandManager(this);
	private boolean protocolLibEnabled = false;

	@Override
	public void onLoad() {
		ConfigurationSerialization.registerClass(AngelicBlock.class);
		ConfigurationSerialization.registerClass(RedstoneClock.class);
		ConfigurationSerialization.registerClass(EnderLeash.class);
		ConfigurationSerialization.registerClass(BlockUpdateDetector.class);
	}

	@Override
	public void onEnable() {
		instance = this;

		LogUtils.init(this);

		configManager = new ConfigurationManager(this, this);

		MiscUtil.init(this);
		MiscUtil.setColouredConsole(getConfig().getBoolean("coloured_console"));

		LogUtils.setLogLevel(getConfig().getString("log_level", "INFO"));

		PluginManager pm = this.getServer().getPluginManager();
		pm.registerEvents(new PlayerListener(this), this);
		pm.registerEvents(new BlockListener(this), this);

		setupProtocolLib();

		registerCommands();

		locationManager = new LocationManager(this);
		locationManager.load();

		saveDefaultConfig();

		if (protocolLibEnabled) {
			ItemGlow.init(this);
		}

		setupRecipes();

		Bukkit.getScheduler().runTaskTimer(this, new Runnable() {
			@Override
			public void run() {
				locationManager.tick();
			}
		}, 1L, 1L);

	}

	public void onDisable() {
		locationManager.save();

		instance = null;
	}

	private void setupProtocolLib() {
		Plugin pLib = getServer().getPluginManager().getPlugin("ProtocolLib");
		if (pLib != null && pLib instanceof ProtocolLibrary && pLib.isEnabled()) {
			protocolLibEnabled = true;
			LogUtils.fine("Hooked ProtocolLib v" + pLib.getDescription().getVersion());
		}
	}

	public boolean isProtocolLibEnabled() {
		return protocolLibEnabled;
	}

	private void registerCommands() {
		cmds.registerCommand(new SaveCommand());
		cmds.registerCommand(new RenameCommand());
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

	public static SensibleToolboxPlugin getInstance() {
		return instance;
	}

	private void setupRecipes() {
		Bukkit.addRecipe(new AngelicBlock().getRecipe());
		Bukkit.addRecipe(new EnderLeash().getRecipe());
		Bukkit.addRecipe(new RedstoneClock().getRecipe());
		Bukkit.addRecipe(new BlockUpdateDetector().getRecipe());
	}

	public LocationManager getLocationManager() {
		return locationManager;
	}

	@Override
	public void onConfigurationValidate(ConfigurationManager configurationManager, String key, Object oldVal, Object newVal) {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public void onConfigurationChanged(ConfigurationManager configurationManager, String key, Object oldVal, Object newVal) {
		//To change body of implemented methods use File | Settings | File Templates.
	}
}
