package me.mrCookieSlime.CSCoreLib.general;

import java.util.Random;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;

public class FireworkShow {
	
	
	public static void launchRandom(Player p, int amount) {
		for (int i = 0; i < amount; i++) {
			Location l = p.getLocation().clone();
			
			l.setX(l.getX() + new Random().nextInt(amount));
			l.setX(l.getX() - new Random().nextInt(amount));
			l.setZ(l.getZ() + new Random().nextInt(amount));
			l.setZ(l.getZ() - new Random().nextInt(amount));
			
			Firework fw = (Firework)p.getWorld().spawnEntity(p.getLocation(), EntityType.FIREWORK);
			
		       FireworkMeta fwm = fw.getFireworkMeta();

		       Random r = new Random();

		       int rt = r.nextInt(3) + 1;
		       FireworkEffect.Type type = FireworkEffect.Type.BALL;
		       if (rt == 1) type = FireworkEffect.Type.BALL;
		       if (rt == 2) type = FireworkEffect.Type.BALL_LARGE;

		       int r1i = r.nextInt(15) + 1;
		       int r2i = r.nextInt(15) + 1;
		       Color c1 = getColor(r1i);
		       Color c2 = getColor(r2i);

		       FireworkEffect effect = FireworkEffect.builder().flicker(r.nextBoolean()).withColor(c1).withFade(c2).with(type).trail(r.nextBoolean()).build();

		       fwm.addEffect(effect);

		       int rp = r.nextInt(2) + 1;
		       fwm.setPower(rp);

		       fw.setFireworkMeta(fwm);
		}
	}
	
	public static Color getColor(int r) {
 	   if (r == 1) {
 		   return Color.AQUA;
 		   }
 	   if (r == 2) {
 		   return Color.BLACK;
 		   }
 	   if (r == 3) {
 		   return Color.BLUE;
 		   }
 	   if (r == 4) {
 		   return Color.FUCHSIA;
 		   }
 	   if (r == 5) {
 		   return Color.GRAY;
 		   }
 	   if (r == 6) {
 		   return Color.GREEN;
 		   }
 	   if (r == 7) {
 		   return Color.LIME;
 		   }
 	   if (r == 8) {
 		   return Color.MAROON;
 		   }
 	   if (r == 9) {
 		   return Color.NAVY;
 		   }
 	   if (r == 10) {
 		   return Color.OLIVE;
 		   }
 	   if (r == 11) {
 		   return Color.ORANGE;
 		   }
 	   if (r == 12) {
 		   return Color.PURPLE;
 		   }
 	   if (r == 13) {
 		   return Color.RED;
 		   }
 	   if (r == 14) {
 		   return Color.SILVER;
 		   }
 	   if (r == 15) {
 		   return Color.TEAL;
 		   }
 	   if (r == 16) {
 		   return Color.YELLOW;
 		   }
 	   return null;
  
}

}
