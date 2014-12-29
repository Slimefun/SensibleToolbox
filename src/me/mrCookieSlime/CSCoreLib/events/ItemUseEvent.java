package me.mrCookieSlime.CSCoreLib.events;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

public class ItemUseEvent extends Event implements Cancellable {
	
	private static final HandlerList handlers = new HandlerList();
	
	Player p;
	ItemStack i;
	Block b;
	boolean cancelled;
	
	public HandlerList getHandlers() {
	    return handlers;
	}
	 
	public static HandlerList getHandlerList() {
	    return handlers;
	}
	
	public ItemUseEvent(Player p, ItemStack item, Block clicked) {
		this.p = p;
		this.i = item;
		this.b = clicked;
	}
	
	public Player getPlayer() {
		return this.p;
	}
	
	public ItemStack getItem() {
		return this.i;
	}
	
	public Block getClickedBlock() {
		return this.b;
	}

	@Override
	public boolean isCancelled() {
		return this.cancelled;
	}

	@Override
	public void setCancelled(boolean cancel) {
		this.cancelled = cancel;
	}

}
