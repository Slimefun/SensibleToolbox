package me.mrCookieSlime.CSCoreLib.PluginStatistics;

import java.io.IOException;

import org.bukkit.plugin.Plugin;

public class PluginStatistics {
	
	public static boolean collect(Plugin plugin) {
		try {
			Metrics metrics = new Metrics(plugin);
			metrics.start();
			return true;
		} catch (IOException e) {
			return false;
		}
	}

}
