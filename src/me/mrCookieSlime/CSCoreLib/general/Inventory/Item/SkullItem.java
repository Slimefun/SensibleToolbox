package me.mrCookieSlime.CSCoreLib.general.Inventory.Item;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

public class SkullItem extends ItemStack {
	
	String owner;
	
	public SkullItem(String name, String owner) {
		super(new ItemStack(Material.SKULL_ITEM));
		setDurability((short) 3);
		ItemMeta im = getItemMeta();
		im.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
		((SkullMeta) im).setOwner(owner);
		setItemMeta(im);
		this.owner = owner;
	}
	
	public SkullItem(String owner) {
		super(new ItemStack(Material.SKULL_ITEM));
		setDurability((short) 3);
		ItemMeta im = getItemMeta();
		((SkullMeta) im).setOwner(owner);
		setItemMeta(im);
		this.owner = owner;
	}
	
	public SkullItem(String name, String owner, List<String> lore) {
		super(new ItemStack(Material.SKULL_ITEM));
		setDurability((short) 3);
		ItemMeta im = getItemMeta();
		im.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
		im.setLore(lore);
		((SkullMeta) im).setOwner(owner);
		setItemMeta(im);
		this.owner = owner;
	}
	
	public String getOwner() {
		return this.owner;
	}

}
