package me.mrCookieSlime.CSCoreLib.general.Inventory.Item;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class CustomItem extends ItemStack {
	
	public CustomItem(Material type, String name, int durability, List<String> lore) {
		super(new ItemStack(type));
		ItemMeta im = getItemMeta();
		im.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
		im.setLore(lore);
		setItemMeta(im);
		setDurability((short) durability);
	}
	
	public CustomItem(Material type, String name, int durability, String[] lore) {
		super(new ItemStack(type));
		ItemMeta im = getItemMeta();
		im.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
		List<String> lines = new ArrayList<String>();
		for (String line: lore) {
			lines.add(ChatColor.translateAlternateColorCodes('&', line));
		}
		im.setLore(lines);
		setItemMeta(im);
		setDurability((short) durability);
	}
	
	public CustomItem(Material type, String name, int durability, String[] lore, String[] enchantments) {
		super(new ItemStack(type));
		ItemMeta im = getItemMeta();
		im.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
		List<String> lines = new ArrayList<String>();
		for (String line: lore) {
			lines.add(ChatColor.translateAlternateColorCodes('&', line));
		}
		im.setLore(lines);
		setItemMeta(im);
		setDurability((short) durability);
		for (String ench: enchantments) {
			addUnsafeEnchantment(Enchantment.getByName(ench.split("-")[0]), Integer.parseInt(ench.split("-")[1]));
		}
	}
	
	public CustomItem(ItemStack item, String[] enchantments) {
		super(item);
		for (String ench: enchantments) {
			addUnsafeEnchantment(Enchantment.getByName(ench.split("-")[0]), Integer.parseInt(ench.split("-")[1]));
		}
	}
	
	public CustomItem(Material type, String name, String[] enchantments, int durability) {
		super(new ItemStack(type));
		ItemMeta im = getItemMeta();
		im.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
		setItemMeta(im);
		setDurability((short) durability);
		for (String ench: enchantments) {
			addUnsafeEnchantment(Enchantment.getByName(ench.split("-")[0]), Integer.parseInt(ench.split("-")[1]));
		}
	}
	
	public CustomItem(Material type, String name, int durability) {
		super(new ItemStack(type));
		ItemMeta im = getItemMeta();
		im.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
		setItemMeta(im);
		setDurability((short) durability);
	}
	
	public CustomItem(Material type, int durability) {
		super(new ItemStack(type));
		setDurability((short) durability);
	}
	
	public CustomItem(Material type, int durability, int amount) {
		super(new ItemStack(type, amount));
		setDurability((short) durability);
	}
	
	public CustomItem(ItemStack item, int amount) {
		super(item.clone());
		setAmount(amount);
	}
	
	public CustomItem(Material type, String name, int durability, int amount, List<String> lore) {
		super(new ItemStack(type, amount));
		ItemMeta im = getItemMeta();
		im.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
		im.setLore(lore);
		setItemMeta(im);
		setDurability((short) durability);
	}

}
