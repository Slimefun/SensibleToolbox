package me.mrCookieSlime.CSCoreLib.general.Inventory.Item;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Dye;

public class DyedItem extends CustomItem {

	public DyedItem(Material type, DyeColor color) {
		super(new ItemStack(type), 1);
		Dye dye = new Dye();
		dye.setColor(color);
		setData(dye);
	}
	
	

}
