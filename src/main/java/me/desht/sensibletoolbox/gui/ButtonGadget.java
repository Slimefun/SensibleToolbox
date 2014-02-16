package me.desht.sensibletoolbox.gui;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ButtonGadget extends ClickableGadget {
	private final ItemStack labelTexture;
	private final Runnable callback;

	public ButtonGadget(InventoryGUI owner, String text) {
		this(owner, text, null, null, null);
	}

	public ButtonGadget(InventoryGUI owner, String text, String[] lore) {
		this(owner, text, lore, null, null);
	}

	public ButtonGadget(InventoryGUI owner, String text, String[] lore, ItemStack texture, Runnable callback) {
		super(owner);
		this.callback = callback;
		labelTexture = texture == null ? new ItemStack(Material.ENDER_PORTAL) : texture;
		ItemMeta meta = labelTexture.getItemMeta();
		meta.setDisplayName(ChatColor.WHITE + ChatColor.UNDERLINE.toString() + text);
		if (lore != null) {
			meta.setLore(InventoryGUI.makeLore(lore));
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
