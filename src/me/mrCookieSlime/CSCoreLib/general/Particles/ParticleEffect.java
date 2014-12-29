package me.mrCookieSlime.CSCoreLib.general.Particles;

import java.lang.reflect.Constructor;

import me.mrCookieSlime.CSCoreLib.general.Reflection.ReflectionUtils;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public enum ParticleEffect {
	
	HUGE_EXPLOSION("hugeexplosion"), 
	LARGE_EXPLODE("largeexplode"), 
	FIREWORKS_SPARK("fireworksSpark"), 
	BUBBLE("bubble"), 
	SUSPEND("suspend"), 
	DEPTH_SUSPEND("depthSuspend"), 
	TOWN_AURA("townaura"), 
	CRIT("crit"), 
	MAGIC_CRIT("magicCrit"), 
 	MOB_SPELL("mobSpell"), 
 	MOB_SPELL_AMBIENT("mobSpellAmbient"), 
 	SPELL("spell"), 
 	INSTANT_SPELL("instantSpell"), 
 	WITCH_MAGIC("witchMagic"), 
 	NOTE("note"), 
 	PORTAL("portal"), 
 	ENCHANTMENT_TABLE("enchantmenttable"), 
	EXPLODE("explode"), 
	FLAME("flame"), 
	LAVA("lava"), 
	FOOTSTEP("footstep"), 
	SPLASH("splash"), 
	LARGE_SMOKE("largesmoke"), 
	CLOUD("cloud"), 
	RED_DUST("reddust"), 
	SNOWBALL_POOF("snowballpoof"), 
	DRIP_WATER("dripWater"), 
	DRIP_LAVA("dripLava"), 
	SNOW_SHOVEL("snowshovel"), 
	SLIME("slime"), 
	HEART("heart"), 
	ANGRY_VILLAGER("angryVillager"), 
	HAPPY_VILLAGER("happyVillager");
	
	String name;
	
	private ParticleEffect(String name) {
		this.name = name;
	}
	
	public String getName() {
		return this.name;
	}
	
	public static Object initializePacket(String name, Location location, float offsetX, float offsetY, float offsetZ, float speed, int amount) {
		try {
			Constructor<?> constructor = ReflectionUtils.getConstructor(ReflectionUtils.getClass("PacketPlayOutWorldParticles"), new Class[0]);
			Object p = constructor.newInstance(new Object[0]);
			ReflectionUtils.setValue(p, "a", name);
			ReflectionUtils.setValue(p, "b", Float.valueOf((float) location.getX()));
			ReflectionUtils.setValue(p, "c", Float.valueOf((float) location.getY()));
			ReflectionUtils.setValue(p, "d", Float.valueOf((float) location.getZ()));
			ReflectionUtils.setValue(p, "e", Float.valueOf(offsetX));
			ReflectionUtils.setValue(p, "f", Float.valueOf(offsetY));
			ReflectionUtils.setValue(p, "g", Float.valueOf(offsetZ));
			ReflectionUtils.setValue(p, "h", Float.valueOf(speed));
			ReflectionUtils.setValue(p, "i", Integer.valueOf(amount));
			
			return p;
		} catch (Exception e) {
			System.err.println("An Error occured while creating the Packet for the Particle Effect \"" + name + "\"");
			e.printStackTrace();
			return null;
		}
	}
	
	public static void display(ParticleEffect effect, Location location, float offsetX, float offsetY, float offsetZ, float speed, int amount, Player... players) {
		Object packet = initializePacket(effect.getName(), location, offsetX, offsetY, offsetZ, speed, amount);
		for (Player p: players) {
			try {
				Object entity = ReflectionUtils.getMethod(p.getClass(), "getHandle").invoke(p, new Object[0]);
				Object connection = ReflectionUtils.getValue(entity, "playerConnection");
				ReflectionUtils.getMethod(connection.getClass(), "sendPacket", ReflectionUtils.toPrimitiveTypeArray(new Object[] {packet})).invoke(connection, new Object[] {packet});
			} catch (Exception e) {
				System.err.println("An Error occured while sending the Packet for the Particle Effect \"" + effect.getName() + "\"");
				e.printStackTrace();
			}
		}
	}
}
