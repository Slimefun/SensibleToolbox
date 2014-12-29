package me.mrCookieSlime.CSCoreLib.Configuration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class Localization {
	
	File file;
	Config config;
	
	public Localization(Plugin plugin) {
		this.file = new File("plugins/" + plugin.getDescription().getName().replace(" ", "_") + "/messages.yml");
		this.config = new Config(file);
	}
	
	public void setDefault(String key, String message) {
		if (!config.contains(key)) {
			config.setValue(key, message);
		}
	}
	
	public void setDefault(String key, String[] messages) {
		List<String> strings = new ArrayList<String>();
		for (String string: messages) {
			strings.add(string);
		}
		if (!config.contains(key)) {
			config.setValue(key, strings);
		}
	}
	
	public String getTranslation(String input) {
		return ChatColor.translateAlternateColorCodes('&', config.getString(input));
	}
	
	public void sendTranslation(Player p, String prefix, String input) {
		p.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix) + getTranslation(input));
	}
	
	public void sendTranslation(Player p, String message) {
		p.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
	}
	
	public String getRandomTranslation(String input) {
		return ChatColor.translateAlternateColorCodes('&', config.getRandomStringfromList(input));
	}

}
