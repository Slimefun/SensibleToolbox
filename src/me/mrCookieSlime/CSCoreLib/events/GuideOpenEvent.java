package me.mrCookieSlime.CSCoreLib.events;

import me.mrCookieSlime.CSCoreLib.Guide.PluginGuide;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class GuideOpenEvent extends Event {

	private static final HandlerList handlers = new HandlerList();
	
	PluginGuide guide;
	Player player;
	 
	public HandlerList getHandlers() {
	    return handlers;
	}
	 
	public static HandlerList getHandlerList() {
	    return handlers;
	}
	
	public GuideOpenEvent(Player player, PluginGuide guide) {
		this.player = player;
		this.guide = guide;
	}
	
	public PluginGuide getGuide() {
		return this.guide;
	}
	
	public Player getPlayer() {
		return this.player;
	}

}
