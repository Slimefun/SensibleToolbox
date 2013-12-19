package me.desht.sensibletoolbox.listeners;

import me.desht.sensibletoolbox.SensibleToolboxPlugin;
import org.bukkit.event.Listener;

public abstract class STBBaseListener implements Listener {
	protected SensibleToolboxPlugin plugin;

	public STBBaseListener(SensibleToolboxPlugin plugin) {
		this.plugin = plugin;
	}
}
