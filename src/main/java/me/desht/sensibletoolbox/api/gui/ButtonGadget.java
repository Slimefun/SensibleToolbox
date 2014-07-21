package me.desht.sensibletoolbox.api.gui;

import me.desht.sensibletoolbox.core.gui.STBInventoryGUI;
import org.bukkit.ChatColor;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * A GUI gadget which allows code to be run when it's clicked.
 */
public class ButtonGadget extends ClickableGadget {
    private final ItemStack labelTexture;
    private final Runnable callback;

    /**
     * Constructs a button gadget.
     *
     * @param owner the GUI which this button belongs to
     * @param slot the GUI slot that this button should be displayed in
     * @param text the button text
     * @param lore the extended button tooltip
     * @param texture the item to use as the button's texture
     * @param callback the code which should be run when the button is
     *                 clicked
     */
    public ButtonGadget(InventoryGUI owner, int slot, String text, String[] lore, ItemStack texture, Runnable callback) {
        super(owner, slot);
        this.callback = callback;
        labelTexture = texture == null ? STBInventoryGUI.BUTTON_TEXTURE.clone() : texture.clone();
        ItemMeta meta = labelTexture.getItemMeta();
        meta.setDisplayName(ChatColor.WHITE + ChatColor.UNDERLINE.toString() + text);
        if (lore != null) {
            meta.setLore(GUIUtil.makeLore(lore));
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
