package me.desht.sensibletoolbox.api.gui;

import me.desht.sensibletoolbox.api.AccessControl;
import me.desht.sensibletoolbox.api.items.BaseSTBBlock;
import me.desht.sensibletoolbox.api.items.BaseSTBItem;
import me.desht.sensibletoolbox.api.util.STBUtil;
import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;

public class AccessControlGadget extends CyclerGadget<AccessControl> {
    public AccessControlGadget(InventoryGUI gui, int slot, BaseSTBBlock stb) {
        super(gui, slot, "Access", stb);
        add(AccessControl.PUBLIC, ChatColor.GREEN, STBUtil.makeColouredMaterial(Material.WOOL, DyeColor.GREEN),
                "Owner: " + ChatColor.ITALIC + "<OWNER>", "All players may access");
        add(AccessControl.PRIVATE, ChatColor.RED, STBUtil.makeColouredMaterial(Material.WOOL, DyeColor.RED),
                "Owner: " + ChatColor.ITALIC + "<OWNER>", "Only owner may access");
        setInitialValue(stb == null ? gui.getOwningBlock().getAccessControl() : stb.getAccessControl());
    }

    public AccessControlGadget(InventoryGUI owner, int slot) {
        this(owner, slot, null);
    }

    @Override
    protected boolean ownerOnly() {
        return true;
    }

    @Override
    protected void apply(BaseSTBItem stbItem, AccessControl newValue) {
        ((BaseSTBBlock) stbItem).setAccessControl(newValue);
    }
}
