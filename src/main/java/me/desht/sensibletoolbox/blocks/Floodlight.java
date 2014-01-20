package me.desht.sensibletoolbox.blocks;

import me.desht.dhutils.Debugger;
import me.desht.dhutils.cuboid.Cuboid;
import me.desht.dhutils.nms.NMSHelper;
import me.desht.sensibletoolbox.SensibleToolboxPlugin;
import me.desht.sensibletoolbox.storage.LocationManager;
import me.desht.sensibletoolbox.util.STBUtil;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.material.Colorable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class Floodlight extends BaseSTBBlock implements Colorable {
	// stop monsters spawning within this range
	public static final int INTERDICTION_RANGE = 16;
	// lighting radius (over and above the default 8)
	private static final int LIGHTING_RADIUS = 6;
	public static final String STB_FLOODLIGHT_FLAME = "STB_Floodlight_Flame";
	private DyeColor color;
	//	private Item flame;

	public Floodlight() {
		color = DyeColor.WHITE;
	}

	public Floodlight(ConfigurationSection conf) {
		color = DyeColor.valueOf(conf.getString("color", "WHITE"));
	}

	public YamlConfiguration freeze() {
		YamlConfiguration conf = super.freeze();
		conf.set("color", color.toString());
		return conf;
	}

	public DyeColor getColor() {
		return color;
	}

	public void setColor(DyeColor color) {
		this.color = color;
	}

	@Override
	public Material getBaseMaterial() {
		return Material.STAINED_GLASS;
	}

	@Override
	public Byte getBaseBlockData() {
		return color.getWoolData();
	}

	@Override
	public String getItemName() {
		return "Floodlight";
	}

	@Override
	public String[] getLore() {
		return new String[] {
				"Lights up a larger area than torches",
				"Also Prevents monsters spawning",
				" in a 16-block radius"
		};
	}

	@Override
	public Recipe getRecipe() {
		ShapedRecipe recipe = new ShapedRecipe(toItemStack(1));
		recipe.shape("GDG", "TLT", " T ");
		recipe.setIngredient('G', Material.GOLD_INGOT);
		recipe.setIngredient('D', Material.DIAMOND);
		recipe.setIngredient('T', Material.TORCH);
		recipe.setIngredient('L', Material.REDSTONE_LAMP_OFF);
		return recipe;
	}

	@Override
	public void setLocation(Location loc) {
		if (loc == null) {
			SensibleToolboxPlugin.getInstance().getFloodlightListener().unregisterFloodlight(this);
			removeLighting(getLocation());
//			if (flame != null) {
//				flame.remove();
//				flame = null;
//			}
		}
		super.setLocation(loc);
		if (loc != null) {
			SensibleToolboxPlugin.getInstance().getFloodlightListener().registerFloodlight(this);
			addLighting(getLocation());
//			if (flame != null) {
//				flame.remove();
//			}
//			addFlame();
		}
	}

//	public void addFlame() {
//		Location spawnLoc = getLocation().clone();
//		spawnLoc.add(0.5, 0.2, 0.5);
//		flame = spawnLoc.getWorld().dropItem(spawnLoc, new ItemStack(Material.FIRE));
//		flame.setPickupDelay(Integer.MAX_VALUE);
//		flame.setMetadata(STB_FLOODLIGHT_FLAME, new FixedMetadataValue(SensibleToolboxPlugin.getInstance(), this));
//	}

	private void removeLighting(Location loc) {
		if (SensibleToolboxPlugin.getInstance().isNMSenabled()) {
			Debugger.getInstance().debug(2, "reset block lighting @ " + loc);
			List<Floodlight> otherLights = new ArrayList<Floodlight>();
			Location loc2 = loc.clone();
			final Cuboid c = new Cuboid(loc).outset(Cuboid.CuboidDirection.Both, 8);
			for (Block b : c) {
				// if there are other lights within the affected area, we need to re-light them
				Floodlight other = LocationManager.getManager().get(b.getLocation(loc2), Floodlight.class);
				if (other != null && other != this) {
					Debugger.getInstance().debug(2, "found other light @ " + other.getLocation());
					otherLights.add(other);
				}
				NMSHelper.getNMS().recalculateBlockLighting(b.getWorld(), b.getX(), b.getY(), b.getZ());
			}

			long delay = 1;
			for (final Floodlight other: otherLights) {
				Bukkit.getScheduler().runTaskLater(SensibleToolboxPlugin.getInstance(), new Runnable() {
					@Override
					public void run() {
						addLighting(other.getLocation());
					}
				}, delay++);
			}

			for (Chunk ch : c.getChunks()) {
				ch.getWorld().refreshChunk(ch.getX(), ch.getZ());
			}
		}
	}

	private void addLighting(Location loc) {
		if (SensibleToolboxPlugin.getInstance().isNMSenabled()) {
			Debugger.getInstance().debug(2, "force block light @ " + loc);
			Block b = loc.getBlock();
			Cuboid c = new Cuboid(loc).outset(Cuboid.CuboidDirection.Both, 1);
			for (Block corner : c.corners()) {
				iterateLight(b, corner);
			}
			for (BlockFace face : STBUtil.directFaces) {
				iterateLight(b, b.getRelative(face));
			}
			NMSHelper.getNMS().forceBlockLightLevel(b.getWorld(), b.getX(), b.getY(), b.getZ(), 15);
			for (Chunk ch : c.getChunks()) {
				ch.getWorld().refreshChunk(ch.getX(), ch.getZ());
			}
		}
	}

	private void iterateLight(Block origin, Block candidate) {
		Vector vOrigin = origin.getLocation().toVector();
		Vector vDir = candidate.getLocation().toVector().subtract(vOrigin);
		Location loc = origin.getLocation();
		for (int i = 0; i < LIGHTING_RADIUS; i++) {
			Location loc2 = loc.clone().add(vDir);
			Block b1 = loc2.getBlock();
			if (!b1.isEmpty() && !b1.isLiquid()) {
				break;
			}
			loc = loc2;
		}
		Block b = loc.getBlock();
		if (!loc.equals(origin.getLocation())) {
			b.setType(Material.GLOWSTONE);
			NMSHelper.getNMS().setBlockFast(b.getWorld(), b.getX(), b.getY(), b.getZ(), 0, (byte) 0);
		}
	}
}
