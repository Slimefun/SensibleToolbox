package me.mrCookieSlime.CSCoreLib.events;

import me.mrCookieSlime.CSCoreLib.general.Inventory.Maps;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class MenuClickEvent extends Event {

	private static final HandlerList handlers = new HandlerList();
	
	Inventory inventory;
	int index;
	Player player;
	ItemStack item;
	int raw;
	 
	public HandlerList getHandlers() {
	    return handlers;
	}
	 
	public static HandlerList getHandlerList() {
	    return handlers;
	}
	
	public MenuClickEvent(Player player, Inventory inv, int index, ItemStack item, int rawSlot) {
		this.inventory = inv;
		this.index = index;
		this.player = player;
		this.item = item;
		this.raw = rawSlot;
	}
	
	public boolean hasKey() {
		return Maps.inv.containsKey(player.getUniqueId());
	}
	
	public String getKey() {
		if (hasKey()) {
			return Maps.inv.get(player.getUniqueId());
		}
		else {
			return null;
		}
	}
	
	public ItemStack getItem() {
		return this.item;
	}
	
	public Inventory getInventory() {
		return this.inventory;
	}
	
	public int getIndex() {
		return this.index;
	}
	
	public int getRawSlot() {
		return this.raw;
	}
	
	public Player getWhoClicked() {
		return this.player;
	}

}
