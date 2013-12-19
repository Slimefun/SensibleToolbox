package me.desht.sensibletoolbox.items;

import me.desht.dhutils.ParticleEffect;
import me.desht.dhutils.PersistableLocation;
import me.desht.sensibletoolbox.SensibleToolboxPlugin;
import org.bukkit.DyeColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;

import java.util.Map;

public class RedstoneClock extends BaseSTBItem {
	private int frequency = 20;
	private int onDuration = 5;

	public int getFrequency() {
		return frequency;
	}

	public void setFrequency(int frequency) {
		this.frequency = frequency;
	}

	public int getOnDuration() {
		return onDuration;
	}

	public void setOnDuration(int onDuration) {
		this.onDuration = onDuration;
	}

	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> res = super.serialize();
		res.put("frequency", frequency);
		res.put("onDuration", onDuration);
		return res;
	}

	public static RedstoneClock deserialize(Map<String, Object> map) {
		RedstoneClock clock = new RedstoneClock();
		clock.setFrequency((Integer) map.get("frequency"));
		clock.setOnDuration((Integer) map.get("onDuration"));
		return clock;
	}

	@Override
	public Material getBaseMaterial() {
		return Material.STAINED_CLAY;
	}

	@Override
	public Byte getBaseBlockData() {
		return DyeColor.RED.getWoolData();
	}

	@Override
	public String getDisplayName() {
		return "Redstone Clock";
	}

	@Override
	public String[] getLore() {
		return new String[] { "Emits a redstone signal once every second" };
	}

	@Override
	public Recipe getRecipe() {
		ShapedRecipe res = new ShapedRecipe(toItemStack(1));
		res.shape("RSR", "STS", "RSR");
		res.setIngredient('R', Material.REDSTONE);
		res.setIngredient('S', Material.STONE);
		res.setIngredient('T', Material.REDSTONE_TORCH_ON);
		return res;
	}

	@Override
	public void handleBlockPlace(BlockPlaceEvent event) {
		blockPlaced(event.getBlock(), this);
	}

	@Override
	public void handleBlockBreak(BlockBreakEvent event) {
		Block block = event.getBlock();
		block.getWorld().dropItemNaturally(block.getLocation(), toItemStack(1));
		blockRemoved(block, this);
		event.getPlayer().playEffect(block.getLocation(), Effect.STEP_SOUND, block.getType());
		block.setType(Material.AIR);
		event.setCancelled(true);
	}

	@Override
	public void onServerTick(PersistableLocation pLoc) {
		Location loc = pLoc.getLocation();
		Block b = loc.getBlock();
		long time = loc.getWorld().getTime();
		if (time % getFrequency() == 0 && !b.isBlockIndirectlyPowered()) {
			// power the neighbours up
			b.setType(Material.REDSTONE_BLOCK);
		} else if (time % getFrequency() == getOnDuration()) {
			// power the neighbours down
			b.setTypeIdAndData(getBaseMaterial().getId(), getBaseBlockData(), true);
		} else if (time % 50 == 10) {
			if (SensibleToolboxPlugin.getInstance().isProtocolLibEnabled()) {
				ParticleEffect.RED_DUST.play(loc.add(0.5, 0.5, 0.5), 1.0f, 1.0f, 1.0f, 0.0f, 4);
			} else {
				loc.getWorld().playEffect(loc.add(0, 0.5, 0), Effect.SMOKE, BlockFace.UP);
			}
		}
	}
}
