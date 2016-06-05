package me.desht.dhutils;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class Debugger {
	private static final String DEBUG_COLOR = ChatColor.DARK_GREEN.toString();
	private static Debugger instance;
	private int level;
	private CommandSender target;
	private String prefix = "";

	private Debugger() {
		level = 0;
	}

	public static synchronized Debugger getInstance() {
		if (instance == null) {
			instance = new Debugger();
		}
		return instance;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public CommandSender getTarget() {
		return target;
	}

	public void setTarget(CommandSender target) {
		this.target = target;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public void debug(String message) {
			debug(1, message);
	}

	public void debug(int msgLevel, String message) {
		if (msgLevel <= level && target != null) {
			target.sendMessage(DEBUG_COLOR + prefix + message);
		}
	}

	@Override
	public Debugger clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}

}
