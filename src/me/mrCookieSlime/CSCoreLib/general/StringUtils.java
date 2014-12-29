package me.mrCookieSlime.CSCoreLib.general;

import org.bukkit.inventory.ItemStack;

public class StringUtils {
	
	public static String formatItemName(ItemStack item, boolean includePlural) {
		String name = item.getType().toString().toLowerCase().replace("_", " ");
		if (includePlural) {
			name = Character.toUpperCase(name.charAt(0)) + name.substring(1) + "/s ";
			name = item.getAmount() + " " + name;
		}
		else {
			if (item.hasItemMeta()) {
				if (item.getItemMeta().hasDisplayName()) name = item.getItemMeta().getDisplayName();
			}
			else name = Character.toUpperCase(name.charAt(0)) + name.substring(1);
		}
		return name;
	}
	
	public static String format(String string) {
		return Character.toUpperCase(string.toLowerCase().replace("_", " ").charAt(0)) + string.toLowerCase().replace("_", " ").substring(1);
	}
	
	public static boolean contains(String string, String... contain) {
		for (String s: contain) {
			if (string.contains(s)) return true;
		}
		return false;
	}
	
	public static boolean equals(String string, String... equal) {
		for (String s: equal) {
			if (string.equals(s)) return true;
		}
		return false;
	}
	
	public static boolean endsWith(String string, String... end) {
		for (String s: end) {
			if (string.endsWith(s)) return true;
		}
		return false;
	}

}
