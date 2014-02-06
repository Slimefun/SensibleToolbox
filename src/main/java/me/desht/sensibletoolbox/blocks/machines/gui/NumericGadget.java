package me.desht.sensibletoolbox.blocks.machines.gui;

import org.apache.commons.lang.math.IntRange;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class NumericGadget extends ClickableGadget {
	private final String title;
	private final IntRange range;
	private final int largeIncr;
	private final int smallIncr;
	private final UpdateListener callback;
	private final ItemStack icon = new ItemStack(Material.PAPER);
	private int value;

	public NumericGadget(InventoryGUI owner, String title, IntRange range, int value, int largeIncr, int smallIncr, UpdateListener callback) {
		super(owner);
		this.title = title;
		this.range = range;
		this.value = value;
		this.largeIncr = largeIncr;
		this.smallIncr = smallIncr;
		this.callback = callback;
	}

	@Override
	public void onClicked(InventoryClickEvent event) {
		int newValue = value;
		if (event.isLeftClick()) {
			newValue -= event.isShiftClick() ? smallIncr : largeIncr;
		} else if (event.isRightClick()) {
			newValue += event.isShiftClick() ? smallIncr : largeIncr;
		}
		if (callback.run(newValue)) {
			value = Math.max(Math.min(newValue, range.getMaximumInteger()), range.getMinimumInteger());
			event.setCurrentItem(getTexture());
		} else {
			// vetoed by the block!
			if (event.getWhoClicked() instanceof Player) {
				((Player)event.getWhoClicked()).playSound(event.getWhoClicked().getLocation(), Sound.NOTE_BASS, 1.0f, 1.0f);
			}
		}
	}

	@Override
	public ItemStack getTexture() {
		ItemMeta meta = icon.getItemMeta();
		meta.setDisplayName(ChatColor.YELLOW + title + ": " + ChatColor.AQUA + value);
		String[] lore = {
				"Valid value range: " + range.getMinimumInteger() + "-" + range.getMaximumInteger(),
				"L-Click: -" + largeIncr,
				"R-Click: +" + largeIncr,
				"With Shift held, +/-" + smallIncr
		};
		meta.setLore(makeLore(lore));
		icon.setItemMeta(meta);
		return icon;
	}

	public interface UpdateListener {
		public boolean run(int value);
	}
}
