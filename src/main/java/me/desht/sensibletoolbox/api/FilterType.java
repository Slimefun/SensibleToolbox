package me.desht.sensibletoolbox.api;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public enum FilterType {
	MATERIAL(Material.STONE, "Match material only"),
	BLOCK_DATA(Material.IRON_SWORD, "Match material & block data"),
	ITEM_META(Material.ENCHANTED_BOOK, "Match material, block data & metadata");

	private final String label;
	private final Material mat;

	private FilterType(Material mat, String label) {
		this.mat = mat;
		this.label = label;
	}

	public String getLabel() {
		return label;
	}

	public ItemStack getTexture() {
		ItemStack res = new ItemStack(mat);
		ItemMeta meta = res.getItemMeta();
		meta.setDisplayName(ChatColor.WHITE.toString() + ChatColor.UNDERLINE + label);
		res.setItemMeta(meta);
		return res;
	}
}
