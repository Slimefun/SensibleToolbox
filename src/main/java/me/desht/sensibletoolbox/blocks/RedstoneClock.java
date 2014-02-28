package me.desht.sensibletoolbox.blocks;

import me.desht.dhutils.ParticleEffect;
import me.desht.sensibletoolbox.SensibleToolboxPlugin;
import me.desht.sensibletoolbox.gui.AccessControlGadget;
import me.desht.sensibletoolbox.gui.InventoryGUI;
import me.desht.sensibletoolbox.gui.NumericGadget;
import me.desht.sensibletoolbox.gui.RedstoneBehaviourGadget;
import me.desht.sensibletoolbox.items.BaseSTBItem;
import me.desht.sensibletoolbox.util.STBUtil;
import org.apache.commons.lang.math.IntRange;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.material.MaterialData;

public class RedstoneClock extends BaseSTBBlock {
	private static final MaterialData md = STBUtil.makeColouredMaterial(Material.STAINED_CLAY, DyeColor.RED);
	private int frequency;
	private int onDuration;

	public RedstoneClock() {
		frequency = 20;
		onDuration = 5;
	}

	public RedstoneClock(ConfigurationSection conf) {
		super(conf);
		setFrequency(conf.getInt("frequency"));
		setOnDuration(conf.getInt("onDuration"));
	}

	@Override
	protected InventoryGUI createGUI() {
		InventoryGUI gui = new InventoryGUI(this, 9, ChatColor.DARK_RED + getItemName());
		gui.addGadget(new NumericGadget(gui, "Pulse Frequency", new IntRange(1, Integer.MAX_VALUE), getFrequency(), 10, 1,new NumericGadget.UpdateListener() {
			@Override
			public boolean run(int value) {
				if (value > getOnDuration()) {
					setFrequency(value);
					return true;
				} else {
					return false;
				}
			}
		}), 0);
		gui.addGadget(new NumericGadget(gui, "Pulse Duration", new IntRange(1, Integer.MAX_VALUE), getOnDuration(), 10, 1, new NumericGadget.UpdateListener() {
			@Override
			public boolean run(int value) {
				if (value < getFrequency()) {
					setOnDuration(value);
					return true;
				} else {
					return false;
				}
			}
		}), 1);
		gui.addGadget(new RedstoneBehaviourGadget(gui), 8);
		gui.addGadget(new AccessControlGadget(gui), 7);
		return gui;
	}

	public int getFrequency() {
		return frequency;
	}

	public void setFrequency(int frequency) {
		this.frequency = frequency;
		updateBlock(false);
	}

	public int getOnDuration() {
		return onDuration;
	}

	public void setOnDuration(int onDuration) {
		this.onDuration = onDuration;
		updateBlock(false);
	}

	@Override
	public YamlConfiguration freeze() {
		YamlConfiguration conf = super.freeze();
		conf.set("frequency", frequency);
		conf.set("onDuration", onDuration);
		return conf;
	}

	@Override
	public MaterialData getMaterialData() {
		return md;
	}

	@Override
	public String getItemName() {
		return "Redstone Clock";
	}

	@Override
	public String[] getLore() {
		return new String[] {
				"Clock-in-a-block",
				"Emits a redstone signal with",
				"configurable interval & duration",
				"R-click block: " + ChatColor.RESET + " configure clock"
		};
	}

	@Override
	public Recipe getRecipe() {
		ShapedRecipe res = new ShapedRecipe(toItemStack());
		res.shape("RSR", "STS", "RSR");
		res.setIngredient('R', Material.REDSTONE);
		res.setIngredient('S', Material.STONE);
		res.setIngredient('T', Material.REDSTONE_TORCH_ON);
		return res;
	}

	@Override
	public String[] getExtraLore() {
		String l = BaseSTBItem.LORE_COLOR + "Frequency: " + ChatColor.GOLD + getFrequency() +
				LORE_COLOR + "t, Duration: " + ChatColor.GOLD + getOnDuration() + LORE_COLOR + "t";
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
		long time = getTicksLived();
		if (time % getFrequency() == 0 && isRedstoneActive()) {
			// power up
			b.setType(Material.REDSTONE_BLOCK);
		} else if (time % getFrequency() == getOnDuration()) {
			// power down
			b.setTypeIdAndData(getMaterialData().getItemTypeId(), getMaterialData().getData(), true);
		} else if (time % 50 == 10) {
			if (SensibleToolboxPlugin.getInstance().isProtocolLibEnabled()) {
				ParticleEffect.RED_DUST.play(loc.add(0.5, 0.5, 0.5), 0.7f, 0.7f, 0.7f, 0.0f, 10);
			} else {
				loc.getWorld().playEffect(loc.add(0, 0.5, 0), Effect.SMOKE, BlockFace.UP);
			}
		}
		super.onServerTick();
	}

	@Override
	public void onInteractBlock(PlayerInteractEvent event) {
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK && !event.getPlayer().isSneaking()) {
			getGUI().show(event.getPlayer());
		}
		super.onInteractBlock(event);
	}
}
