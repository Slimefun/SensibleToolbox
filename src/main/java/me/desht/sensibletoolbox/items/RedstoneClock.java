package me.desht.sensibletoolbox.items;

import me.desht.dhutils.MiscUtil;
import me.desht.dhutils.ParticleEffect;
import me.desht.dhutils.PersistableLocation;
import me.desht.sensibletoolbox.SensibleToolboxPlugin;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.configuration.Configuration;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.material.Attachable;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RedstoneClock extends BaseSTBBlock {
	private int frequency = 20;
	private int onDuration = 5;
//	private BlockState savedState = null;

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
	public String getItemName() {
		return "Redstone Clock";
	}

	@Override
	public String[] getLore() {
		return new String[] { "Emits a redstone signal" };
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
	public String[] getExtraLore() {
		String l = BaseSTBItem.LORE_COLOR + " every " + ChatColor.GOLD + getFrequency() +
				BaseSTBItem.LORE_COLOR + " ticks for " + ChatColor.GOLD + getOnDuration() +
				BaseSTBItem.LORE_COLOR + " ticks";
		return new String[] { l };
	}

	@Override
	public void handleBlockPlace(BlockPlaceEvent event) {
		super.handleBlockPlace(event);
		Configuration conf = getItemAttributes(event.getItemInHand());
		setFrequency(conf.getInt("frequency"));
		setOnDuration(conf.getInt("onDuration"));
	}

	@Override
	public void onServerTick(PersistableLocation pLoc) {
		Location loc = pLoc.getLocation();
		Block b = loc.getBlock();
		long time = loc.getWorld().getTime();
		if (time % getFrequency() == 0 && !b.isBlockIndirectlyPowered()) {
			// power the neighbours up
//			savedState = b.getState();
			b.setType(Material.REDSTONE_BLOCK);
		} else if (time % getFrequency() == getOnDuration()) {
			// power the neighbours down
//			if (savedState != null) {
//				savedState.update(true);
//				savedState = null;
//			}
			b.setTypeIdAndData(getBaseMaterial().getId(), getBaseBlockData(), true);
		} else if (time % 50 == 10) {
			if (SensibleToolboxPlugin.getInstance().isProtocolLibEnabled()) {
				ParticleEffect.RED_DUST.play(loc.add(0.5, 0.5, 0.5), 1.0f, 1.0f, 1.0f, 0.0f, 4);
			} else {
				loc.getWorld().playEffect(loc.add(0, 0.5, 0), Effect.SMOKE, BlockFace.UP);
			}
		}
	}

	private static final Pattern intPat = Pattern.compile("(^[df])\\s*(\\d+)");

	@Override
	public boolean handleSignConfigure(SignChangeEvent event) {
		boolean updated = false, show = false;

		for (String line : event.getLines()) {
			if (line.equals("[show]")) {
				show = true;
			} else {
				Matcher m = intPat.matcher(line.toLowerCase());
				if (m.find()) {
					if (m.group(1).equals("f")) {
						setFrequency(Integer.parseInt(m.group(2)));
					} else if (m.group(1).equals("d")) {
						setOnDuration(Integer.parseInt(m.group(2)));
					}
					updated = true;
				}
			}
		}
		if (show) {
			event.setLine(0, ChatColor.DARK_RED + "F: " + ChatColor.RESET + getFrequency());
			event.setLine(1, ChatColor.DARK_RED + "D: " + ChatColor.RESET + getOnDuration());
			event.setLine(2, "");
			event.setLine(3, "");
		} else if (updated) {
			MiscUtil.statusMessage(event.getPlayer(),
					getItemName() + " updated: frequency=&6" + getFrequency() + "&- duration=&6" + getOnDuration());
			Sign sign = (Sign) event.getBlock().getState();
			blockUpdated(event.getBlock().getRelative(((Attachable) sign.getData()).getAttachedFace()));
		} else {
			MiscUtil.errorMessage(event.getPlayer(), "No valid data found: clock not updated");
		}
		return !show && updated;
	}
}
