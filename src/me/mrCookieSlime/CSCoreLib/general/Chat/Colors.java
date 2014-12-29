package me.mrCookieSlime.CSCoreLib.general.Chat;

import java.util.Random;

import org.bukkit.ChatColor;

public class Colors {
	
	public static ChatColor[] colors = new ChatColor[]
	{
		ChatColor.YELLOW,
		ChatColor.GREEN,
		ChatColor.GOLD,
		ChatColor.AQUA,
		ChatColor.DARK_AQUA,
		ChatColor.DARK_BLUE
	};
	
	public static ChatColor getRandom() {
		return colors[new Random().nextInt(colors.length)];
	}

}
