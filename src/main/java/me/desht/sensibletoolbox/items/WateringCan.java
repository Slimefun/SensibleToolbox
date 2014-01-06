package me.desht.sensibletoolbox.items;

import me.desht.dhutils.MiscUtil;
import me.desht.dhutils.ParticleEffect;
import me.desht.sensibletoolbox.SensibleToolboxPlugin;
import me.desht.sensibletoolbox.util.SoilSaturation;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.material.Dye;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class WateringCan extends BaseSTBItem {
	private static final int GROW_CHANCE = 10;
	private static final int MAX_LEVEL = 100;
	private static final int FIRE_EXTINGUISH_AMOUNT = 50;



	private static BlockFace[] faces = new BlockFace[] {
		BlockFace.SELF, BlockFace.NORTH_EAST, BlockFace.EAST, BlockFace.SOUTH_EAST,
			BlockFace.SOUTH,BlockFace.SOUTH_WEST, BlockFace.WEST, BlockFace.NORTH_WEST, BlockFace.NORTH
	};

	private int level;
	private boolean floodWarning = false;

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> res = new HashMap<String, Object>();
		res.put("level", level);
		return res;
	}

	public static WateringCan deserialize(Map<String, Object> map) {
		WateringCan wc = new WateringCan();
		if (!map.isEmpty()) {
			wc.setLevel((Integer) map.get("level"));
		}
		return wc;
	}

	@Override
	public Material getBaseMaterial() {
		return Material.POTION;
	}

	@Override
	public String getItemName() {
		return "Watering Can";
	}

	@Override
	public String getDisplaySuffix() {
		return getLevel() + "%";
	}

	@Override
	public String[] getLore() {
		return new String[] { "Right-click to irrigate crops.", "Right-click in water to refill", "Don't over-use!" };
	}

	@Override
	public Recipe getRecipe() {
		ShapedRecipe recipe = new ShapedRecipe(toItemStack(1));
		Dye d = new Dye();
		d.setColor(DyeColor.WHITE);
		recipe.shape("SM ", "SBS", " S ");
		recipe.setIngredient('S', Material.STONE);
		recipe.setIngredient('M', d);
		recipe.setIngredient('B', Material.BOWL);
		return recipe;
	}

	@Override
	public String[] getExtraLore() {
		return new String[] { ChatColor.GOLD.toString() + getLevel() + ChatColor.RESET + "% full" };
	}

	@Override
	public void handleInteraction(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		ItemStack newStack = null;
		Configuration conf = BaseSTBItem.getItemAttributes(event.getPlayer().getItemInHand());
		setLevel(conf.getInt("level"));
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			Block b = event.getClickedBlock();
			Block neighbour = b.getRelative(event.getBlockFace());
			if ((neighbour.getType() == Material.STATIONARY_WATER || neighbour.getType() == Material.WATER) && neighbour.getData() == 0) {
				// attempt to refill the watering can
				player.playSound(player.getLocation(), Sound.WATER, 1.0f, 0.8f);
				neighbour.setType(Material.AIR);
				setLevel(MAX_LEVEL);
				newStack = toItemStack(1);
			} else if (isCrop(b.getType())) {
				// attempt to grow the crops in a 3x3 area, and use some water from the can
				waterCrops(player, b);
				newStack = toItemStack(1);
			} else if (b.getType() == Material.SOIL) {
				if (isCrop(b.getRelative(BlockFace.UP).getType())) {
					waterCrops(player, b.getRelative(BlockFace.UP));
					newStack = toItemStack(1);
				} else {
					// make the soil wetter if possible
					waterSoil(player, b);
					newStack = toItemStack(1);
				}
			}
		} else if (event.getAction() == Action.RIGHT_CLICK_AIR) {
			Block b = player.getEyeLocation().getBlock();
			if (b.getType() == Material.WATER || b.getType() == Material.STATIONARY_WATER) {
				// attempt to refill the watering can
				b.setType(Material.AIR);
				player.playSound(player.getLocation(), Sound.WATER, 1.0f, 0.8f);
				setLevel(MAX_LEVEL);
				newStack = toItemStack(1);
			}
		}
		if (newStack != null) {
			event.setCancelled(true);
			player.setItemInHand(newStack);
			System.out.println("new stack: " + newStack);
			player.updateInventory();
		}
		if (floodWarning) {
			MiscUtil.alertMessage(player, "This soil is getting very wet!");
			floodWarning = false;
		}
	}

	@Override
	public void handleConsume(PlayerItemConsumeEvent event) {
		Player player = event.getPlayer();
		Configuration conf = BaseSTBItem.getItemAttributes(player.getItemInHand());
		setLevel(conf.getInt("level"));
		if (player.getFireTicks() > 0 && getLevel() > FIRE_EXTINGUISH_AMOUNT) {
			System.out.println("fire ticks = " + player.getFireTicks());
			player.setFireTicks(0);
			setLevel(getLevel() - FIRE_EXTINGUISH_AMOUNT);
			MiscUtil.alertMessage(player, "The fire is out!");
		}
		player.setItemInHand(toItemStack(1));
		player.updateInventory();
		event.setCancelled(true);
	}

	private void waterSoil(Player player, Block b) {
		for (BlockFace face : faces) {
			if (getLevel() <= 0) {
				player.playSound(player.getLocation(), Sound.NOTE_BASS, 1.0f, 1.0f);
				break;
			}
			Block b1 = b.getRelative(face);
			if (b1.getType() == Material.SOIL) {
				if (b1.getData() < 8) {
					b1.setData((byte) (b1.getData() + 1));
				}
				checkForFlooding(b1);
				useSomeWater(player, b);
			}
			if (player.isSneaking()) {
				break; // only water one block if sneaking
			}
		}
	}

	private void waterCrops(Player player, Block b) {
		for (BlockFace face : faces) {
			if (getLevel() <= 0) {
				player.playSound(player.getLocation(), Sound.NOTE_BASS, 1.0f, 1.0f);
				break;
			}
			maybeGrowCrop(player, b.getRelative(face));
			if (player.isSneaking()) {
				break; // only water one block if sneaking
			}
		}
	}

	private void maybeGrowCrop(Player player, Block b) {
//		System.out.println("grow crop? " + b);
		if (!isCrop(b.getType())) {
			return;
		}
		if (new Random().nextInt(100) < GROW_CHANCE) {
			if (b.getData() < 8) {
				b.setData((byte)(b.getData() + 1));
			}
		}
		checkForFlooding(b.getRelative(BlockFace.DOWN));
		useSomeWater(player, b);
	}

	private void checkForFlooding(Block soil) {
		int saturation = SoilSaturation.getSaturationLevel(soil);
		long now = System.currentTimeMillis();
		long delta = (now - SoilSaturation.getLastWatered(soil)) / 1000;
		saturation = Math.max(0, saturation + 7 - (int) delta);
		System.out.println("Flood check: " + soil + ", saturation = " + saturation + ", last watered = " + delta + " secs ago");
		if (saturation > SoilSaturation.MAX_SATURATION && new Random().nextBoolean()) {
			soil.setType(Material.STATIONARY_WATER);
			SoilSaturation.clear(soil);
		} else {
			SoilSaturation.setLastWatered(soil, System.currentTimeMillis());
			SoilSaturation.setSaturationLevel(soil, saturation);
		}
		if (saturation > SoilSaturation.MAX_SATURATION - 10) {
			floodWarning = true;
		}
	}



	public static boolean isCrop(Material m) {
		return m == Material.CROPS || m == Material.CARROT || m == Material.POTATO || m == Material.PUMPKIN_STEM || m == Material.MELON_STEM;
	}

	private void useSomeWater(Player p, Block b) {
		setLevel(getLevel() - 1);
		p.playSound(p.getLocation(), Sound.WATER, 1.0f, 2.0f);
		if (SensibleToolboxPlugin.getInstance().isProtocolLibEnabled()) {
			Location loc = b.getLocation();
			ParticleEffect.SPLASH.play(loc.add(0, 0.5, 0), 0.2f, 0.2f, 0.2f, 1.0f, 10);
		}
	}
}
