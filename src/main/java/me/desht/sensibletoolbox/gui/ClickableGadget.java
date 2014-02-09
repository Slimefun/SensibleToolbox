package me.desht.sensibletoolbox.gui;

import org.bukkit.ChatColor;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public abstract class ClickableGadget {
	private final InventoryGUI gui;

	protected ClickableGadget(InventoryGUI gui) {
		this.gui = gui;
	}

	public InventoryGUI getGUI() {
		return gui;
	}

	public abstract void onClicked(InventoryClickEvent event);

	public abstract ItemStack getTexture();

	public List<String> makeLore(String... lore) {
		List<String> res = new ArrayList<String>();
		for (String s : lore) {
			res.add(ChatColor.GRAY + s);
		}
		return res;
	}
}
