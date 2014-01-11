package me.desht.sensibletoolbox.items;

import me.desht.dhutils.MiscUtil;
import me.desht.dhutils.ParticleEffect;
import me.desht.sensibletoolbox.SensibleToolboxPlugin;
import me.desht.sensibletoolbox.util.STBUtil;
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

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class WateringCan extends BaseSTBItem {
	private static final int GROW_CHANCE = 10;
	private static final int MAX_LEVEL = 100;
	private static final int FIRE_EXTINGUISH_AMOUNT = 50;
	private int waterLevel;
	private boolean floodWarning;

	public WateringCan(Configuration conf) {
		setWaterLevel(conf.getInt("level"));
	}

	public WateringCan() {
		waterLevel = 0;
	}

	public int getWaterLevel() {
		return waterLevel;
	}

	public void setWaterLevel(int level) {
		this.waterLevel = level;
	}

	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> res = new HashMap<String, Object>();
		res.put("level", waterLevel);
		return res;
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
		return getWaterLevel() + "%";
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
	public void handleItemInteraction(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		ItemStack newStack = null;
		floodWarning = false;
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			Block b = event.getClickedBlock();
			Block neighbour = b.getRelative(event.getBlockFace());
			if ((neighbour.getType() == Material.STATIONARY_WATER || neighbour.getType() == Material.WATER) && neighbour.getData() == 0) {
				// attempt to refill the watering can
				player.playSound(player.getLocation(), Sound.WATER, 1.0f, 0.8f);
				neighbour.setType(Material.AIR);
				setWaterLevel(MAX_LEVEL);
				newStack = toItemStack(1);
			} else if (STBUtil.isCrop(b.getType())) {
				// attempt to grow the crops in a 3x3 area, and use some water from the can
				waterCrops(player, b);
				newStack = toItemStack(1);
			} else if (b.getType() == Material.SOIL) {
				if (STBUtil.isCrop(b.getRelative(BlockFace.UP).getType())) {
					waterCrops(player, b.getRelative(BlockFace.UP));
					newStack = toItemStack(1);
				} else {
					// make the soil wetter if possible
					waterSoil(player, b);
					newStack = toItemStack(1);
				}
			} else if (b.getType() == Material.COBBLESTONE && getWaterLevel() >= 10) {
				if (new Random().nextBoolean()) {
					b.setType(Material.MOSSY_COBBLESTONE);
				}
				useSomeWater(player, b, 10);
				newStack = toItemStack(1);
			} else if (b.getType() == Material.SMOOTH_BRICK && b.getData() != 1  && getWaterLevel() >= 10) {
				if (new Random().nextBoolean()) {
					b.setData((byte) 1);
				}
				useSomeWater(player, b, 10);
				newStack = toItemStack(1);
			}
		} else if (event.getAction() == Action.RIGHT_CLICK_AIR) {
			Block b = player.getEyeLocation().getBlock();
			if (b.getType() == Material.WATER || b.getType() == Material.STATIONARY_WATER) {
				// attempt to refill the watering can
				b.setType(Material.AIR);
				player.playSound(player.getLocation(), Sound.WATER, 1.0f, 0.8f);
				setWaterLevel(MAX_LEVEL);
				newStack = toItemStack(1);
			}
		}
		if (newStack != null) {
			event.setCancelled(true);
			player.setItemInHand(newStack);
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
		setWaterLevel(conf.getInt("level"));
		if (player.getFireTicks() > 0 && getWaterLevel() > FIRE_EXTINGUISH_AMOUNT) {
			player.setFireTicks(0);
			setWaterLevel(getWaterLevel() - FIRE_EXTINGUISH_AMOUNT);
			MiscUtil.alertMessage(player, "The fire is out!");
		}
		player.setItemInHand(toItemStack(1));
		player.updateInventory();
		event.setCancelled(true);
	}

	private void waterSoil(Player player, Block b) {
		for (Block b1 : STBUtil.getSurroundingBlocks(b)) {
			if (getWaterLevel() <= 0) {
				player.playSound(player.getLocation(), Sound.NOTE_BASS, 1.0f, 1.0f);
				break;
			}
			if (b1.getType() == Material.SOIL) {
				if (b1.getData() < 8) {
					b1.setData((byte) (b1.getData() + 1));
				}
				checkForFlooding(b1);
				useSomeWater(player, b, 1);
			}
			if (player.isSneaking()) {
				break; // only water one block if sneaking
			}
		}
	}

	private void waterCrops(Player player, Block b) {
		for (Block b1 : STBUtil.getSurroundingBlocks(b)) {
			if (getWaterLevel() <= 0) {
				player.playSound(player.getLocation(), Sound.NOTE_BASS, 1.0f, 1.0f);
				break;
			}
			maybeGrowCrop(player, b1);
			if (player.isSneaking()) {
				break; // only water one block if sneaking
			}
		}
	}

	private void maybeGrowCrop(Player player, Block b) {
		if (!STBUtil.isCrop(b.getType())) {
			return;
		}
		if (new Random().nextInt(100) < GROW_CHANCE) {
			if (b.getData() < 8) {
				b.setData((byte)(b.getData() + 1));
			}
		}
		checkForFlooding(b.getRelative(BlockFace.DOWN));
		useSomeWater(player, b, 1);
	}

	private void checkForFlooding(Block soil) {
		int saturation = SoilSaturation.getSaturationLevel(soil);
		long now = System.currentTimeMillis();
		long delta = (now - SoilSaturation.getLastWatered(soil)) / 1000;
		saturation = Math.max(0, saturation + 7 - (int) delta);
		if (saturation > SoilSaturation.MAX_SATURATION && new Random().nextBoolean()) {
			soil.setTypeIdAndData(Material.WATER.getId(), (byte) 0, true);
			SoilSaturation.clear(soil);
			soil.getWorld().dropItemNaturally(soil.getLocation(), new ItemStack(Material.DIRT));
		} else {
			SoilSaturation.setLastWatered(soil, System.currentTimeMillis());
			SoilSaturation.setSaturationLevel(soil, saturation);
		}
		if (saturation > SoilSaturation.MAX_SATURATION - 10) {
			floodWarning = true;
		}
	}

	private void useSomeWater(Player p, Block b, int amount) {
		setWaterLevel(Math.max(0, getWaterLevel() - amount));
		p.playSound(p.getLocation(), Sound.WATER, 1.0f, 2.0f);
		if (SensibleToolboxPlugin.getInstance().isProtocolLibEnabled()) {
			Location loc = b.getLocation();
			ParticleEffect.SPLASH.play(loc.add(0.5, 0.5, 0.5), 0.2f, 0.2f, 0.2f, 1.0f, 10);
		}
	}
}
