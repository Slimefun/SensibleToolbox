package me.desht.sensibletoolbox.items;

import me.desht.dhutils.MiscUtil;
import me.desht.sensibletoolbox.SensibleToolboxPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.configuration.Configuration;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.material.Attachable;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BlockUpdateDetector extends BaseSTBBlock {
	private static final Pattern intPat = Pattern.compile("(^d)\\s*(\\d+)");
	private long lastPulse;
	private int duration = 2;

	public static BlockUpdateDetector deserialize(Map<String, Object> map) {
		BlockUpdateDetector bud = new BlockUpdateDetector();
		bud.setDuration((Integer) map.get("duration"));
		return bud;
	}

	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> res = super.serialize();
		res.put("duration", duration);
		return res;
	}

	public int getDuration() {
		return duration;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}

	@Override
	public Material getBaseMaterial() {
		return Material.STAINED_CLAY;
	}

	@Override
	public Byte getBaseBlockData() {
		return DyeColor.PURPLE.getWoolData();
	}

	@Override
	public String getItemName() {
		return "Block Update Detector";
	}

	@Override
	public String[] getLore() {
		return new String[] { "Emits a redstone pulse when", " an adjacent block updates"};
	}

	@Override
	public String[] getExtraLore() {
		return new String[] { "Pulse duration: " + ChatColor.GOLD + getDuration() + " ticks"};
	}

	@Override
	public Recipe getRecipe() {
		ShapedRecipe res = new ShapedRecipe(toItemStack(1));
		res.shape("SRS", "SPS", "STS");
		res.setIngredient('S', Material.STONE);
		res.setIngredient('P', Material.PISTON_STICKY_BASE);
		res.setIngredient('R', Material.REDSTONE);
		res.setIngredient('T', Material.REDSTONE_TORCH_ON);
		return res;
	}

	@Override
	public void handleBlockPlace(BlockPlaceEvent event) {
		super.handleBlockPlace(event);
		Configuration conf = getItemAttributes(event.getItemInHand());
		lastPulse = event.getBlock().getWorld().getFullTime();
		duration = conf.getInt("duration");
	}

	@Override
	public void handleBlockPhysics(BlockPhysicsEvent event) {
		final Block b = event.getBlock();
		if (b.getWorld().getFullTime() - lastPulse > duration + 1) {
			// emit a signal for one tick
			b.setType(Material.REDSTONE_BLOCK);
			lastPulse = b.getWorld().getFullTime();
			Bukkit.getScheduler().runTaskLater(SensibleToolboxPlugin.getInstance(), new Runnable() {
				@Override
				public void run() {
					b.setTypeIdAndData(getBaseMaterial().getId(), getBaseBlockData(), true);
				}
			}, duration);
		}
	}

	@Override
	public boolean handleSignConfigure(SignChangeEvent event) {
		boolean updated = false, show = false;

		for (String line : event.getLines()) {
			if (line.equals("[show]")) {
				show = true;
			} else {
				Matcher m = intPat.matcher(line.toLowerCase());
				if (m.find()) {
					if (m.group(1).equals("d")) {
						setDuration(Integer.parseInt(m.group(2)));
					}
					updated = true;
				}
			}
		}
		if (show) {
			event.setLine(0, ChatColor.DARK_RED + "D: " + ChatColor.RESET + getDuration());
			event.setLine(1, "");
			event.setLine(2, "");
			event.setLine(3, "");
		} else if (updated) {
			MiscUtil.statusMessage(event.getPlayer(),
					getItemName() + " updated: duration=&6" + getDuration());
			Sign sign = (Sign) event.getBlock().getState();
			blockUpdated(event.getBlock().getRelative(((Attachable) sign.getData()).getAttachedFace()));
		} else {
			MiscUtil.errorMessage(event.getPlayer(), "No valid data found: clock not updated");
		}
		return !show && updated;
	}
}
