package me.desht.sensibletoolbox.api.gui;

import me.desht.sensibletoolbox.api.ChargeDirection;
import me.desht.sensibletoolbox.api.items.BaseSTBMachine;
import org.apache.commons.lang.Validate;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class ChargeDirectionGadget extends ClickableGadget {
    private ChargeDirection chargeDirection;
    private final BaseSTBMachine machine;

    public ChargeDirectionGadget(InventoryGUI gui, int slot) {
        super(gui, slot);
        Validate.isTrue(gui.getOwningItem() instanceof BaseSTBMachine, "Charge Direction gadget can only be added to machines!");
        machine = (BaseSTBMachine) gui.getOwningItem();
        chargeDirection = machine.getChargeDirection();
    }

    @Override
    public void onClicked(InventoryClickEvent event) {
        int n = (chargeDirection.ordinal() + 1) % ChargeDirection.values().length;
        chargeDirection = ChargeDirection.values()[n];
        event.setCurrentItem(chargeDirection.getTexture());
        machine.setChargeDirection(chargeDirection);
    }

    @Override
    public ItemStack getTexture() {
        return chargeDirection.getTexture();
    }
}
