package me.mrCookieSlime.CSCoreLib.minigame;

import org.bukkit.ChatColor;

public enum GameState {
	
	LOBBY(ChatColor.translateAlternateColorCodes('&', "&a&lLobby")),
	INGAME(ChatColor.translateAlternateColorCodes('&', "&aRunning")),
	VIP(ChatColor.translateAlternateColorCodes('&', "&6&lDonators Only")),
	FULL(ChatColor.translateAlternateColorCodes('&', "&4&lFULL"));
	
	String name;
	
	public String toTitle() {
		return name;
	}
	
	GameState(String title) {
		this.name = title;
	}
}
