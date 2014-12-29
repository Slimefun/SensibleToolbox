package me.mrCookieSlime.CSCoreLib.Guide;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

public class GuideItem extends ItemStack {
	
	public GuideItem(Plugin plugin, int id) {
		super(new ItemStack(Material.ENCHANTED_BOOK));
		setDurability((short) id);
		ItemMeta im = getItemMeta();
		im.setDisplayName(ChatColor.AQUA + plugin.getDescription().getName() + " Guide " + ChatColor.GRAY + "(Right Click)");
		List<String> lore = new ArrayList<String>();
		lore.add("");
		lore.add(ChatColor.GREEN + "This is your basic Guide for " + ChatColor.DARK_GREEN + plugin.getDescription().getName());
		lore.add(ChatColor.GREEN + "You can see all Items added by this Plugin");
		lore.add(ChatColor.GREEN + "in here (even their Recipes)");
		lore.add(ChatColor.GREEN + "Click on an Item for more Info");
		im.setLore(lore);
		setItemMeta(im);
	}
	
	public GuideItem(String name, int id) {
		super(new ItemStack(Material.ENCHANTED_BOOK));
		setDurability((short) id);
		ItemMeta im = getItemMeta();
		im.setDisplayName(ChatColor.AQUA + name + " Guide " + ChatColor.GRAY + "(Right Click)");
		List<String> lore = new ArrayList<String>();
		lore.add("");
		lore.add(ChatColor.GREEN + "This is your basic Guide for " + name);
		lore.add(ChatColor.GREEN + "You can see all " + name + " in here");
		lore.add(ChatColor.GREEN + "Click on an Item for more Info");
		im.setLore(lore);
		setItemMeta(im);
	}
	
	public String getName() {
		return this.getItemMeta().getDisplayName();
	}

}
