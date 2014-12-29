package me.mrCookieSlime.CSCoreLib.general.Inventory.Item;

import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class NamedItem extends CustomItem {

	public NamedItem(ItemStack item, String name) {
		super(item, item.getAmount());
		ItemMeta im = getItemMeta();
		im.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
		setItemMeta(im);
	}
	
	

}
