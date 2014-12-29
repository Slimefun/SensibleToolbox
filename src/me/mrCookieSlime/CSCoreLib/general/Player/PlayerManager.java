package me.mrCookieSlime.CSCoreLib.general.Player;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

public class PlayerManager {
	
	public static void reset(Player p) {
		p.setGameMode(GameMode.SURVIVAL);
		p.setHealth(20);
		p.setFoodLevel(20);
		
		clearEffects(p);
		p.setExp(0);
		p.setLevel(0);
		
		p.getInventory().clear();
		p.getInventory().setArmorContents(null);
		
		PlayerInventory.update(p);
	}
	
	public static void clearEffects(Player p) {
		for (PotionEffect e : p.getActivePotionEffects()) {
			p.removePotionEffect(e.getType());
		}
	}
	
	public static void loseHunger(Player p, int amount) {
		if (p.getGameMode() != GameMode.CREATIVE) {
			int starve = p.getFoodLevel() - amount;
			if (starve < 0) starve = 0;
			p.setFoodLevel(starve);
		}
	}
	
}
