package me.desht.sensibletoolbox.api.gui;

import me.desht.sensibletoolbox.api.RedstoneBehaviour;
import me.desht.sensibletoolbox.api.items.BaseSTBBlock;
import me.desht.sensibletoolbox.api.items.BaseSTBItem;
import me.desht.sensibletoolbox.api.util.STBUtil;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.material.MaterialData;

/**
 * A GUI gadet which allows a STB block's redstone behaviour to be
 * displayed and changed.
 */
public class RedstoneBehaviourGadget extends CyclerGadget<RedstoneBehaviour> {
    public RedstoneBehaviourGadget(InventoryGUI gui, int slot) {
        super(gui, slot, "Redstone Mode");
        add(RedstoneBehaviour.IGNORE, ChatColor.GRAY, new MaterialData(Material.SULPHUR),
                "Operate regardless of", "redstone signal level");
        add(RedstoneBehaviour.HIGH, ChatColor.RED, new MaterialData(Material.REDSTONE),
                "Require a redstone", "signal to operate");
        add(RedstoneBehaviour.LOW, ChatColor.YELLOW, new MaterialData(Material.GLOWSTONE_DUST),
                "Require no redstone", "signal to operate");
        add(RedstoneBehaviour.PULSED, ChatColor.DARK_AQUA, STBUtil.makeColouredMaterial(Material.INK_SACK, DyeColor.BLUE),
                "Operate once per", "redstone pulse");
        setInitialValue(gui.getOwningBlock().getRedstoneBehaviour());
    }

    @Override
    protected boolean ownerOnly() {
        return false;
    }

    @Override
    protected boolean supported(BaseSTBItem stbItem, RedstoneBehaviour what) {
        return ((BaseSTBBlock) stbItem).supportsRedstoneBehaviour(what);
    }

    @Override
    protected void apply(BaseSTBItem stbItem, RedstoneBehaviour newValue) {
        ((BaseSTBBlock) stbItem).setRedstoneBehaviour(newValue);
    }
}
