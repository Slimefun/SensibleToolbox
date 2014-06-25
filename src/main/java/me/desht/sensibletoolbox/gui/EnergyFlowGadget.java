package me.desht.sensibletoolbox.gui;

import me.desht.sensibletoolbox.blocks.machines.BatteryBox;
import org.apache.commons.lang.Validate;
import org.bukkit.block.BlockFace;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class EnergyFlowGadget extends ClickableGadget {
    private final BlockFace face;
    private BatteryBox.EnergyFlow flow;

    public EnergyFlowGadget(InventoryGUI gui, int slot, BlockFace face) {
        super(gui, slot);
        this.face = face;
        Validate.isTrue(gui.getOwningItem() instanceof BatteryBox, "Energy flow gadget can only be used on a battery box!");
        flow = ((BatteryBox) gui.getOwningItem()).getEnergyFlow(face);
    }

    @Override
    public void onClicked(InventoryClickEvent event) {
        int n = (flow.ordinal() + 1) % BatteryBox.EnergyFlow.values().length;
        flow = BatteryBox.EnergyFlow.values()[n];
        event.setCurrentItem(flow.getTexture(face));
        ((BatteryBox) getGUI().getOwningItem()).setFlow(face, flow);
    }

    @Override
    public ItemStack getTexture() {
        return flow.getTexture(face);
    }
}
