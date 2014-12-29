package me.mrCookieSlime.CSCoreLib.general.Inventory;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class InvUtils {
	
	public static boolean fits(Inventory inv, ItemStack item) {
		Inventory inv2 = Bukkit.createInventory(null, inv.getSize());
		for (int i = 0; i < inv.getContents().length; i++) {
			inv2.setItem(i, inv.getContents()[i]);
		}
		return inv2.addItem(item).isEmpty();
	}
	
	public static boolean fits(Inventory inv, ItemStack item, int slot) {
		if (inv.getContents()[slot] == null) return true;
		else {
			ItemStack clone = inv.getContents()[slot].clone();
			int fits = (clone.getType().getMaxStackSize() - inv.getContents()[slot].getAmount());
			if (clone.getType() == item.getType() && clone.getDurability() == item.getDurability()) {
				if (clone.getItemMeta().toString().equalsIgnoreCase(item.getItemMeta().toString())) {
					if (fits >= item.getAmount()) {
						return true;
					}
					else return false;
				}
				else return false;
			}
			else return false;
		}
	}
	
	public static ItemStack decreaseItem(ItemStack item, int amount) {
		ItemStack clone = item.clone();
		if (amount < clone.getAmount()) clone.setAmount(clone.getAmount() - amount);
		else return null;
		return clone;
	}
	
	public static void removeItem(Inventory inv, ItemStack item, int Amount) {
		ItemStack[] contents = inv.getContents();
		
		 for (int i = 0; i < Amount; i++) {
			 for (int j = 0; j < contents.length; j++) {
				 if (contents[j] != null) {
					 if (contents[j].getType() == item.getType() && contents[j].getDurability() == item.getDurability()) {
						 if (contents[j].getAmount() > 1) {
							 contents[j].setAmount(contents[j].getAmount() - 1);
						 }
						 else {
							 inv.removeItem(contents[j]);
						 }
						 break;
					 }
				 }
			 }
		 }
	}

}
