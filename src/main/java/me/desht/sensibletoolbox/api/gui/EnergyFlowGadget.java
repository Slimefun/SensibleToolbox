package me.desht.sensibletoolbox.api.gui;

import me.desht.sensibletoolbox.api.items.BaseSTBItem;
import me.desht.sensibletoolbox.api.util.STBUtil;
import me.desht.sensibletoolbox.blocks.machines.BatteryBox;
import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class EnergyFlowGadget extends CyclerGadget<BatteryBox.EnergyFlow> {
    private final BlockFace face;

    public EnergyFlowGadget(InventoryGUI gui, int slot, BlockFace face) {
        super(gui, slot, face.toString());
        Validate.isTrue(gui.getOwningItem() instanceof BatteryBox, "Energy flow gadget can only be used on a battery box!");
        this.face = face;
        add(BatteryBox.EnergyFlow.IN, ChatColor.DARK_AQUA, STBUtil.makeColouredMaterial(Material.WOOL, DyeColor.BLUE),
                "Device accepts energy", "on this face");
        add(BatteryBox.EnergyFlow.OUT, ChatColor.GOLD, STBUtil.makeColouredMaterial(Material.WOOL, DyeColor.ORANGE),
                "Device emits energy", "on this face");
        add(BatteryBox.EnergyFlow.NONE, ChatColor.GRAY, STBUtil.makeColouredMaterial(Material.WOOL, DyeColor.SILVER),
                "This face does not", "accept or emit energy");
        setInitialValue(((BatteryBox) gui.getOwningItem()).getEnergyFlow(face));
    }

    @Override
    protected boolean ownerOnly() {
        return false;
    }

    @Override
    protected void apply(BaseSTBItem stbItem, BatteryBox.EnergyFlow newValue) {
        ((BatteryBox) getGUI().getOwningItem()).setFlow(face, newValue);
    }
}
