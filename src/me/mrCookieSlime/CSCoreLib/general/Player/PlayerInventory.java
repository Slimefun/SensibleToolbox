package me.mrCookieSlime.CSCoreLib.general.Player;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class PlayerInventory {
	
	public static void consumeItemInHand(Player p) {
		if (p.getItemInHand().getAmount() != 1) {
  		  	if (p.getGameMode() != GameMode.CREATIVE) {
  		  		ItemStack item = p.getItemInHand().clone();
  		  		item.setAmount(item.getAmount() - 1);
  		  		p.setItemInHand(item);
  		  	}
  	  	}
  	  	else if (p.getGameMode() != GameMode.CREATIVE) {
  	  		p.setItemInHand(null);
  	  	}
	}
	
	public static void removeItemIgnoringMetaAndDamage(Player p, Material Item, int Amount) {
		ItemStack[] contents = p.getInventory().getContents();
		
		 for (int i = 0; i < Amount; i++) {
			 for (int j = 0; j < contents.length; j++) {
				 if (contents[j] != null) {
					 if (contents[j].getType() == Item) {
						 if (contents[j].getAmount() > 1) {
							 contents[j].setAmount(contents[j].getAmount() - 1);
						 }
						 else {
							 p.getInventory().removeItem(contents[j]);
						 }
						 break;
					 }
				 }
			 }
		 }
	}
	
	public static void removeItemIgnoringMeta(Player p, Material Item, int Amount) {
		ItemStack[] contents = p.getInventory().getContents();
		
		 for (int i = 0; i < Amount; i++) {
			 for (int j = 0; j < contents.length; j++) {
				 if (contents[j] != null) {
					 if (contents[j].getType() == Item && contents[j].getDurability() == 0) {
						 if (contents[j].getAmount() > 1) {
							 contents[j].setAmount(contents[j].getAmount() - 1);
						 }
						 else {
							 p.getInventory().removeItem(contents[j]);
						 }
						 break;
					 }
				 }
			 }
		 }
	}
	
	public static void removeItemIgnoringMeta(Player p, Material Item, int Amount, int durability) {
		ItemStack[] contents = p.getInventory().getContents();
		
		 for (int i = 0; i < Amount; i++) {
			 for (int j = 0; j < contents.length; j++) {
				 if (contents[j] != null) {
					 if (contents[j].getType() == Item && contents[j].getDurability() == durability) {
						 if (contents[j].getAmount() > 1) {
							 contents[j].setAmount(contents[j].getAmount() - 1);
						 }
						 else {
							 p.getInventory().removeItem(contents[j]);
						 }
						 break;
					 }
				 }
			 }
		 }
	}
	
	public static void removeItem(Player p, ItemStack item, int Amount) {
		ItemStack[] contents = p.getInventory().getContents();
		
		 for (int i = 0; i < Amount; i++) {
			 for (int j = 0; j < contents.length; j++) {
				 if (contents[j] != null) {
					 if (contents[j].getType() == item.getType() && contents[j].getDurability() == item.getDurability()) {
						 if (contents[j].getAmount() > 1) {
							 contents[j].setAmount(contents[j].getAmount() - 1);
						 }
						 else {
							 p.getInventory().removeItem(contents[j]);
						 }
						 break;
					 }
				 }
			 }
		 }
	}
	
	@SuppressWarnings("deprecation")
	public static void update(Player p) {
		 p.updateInventory();
	}
	
	public static void damageItemInHand(Player p) {
		if (p.getGameMode() != GameMode.CREATIVE) {
			ItemStack item = p.getItemInHand().clone();
			
			if (item.getDurability() >= item.getType().getMaxDurability()) item.setAmount(0);
			else item.setDurability((short) (item.getDurability() + 1));
			p.setItemInHand(item);
		}
	}
	
	public static void giveItem(Player p, ItemStack item) {
		Item i = p.getWorld().dropItem(p.getLocation(), item);
		i.setPickupDelay(0);
	}

}
