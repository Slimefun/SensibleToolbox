package me.mrCookieSlime.CSCoreLib.general.Inventory.Item;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class MenuItem extends ItemStack {
	
	public MenuItem(Material type, String name, int amount, int durability, String action) {
		super(type, amount);
		ItemMeta im = getItemMeta();
		im.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
		List<String> lore = new ArrayList<String>();
		lore.add("");
		lore.add(ChatColor.GREEN + "> Click to " + action);
		im.setLore(lore);
		setItemMeta(im);
		setDurability((short) durability);
	}
	
	public MenuItem(Material type, String name, int durability, String action) {
		super(type);
		ItemMeta im = getItemMeta();
		im.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
		List<String> lore = new ArrayList<String>();
		lore.add("");
		lore.add(ChatColor.GREEN + "> Click to " + action);
		im.setLore(lore);
		setItemMeta(im);
		setDurability((short) durability);
	}

}
