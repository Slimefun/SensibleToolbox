package me.mrCookieSlime.CSCoreLib.Configuration;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

public class ConfigSetup {
	
	public static void setup(Plugin plugin) {
		FileConfiguration cfg = plugin.getConfig();
		
		cfg.options().copyDefaults(true);
		plugin.saveConfig();
	}

}
