package me.desht.sensibletoolbox.blocks;

import me.desht.dhutils.MiscUtil;
import me.desht.dhutils.ParticleEffect;
import me.desht.sensibletoolbox.SensibleToolboxPlugin;
import me.desht.sensibletoolbox.items.BaseSTBItem;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RedstoneClock extends BaseSTBBlock {
	private static final Pattern intPat = Pattern.compile("(^[df])\\s*(\\d+)");
	private int frequency;
	private int onDuration;

	public RedstoneClock(ConfigurationSection conf) {
		super(conf);
		setFrequency(conf.getInt("frequency"));
		setOnDuration(conf.getInt("onDuration"));
	}

	public RedstoneClock() {
		frequency = 20;
		onDuration = 5;
	}

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
	public YamlConfiguration freeze() {
		YamlConfiguration conf = super.freeze();
		conf.set("frequency", frequency);
		conf.set("onDuration", onDuration);
		return conf;
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
				LORE_COLOR + " ticks for " + ChatColor.GOLD + getOnDuration() +
				LORE_COLOR + " ticks";
		return new String[] { l };
	}

	@Override
	public boolean shouldTick() {
		return true;
	}

	@Override
	public void onServerTick() {
		Location loc = getLocation();
		Block b = loc.getBlock();
		long time = loc.getWorld().getTime();
		if (time % getFrequency() == 0 && !b.isBlockIndirectlyPowered()) {
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

	@Override
	public String[] getSignLabel() {
		return new String[] {
				getItemName(),
				ChatColor.DARK_RED + "Freq " + ChatColor.RESET + getFrequency(),
				ChatColor.DARK_RED + "Duration " + ChatColor.RESET + getOnDuration(),
				""
		};
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
			String l[] = getSignLabel();
			for (int i = 0; i < 4; i++) {
				event.setLine(i, l[i]);
			}
		} else if (updated) {
			MiscUtil.statusMessage(event.getPlayer(), String.format("%s updated: frequency=&6%d&-, duration=&6%d",
					getItemName(), getFrequency(), getOnDuration()));
			updateBlock();
		} else {
			MiscUtil.errorMessage(event.getPlayer(), "No valid data found: clock not updated");
		}
		return !show && updated;
	}
}
