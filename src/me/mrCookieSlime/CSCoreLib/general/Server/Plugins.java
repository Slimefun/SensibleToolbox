package me.mrCookieSlime.CSCoreLib.general.Server;

import me.mrCookieSlime.CSCoreLib.CSCoreLib;
import me.mrCookieSlime.CSCoreLib.general.Clock;

import org.bukkit.plugin.Plugin;

public class Plugins {
	
	public static void load(Plugin plugin) {
		if (!CSCoreLib.isRegistered()) {
			System.out.println("[CS-CoreLib] Loading CS-CoreLib...");
			CSCoreLib.registerListeners(plugin);
			System.out.println("[CS-CoreLib] Successfully loaded!");
			System.out.println("[CS-CoreLib] CS-CoreLib initialized!");
			System.out.println("[CS-CoreLib] Internal Clock is now at " + Clock.getFormattedTime());
		}
		try {
			System.out.println("[" + plugin.getDescription().getName() + "] " + plugin.getDescription().getName() + " v" + plugin.getDescription().getVersion() + " has been enabled!");
			System.out.println("[CS-CoreLib] Successfully loaded Plugin \"" + plugin.getDescription().getName() + "\"");
		} catch(Exception x) {
			System.out.println("[CS-CoreLib] Failed to load Plugin \"" + plugin.getDescription().getName() + "\"");
			System.out.println("[CS-CoreLib] Disabled Plugin \"" + plugin.getDescription().getName() + "\"");
			plugin.getServer().getPluginManager().disablePlugin(plugin);
		}
	}
	
	public static void unload(Plugin plugin) {
		System.out.println("[" + plugin.getDescription().getName() + "] " + plugin.getDescription().getName() + " v" + plugin.getDescription().getVersion() + " has been disabled!");
		System.out.println("[CS-CoreLib] Successfully unloaded Plugin \"" + plugin.getDescription().getName() + "\"");
		System.out.println("[CS-CoreLib] Disabled Plugin \"" + plugin.getDescription().getName() + "\"");
	}

}
