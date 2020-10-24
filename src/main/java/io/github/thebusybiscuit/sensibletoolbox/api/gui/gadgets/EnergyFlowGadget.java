package io.github.thebusybiscuit.sensibletoolbox.api.gui.gadgets;

import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;

import io.github.thebusybiscuit.sensibletoolbox.api.energy.EnergyFlow;
import io.github.thebusybiscuit.sensibletoolbox.api.gui.InventoryGUI;
import io.github.thebusybiscuit.sensibletoolbox.api.items.BaseSTBItem;
import io.github.thebusybiscuit.sensibletoolbox.blocks.machines.BatteryBox;

/**
 * A GUI gadget which allows energy flow settings for a block to be displayed
 * and modified.
 * 
 * @author desht
 */
public class EnergyFlowGadget extends CyclerGadget<EnergyFlow> {

    private final BlockFace face;

    /**
     * Construct an energy flow gadget.
     *
     * @param gui
     *            the GUI that the gadget belongs to
     * @param slot
     *            the GUI slot that the gadget occupies
     * @param face
     *            the block face that this energy flow applies to
     */
    public EnergyFlowGadget(InventoryGUI gui, int slot, BlockFace face) {
        super(gui, slot, face.toString());

        Validate.isTrue(gui.getOwningItem() instanceof BatteryBox, "Energy flow gadget can only be used on a battery box!");

        this.face = face;
        add(EnergyFlow.IN, ChatColor.DARK_AQUA, Material.BLUE_WOOL, "Device accepts energy", "on this face");
        add(EnergyFlow.OUT, ChatColor.GOLD, Material.ORANGE_WOOL, "Device emits energy", "on this face");
        add(EnergyFlow.NONE, ChatColor.GRAY, Material.LIGHT_GRAY_WOOL, "This face does not", "accept or emit energy");
        setInitialValue(((BatteryBox) gui.getOwningItem()).getEnergyFlow(face));
    }

    @Override
    protected boolean ownerOnly() {
        return false;
    }

    @Override
    protected void apply(BaseSTBItem stbItem, EnergyFlow newValue) {
        ((BatteryBox) getGUI().getOwningItem()).setFlow(face, newValue);
    }
}
