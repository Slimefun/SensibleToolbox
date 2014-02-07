package me.desht.sensibletoolbox.api;

import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.Wool;

/**
 * Represents the user-based access control in force for this machine.
 */
public enum AccessControl {
	PUBLIC(DyeColor.GREEN, "Public Access"),
	PRIVATE(DyeColor.RED, "Owner Only Access");
	private final String label;
	private final DyeColor color;

	AccessControl(DyeColor color, String label) {
		this.color = color;
		this.label = label;
	}

	public ItemStack getTexture() {
		ItemStack res = new Wool(color).toItemStack(1);
		ItemMeta meta = res.getItemMeta();
		meta.setDisplayName(ChatColor.WHITE.toString() + ChatColor.UNDERLINE + label);
		res.setItemMeta(meta);
		return res;
	}
}
