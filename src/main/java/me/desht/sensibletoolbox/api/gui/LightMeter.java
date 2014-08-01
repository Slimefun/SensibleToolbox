package me.desht.sensibletoolbox.api.gui;

import me.desht.sensibletoolbox.api.LightSensitive;
import org.apache.commons.lang.Validate;

/**
 * Measures the light intensity for an STB block.  The GUI that this gadget
 * is added to must be owned by an STB block which implements
 * {@link me.desht.sensibletoolbox.api.LightSensitive}.
 */
public class LightMeter extends MonitorGadget {
    /**
     * Constructs a new light meter gadget.
     *
     * @param gui the GUI which holds this gadget
     */
    public LightMeter(InventoryGUI gui) {
        super(gui);
        Validate.isTrue(gui.getOwningBlock() instanceof LightSensitive, "Attempt to install light meter in non-light-sensitive block!");
    }

    @Override
    public void repaint() {
        LightSensitive ls = (LightSensitive) getOwner();
        getGUI().getInventory().setItem(ls.getLightMeterSlot(), ls.getIndicator());
    }

    @Override
    public int[] getSlots() {
        return new int[]{((LightSensitive) getOwner()).getLightMeterSlot()};
    }
}
