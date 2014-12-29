package me.mrCookieSlime.CSCoreLib.general.Chat;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

public class CommandHelp {
	
	public static void sendCommandHelp(CommandSender p, Plugin plugin, List<String> commands, List<String> descriptions) {
		ChatColor label = Colors.getRandom();
		ChatColor info = Colors.getRandom();
		
		String authors = "";
		for (int i = 0; i < plugin.getDescription().getAuthors().size(); i++) {
			if (i > 0) {
				authors = authors + ", " + plugin.getDescription().getAuthors().get(i);
			}
			else {
				authors = plugin.getDescription().getAuthors().get(i);
			}
		}
		
		p.sendMessage("");
		p.sendMessage(label + plugin.getDescription().getName() + " v" + plugin.getDescription().getVersion() + " by " + authors);
		p.sendMessage("");
		for (int i = 0; i < commands.size(); i++) {
			p.sendMessage(label + commands.get(i) + " " + info + descriptions.get(i));
		}
		
	}

}
