package me.mrCookieSlime.CSCoreLib.general.Player;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class Players {
	
	public static boolean isOnline(String name) {
		boolean online = false;
		
		for (Player p: Bukkit.getOnlinePlayers()) {
			if (p.getName().equalsIgnoreCase(name)) {
				online = true;
				break;
			}
		}
		
		return online;
	}
}
