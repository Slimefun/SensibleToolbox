package me.mrCookieSlime.CSCoreLib.updater;

import java.io.File;

import org.bukkit.plugin.Plugin;

public class UpdaterService {
	
	public static boolean setup(Plugin plugin, int id, File file) {
		boolean allowed = false;
		
		if (plugin.getConfig().getBoolean("options.auto-update")) {
			new Updater(plugin, id, file, Updater.UpdateType.DEFAULT, true);
			allowed = true;
		}
		
		return allowed;
	}

}
