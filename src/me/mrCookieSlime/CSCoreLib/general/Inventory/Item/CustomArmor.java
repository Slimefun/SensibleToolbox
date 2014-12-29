package me.mrCookieSlime.CSCoreLib.general.Inventory.Item;

import org.bukkit.Color;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;

public class CustomArmor extends ItemStack {
	
	public CustomArmor(ItemStack item, Color color) {
		super(item.clone());
		ItemMeta im = getItemMeta();
		((LeatherArmorMeta) im).setColor(color);
		setItemMeta(im);
	}

}
