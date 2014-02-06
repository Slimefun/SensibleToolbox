package me.desht.sensibletoolbox.blocks;

import me.desht.dhutils.Debugger;
import me.desht.dhutils.MiscUtil;
import me.desht.sensibletoolbox.SensibleToolboxPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.material.MaterialData;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BlockUpdateDetector extends BaseSTBBlock {
	private static final Pattern intPat = Pattern.compile("(^[dq])\\s*(\\d+)");
	private static final MaterialData md = new MaterialData(Material.STAINED_CLAY, DyeColor.PURPLE.getWoolData());
	private long lastPulse;
	private int duration;

	private int quiet;

	public BlockUpdateDetector() {
		quiet = 1;
		duration = 2;
	}

	public BlockUpdateDetector(ConfigurationSection conf) {
		super(conf);
		setDuration(conf.getInt("duration"));
	}

	@Override
	public YamlConfiguration freeze() {
		YamlConfiguration conf = super.freeze();
		conf.set("duration", duration);
		return conf;
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
	public MaterialData getMaterialData() {
		return md;
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
		return new String[] {
				"Pulse duration: " + ChatColor.GOLD + getDuration() + " ticks",
				"Sleep time after pulse: " + ChatColor.GOLD + getQuiet() + " ticks",
		};
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
	public void onBlockPhysics(BlockPhysicsEvent event) {
		final Block b = event.getBlock();
		Debugger.getInstance().debug(this + ": BUD physics: time=" + getTicksLived() + ", lastPulse=" + lastPulse + ", duration=" + getDuration());
		if (getTicksLived() - lastPulse > getDuration() + getQuiet()) {
			// emit a signal for one or more ticks
			lastPulse = getTicksLived();
			b.setType(Material.REDSTONE_BLOCK);
			Bukkit.getScheduler().runTaskLater(SensibleToolboxPlugin.getInstance(), new Runnable() {
				@Override
				public void run() {
					MaterialData m = getMaterialData();
					b.setTypeIdAndData(m.getItemTypeId(), m.getData(), true);
				}
			}, duration);
		}
	}

	@Override
	public boolean onSignChange(SignChangeEvent event) {
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
			updateBlock(false);
		} else {
			MiscUtil.errorMessage(event.getPlayer(), "No valid data found: clock not updated");
		}
		return !show && updated;
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
