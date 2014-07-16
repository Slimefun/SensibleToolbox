package me.desht.sensibletoolbox.api.gui;

import me.desht.sensibletoolbox.api.AccessControl;
import me.desht.sensibletoolbox.api.items.BaseSTBBlock;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class AccessControlGadget extends ClickableGadget {
    private AccessControl accessControl;
    private final BaseSTBBlock stb;

    public AccessControlGadget(InventoryGUI owner, int slot) {
        super(owner, slot);
        stb = owner.getOwningBlock();
        accessControl = stb.getAccessControl();
    }

    public AccessControlGadget(InventoryGUI owner, int slot, BaseSTBBlock stb) {
        super(owner, slot);
        this.stb = stb;
        accessControl = stb.getAccessControl();
    }

    @Override
    public void onClicked(InventoryClickEvent event) {
        if (!event.getWhoClicked().getUniqueId().equals(stb.getOwner())) {
            return;
        }
        int n = (accessControl.ordinal() + 1) % AccessControl.values().length;
        accessControl = AccessControl.values()[n];
        event.setCurrentItem(accessControl.getTexture(stb.getOwner()));
        stb.setAccessControl(accessControl);
    }

    @Override
    public ItemStack getTexture() {
        return accessControl.getTexture(stb.getOwner());
    }
}
