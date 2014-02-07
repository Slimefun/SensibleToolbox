package me.desht.sensibletoolbox.api;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Represents how a STB block reacts to the presence or absence of a redstone signal.
 */
public enum RedstoneBehaviour {
	IGNORE(Material.SULPHUR, "Ignore Redstone"),
	HIGH(Material.REDSTONE, "Require Signal"),
	LOW(Material.GLOWSTONE_DUST, "Require No Signal");
	private final Material material;
	private final String label;

	RedstoneBehaviour(Material mat, String label) {
		this.material = mat;
		this.label = label;
	}

	public ItemStack getTexture() {
		ItemStack res = new ItemStack(material);
		ItemMeta meta = res.getItemMeta();
		meta.setDisplayName(ChatColor.WHITE.toString() + ChatColor.UNDERLINE + label);
		res.setItemMeta(meta);
		return res;
	}
}
