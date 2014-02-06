package me.desht.sensibletoolbox.blocks.machines.gui;

import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ButtonGadget extends ClickableGadget {
	private final ItemStack labelTexture;
	private final Runnable callback;

	public ButtonGadget(InventoryGUI owner, String text) {
		this(owner, text, null, null);
	}

	public ButtonGadget(InventoryGUI owner, String text, String[] lore) {
		this(owner, text, lore, null);
	}

	public ButtonGadget(InventoryGUI owner, String text, String[] lore, Runnable callback) {
		super(owner);
		this.callback = callback;
		labelTexture = new ItemStack(Material.ENDER_PORTAL);
		ItemMeta meta = labelTexture.getItemMeta();
		meta.setDisplayName(text);
		if (lore != null) {
			meta.setLore(makeLore(lore));
		}
		labelTexture.setItemMeta(meta);
	}

	@Override
	public void onClicked(InventoryClickEvent event) {
		if (callback != null) {
			callback.run();
		}
	}

	@Override
	public ItemStack getTexture() {
		return labelTexture;
	}
}
