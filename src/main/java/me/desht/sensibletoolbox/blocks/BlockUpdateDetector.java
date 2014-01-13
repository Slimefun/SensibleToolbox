package me.desht.sensibletoolbox.blocks;

import me.desht.dhutils.MiscUtil;
import me.desht.sensibletoolbox.SensibleToolboxPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.Configuration;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BlockUpdateDetector extends BaseSTBBlock {
	private static final Pattern intPat = Pattern.compile("(^[dq])\\s*(\\d+)");
	private long lastPulse;
	private int duration;

	private int quiet;

	public BlockUpdateDetector(Configuration conf) {
		setDuration(conf.getInt("duration"));
	}

	public BlockUpdateDetector() {
		quiet = 1;
		duration = 2;
	}

	public static BlockUpdateDetector deserialize(Map<String, Object> map) {
		return new BlockUpdateDetector(getConfigFromMap(map));
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

	public int getQuiet() {
		return quiet;
	}

	public void setQuiet(int quiet) {
		this.quiet = quiet;
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
	public void handleBlockPhysics(BlockPhysicsEvent event) {
		final Block b = event.getBlock();
		System.out.println("BUD physics: time=" + b.getWorld().getFullTime() + ", lastPulse=" + lastPulse + ", duration=" + getDuration());
		if (b.getWorld().getFullTime() - lastPulse > getDuration() + getQuiet()) {
			// emit a signal for one tick
			System.out.println(" -> pulse!");
			lastPulse = b.getWorld().getFullTime();
			b.setType(Material.REDSTONE_BLOCK);
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
					} else if (m.group(1).equals("q")) {
						setQuiet(Integer.parseInt(m.group(2)));
					}
					updated = true;
				}
			}
		}
		if (show) {
			String l[] = getSignLabel();
			for (int i = 0; i < 4; i++) {
				event.setLine(i, l[i]);
			}
		} else if (updated) {
			MiscUtil.statusMessage(event.getPlayer(), getItemName() + " updated: duration=&6" + getDuration());
			updateBlock();
		} else {
			MiscUtil.errorMessage(event.getPlayer(), "No valid data found: clock not updated");
		}
		return !show && updated;
	}

	@Override
	public void handleBlockInteraction(PlayerInteractEvent event) {
		if (event.getAction() == Action.LEFT_CLICK_BLOCK && event.getPlayer().getItemInHand().getType() == Material.SIGN) {
			// attach a label sign
			attachLabelSign(event);
		}
	}

	@Override
	protected String[] getSignLabel() {
		return new String[] {
				getItemName(),
				ChatColor.DARK_RED + "Duration " + ChatColor.RESET + getDuration(),
				ChatColor.DARK_RED + "Quiet " + ChatColor.RESET + getQuiet(),
				"",
		};
	}
}
