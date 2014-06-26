package me.desht.sensibletoolbox.gui;

import me.desht.sensibletoolbox.util.STBUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class ToggleButton extends ClickableGadget {
    private final ItemStack trueTexture;
    private final ItemStack falseTexture;
    private final ToggleListener callback;
    boolean value;

    public ToggleButton(InventoryGUI gui, int slot, boolean value, ItemStack trueTexture, ItemStack falseTexture, ToggleListener callback) {
        super(gui, slot);
        this.trueTexture = trueTexture;
        this.falseTexture = falseTexture;
        this.callback = callback;
        this.value = value;
    }

    @Override
    public void onClicked(InventoryClickEvent event) {
        boolean newValue = !value;

        if (callback.run(newValue)) {
            value = newValue;
            event.setCurrentItem(getTexture());
        } else {
            // vetoed!
            if (event.getWhoClicked() instanceof Player) {
                STBUtil.complain((Player) event.getWhoClicked());
            }
        }
    }

    @Override
    public ItemStack getTexture() {
        return value ? trueTexture : falseTexture;
    }

    public void setValue(boolean newValue) {
        value = newValue;
        updateGUI();
    }

    public interface ToggleListener {
        public boolean run(boolean newValue);
    }
}
