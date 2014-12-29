package me.mrCookieSlime.CSCoreLib.general.Inventory.Item;

import org.bukkit.Material;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class CustomPotion extends CustomItem {

	public CustomPotion(String name, int durability, String[] lore, PotionEffect effect) {
		super(Material.POTION, name, durability, lore);
		PotionMeta meta = (PotionMeta) getItemMeta();
		meta.setMainEffect(PotionEffectType.SATURATION);
		meta.addCustomEffect(effect, true);
		setItemMeta(meta);
	}

}
