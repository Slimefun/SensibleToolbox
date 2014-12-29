package me.mrCookieSlime.CSCoreLib.general.Inventory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.mrCookieSlime.CSCoreLib.general.Math.Calculator;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class Menu {
	
	Inventory inv;
	List<ItemStack> contents;
	Map<Integer, ItemStack> map;
	String name;
	
	public Menu(String name, List<ItemStack> items) {
		this.name = name;
		this.inv = Bukkit.createInventory(null, Calculator.formToLine(items.size()) * 9, ChatColor.translateAlternateColorCodes('&', name));
		this.contents = items;
		this.map = new HashMap<Integer, ItemStack>();
		for (int i = 0; i < this.inv.getSize(); i++) {
			if ((items.size() - 1) < i) {
				this.inv.setItem(i, new ItemStack(Material.AIR));
				this.map.put(i, new ItemStack(Material.AIR));
			}
			else {
				this.inv.setItem(i, items.get(i));
				this.map.put(i, items.get(i));
			}
		}
	}
	
	public Menu(String name, ItemStack[] items) {
		this.name = name;
		this.inv = Bukkit.createInventory(null, Calculator.formToLine(items.length) * 9, ChatColor.translateAlternateColorCodes('&', name));
		List<ItemStack> list = new ArrayList<ItemStack>();
		for (ItemStack item: items) {
			list.add(item);
		}
		this.contents = list;
		this.map = new HashMap<Integer, ItemStack>();
		for (int i = 0; i < this.inv.getSize(); i++) {
			if ((items.length - 1) < i) {
				this.inv.setItem(i, new ItemStack(Material.AIR));
				this.map.put(i, new ItemStack(Material.AIR));
			}
			else {
				this.inv.setItem(i, items[i]);
				this.map.put(i, items[i]);
			}
		}
	}
	
	public Menu(String name, int size) {
		this.name = name;
		this.inv = Bukkit.createInventory(null, size, ChatColor.translateAlternateColorCodes('&', name));
		this.contents = new ArrayList<ItemStack>();
		this.map = new HashMap<Integer, ItemStack>();
		for (int i = 0; i < this.inv.getSize(); i++) {
			this.inv.setItem(i, new ItemStack(Material.AIR));
			this.map.put(i, new ItemStack(Material.AIR));
		}
	}
	
	public boolean hasKey(Player p) {
		return Maps.inv.containsKey(p.getUniqueId());
	}
	
	public void setSize(int size) {
		this.inv = Bukkit.createInventory(null, size, ChatColor.translateAlternateColorCodes('&', this.name));
		for (int i = 0; i < size; i++) {
			if ((this.contents.size() - 1) < i) {
				this.inv.setItem(i, new ItemStack(Material.AIR));
				this.map.put(i, new ItemStack(Material.AIR));
			}
		}
	}
	
	public Inventory getInventory() {
		return this.inv;
	}
	
	public Map<Integer, ItemStack> toHashMap() {
		return this.map;
	}
	
	public List<ItemStack> getContents() {
		return this.contents;
	}
	
	public String getName() {
		return this.name;
	}
	
	public void open(Player p) {
		p.openInventory(this.inv);
	}
	
	public void open(Player p, String key) {
		p.openInventory(this.inv);
		Maps.inv.put(p.getUniqueId(), key);
	}
	
	public void openIndividually(Player p, List<ItemStack> items) {
		Inventory inv = Bukkit.createInventory(null, Calculator.formToLine(items.size()) * 9, ChatColor.translateAlternateColorCodes('&', this.name));
		for (int i = 0; i < items.size(); i++) {
			inv.setItem(i, items.get(i));
		}
		p.openInventory(inv);
	}
	
	public void openIndividually(Player p, List<ItemStack> items, String key) {
		openIndividually(p, items);
		Maps.inv.put(p.getUniqueId(), key);
	}
	
	public void setItem(int slot, ItemStack item) {
		this.contents.set(slot, item);
		this.inv.setItem(slot, item);
		this.map.put(slot, item);
	}

}
