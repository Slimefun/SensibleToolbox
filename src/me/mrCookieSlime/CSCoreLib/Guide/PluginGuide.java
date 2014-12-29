package me.mrCookieSlime.CSCoreLib.Guide;

import me.mrCookieSlime.CSCoreLib.general.Inventory.Menu;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public class PluginGuide {
	
	String name;
	int durability;
	GuideItem guide;
	Menu menu;
	Plugin plugin;
	boolean onjoin;
	
	public PluginGuide(Plugin plugin, int id) {
		this.plugin = plugin;
		this.name = plugin.getDescription().getName();
		this.durability = id;
		this.guide = new GuideItem(plugin, id);
		this.menu = new Menu(this.name + " Guide", 0);
		this.onjoin = false;
		Guides.set(id, this);
	}
	
	public GuideItem getItem() {
		return this.guide;
	}
	
	public void setCustomName(String name) {
		this.guide = new GuideItem(name, this.durability);
	}
	
	public void setID(int id) {
		this.durability = id;
		this.guide = new GuideItem(plugin, id);
		refresh();
	}
	 
	public void give(Player p) {
		p.getInventory().addItem(getItem());
	}
	
	public void giveInHand(Player p) {
		p.setItemInHand(getItem());
	}
	
	public void give(Player p, int slot) {
		p.getInventory().setItem(slot, getItem());
	}
	
	public void setItem(int slot, ItemStack item) {
		this.menu.setItem(slot, item);
		refresh();
	}
	
	public void setGivenOnJoin(boolean give) {
		this.onjoin = give;
		refresh();
	}
	
	public boolean isGivenOnJoin(){
		return this.onjoin;
	}
	
	public Menu getMenu() {
		return this.menu;
	}
	
	public String getPluginName() {
		return this.plugin.getDescription().getName();
	}
	
	public void refresh() {
		Guides.refresh(durability, this);
	}
	
}
