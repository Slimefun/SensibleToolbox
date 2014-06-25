package me.desht.sensibletoolbox.gui;

import me.desht.sensibletoolbox.api.STBMachine;
import org.apache.commons.lang.Validate;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class ChargeDirectionGadget extends ClickableGadget {
    private STBMachine.ChargeDirection chargeDirection;
    private final STBMachine machine;

    public ChargeDirectionGadget(InventoryGUI gui, int slot) {
        super(gui, slot);
        Validate.isTrue(gui.getOwningItem() instanceof STBMachine, "Charge Direction gadget can only be added to machines!");
        machine = (STBMachine) gui.getOwningItem();
        chargeDirection = machine.getChargeDirection();
    }

    @Override
    public void onClicked(InventoryClickEvent event) {
        int n = (chargeDirection.ordinal() + 1) % STBMachine.ChargeDirection.values().length;
        chargeDirection = STBMachine.ChargeDirection.values()[n];
        event.setCurrentItem(chargeDirection.getTexture());
        machine.setChargeDirection(chargeDirection);
    }

    @Override
    public ItemStack getTexture() {
        return chargeDirection.getTexture();
    }
}
