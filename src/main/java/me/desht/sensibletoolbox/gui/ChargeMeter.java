package me.desht.sensibletoolbox.gui;

import me.desht.sensibletoolbox.api.ChargeableBlock;
import me.desht.sensibletoolbox.util.STBUtil;
import org.apache.commons.lang.Validate;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;

public class ChargeMeter extends MonitorGadget {
    private final ItemStack indicator;
    private final ChargeableBlock chargeable;

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

    public void repaint() {
        ItemMeta meta = indicator.getItemMeta();
        meta.setDisplayName(STBUtil.getChargeString(chargeable));
        indicator.setItemMeta(meta);
        short max = indicator.getType().getMaxDurability();
        double d = chargeable.getCharge() / (double) chargeable.getMaxCharge();
        short dur = (short) (max * d);
        indicator.setDurability((short) Math.max(1, max - dur));
        getGUI().getInventory().setItem(chargeable.getChargeMeterSlot(), indicator);
    }

    @Override
    public int[] getSlots() {
        return new int[]{chargeable.getChargeMeterSlot()};
    }
}
