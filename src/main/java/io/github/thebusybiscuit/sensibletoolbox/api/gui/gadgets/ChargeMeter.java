package io.github.thebusybiscuit.sensibletoolbox.api.gui.gadgets;

import org.apache.commons.lang.Validate;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import io.github.thebusybiscuit.sensibletoolbox.api.energy.ChargeableBlock;
import io.github.thebusybiscuit.sensibletoolbox.api.gui.InventoryGUI;
import io.github.thebusybiscuit.sensibletoolbox.utils.STBUtil;

/**
 * Shows the SCU level for an STB block. The GUI that this gadget is added to
 * must implement {@link ChargeableBlock}.
 * 
 * @author desht
 */
public class ChargeMeter extends MonitorGadget {

    private final ItemStack indicator;
    private final ChargeableBlock chargeable;

    /**
     * Constructs a charge meter gadget.
     *
     * @param gui
     *            the GUI which holds this gadget
     */
    public ChargeMeter(InventoryGUI gui) {
        super(gui);

        Validate.isTrue(getOwner() instanceof ChargeableBlock, "Attempt to add charge meter to non-chargeable block!");

        chargeable = (ChargeableBlock) getOwner();
        this.indicator = new ItemStack(Material.LEATHER_HELMET);
        ItemMeta meta = indicator.getItemMeta();
        meta.setDisplayName(STBUtil.getChargeString(chargeable));
        ((LeatherArmorMeta) meta).setColor(Color.YELLOW);
        indicator.setItemMeta(meta);
    }

    @Override
    public void repaint() {
        ItemMeta meta = indicator.getItemMeta();
        meta.setDisplayName(STBUtil.getChargeString(chargeable));
        indicator.setItemMeta(meta);
        STBUtil.levelToDurability(indicator, (int) chargeable.getCharge(), chargeable.getMaxCharge());
        getGUI().getInventory().setItem(chargeable.getChargeMeterSlot(), indicator);
    }

    @Override
    public int[] getSlots() {
        return new int[] { chargeable.getChargeMeterSlot() };
    }
}
