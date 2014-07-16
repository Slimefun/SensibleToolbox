package me.desht.sensibletoolbox.api.gui;

import org.bukkit.ChatColor;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ButtonGadget extends ClickableGadget {
    private final ItemStack labelTexture;
    private final Runnable callback;

    public ButtonGadget(InventoryGUI owner, int slot, String text, String[] lore, ItemStack texture, Runnable callback) {
        super(owner, slot);
        this.callback = callback;
        labelTexture = texture == null ? InventoryGUI.BUTTON_TEXTURE.clone() : texture.clone();
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
