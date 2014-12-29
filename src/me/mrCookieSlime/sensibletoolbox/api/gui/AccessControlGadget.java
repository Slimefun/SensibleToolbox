package me.mrCookieSlime.sensibletoolbox.api.gui;

import me.mrCookieSlime.sensibletoolbox.api.AccessControl;
import me.mrCookieSlime.sensibletoolbox.api.items.BaseSTBBlock;
import me.mrCookieSlime.sensibletoolbox.api.items.BaseSTBItem;
import me.mrCookieSlime.sensibletoolbox.api.util.STBUtil;

import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;

/**
 * A gadget which can display and update the access control for an STB block.
 */
public class AccessControlGadget extends CyclerGadget<AccessControl> {
    /**
     * Construct an access control gadget.
     * <p/>
     * The <em>stb</em> parameter would normally refer to a different block
     * than the GUI's owner.  This allows, for example, a GUI to be created on
     * a non-block item to be used to configure the access control settings
     * for another block.
     *
     * @param gui the GUI to add the gadget to
     * @param slot the GUI slot to display the gadget in
     * @param stb the STB block that the gadget controls
     */
    public AccessControlGadget(InventoryGUI gui, int slot, BaseSTBBlock stb) {
        super(gui, slot, "Access", stb);
        add(AccessControl.PUBLIC, ChatColor.GREEN, STBUtil.makeColouredMaterial(Material.WOOL, DyeColor.GREEN),
                "Owner: " + ChatColor.ITALIC + "<OWNER>", "All players may access");
        add(AccessControl.PRIVATE, ChatColor.RED, STBUtil.makeColouredMaterial(Material.WOOL, DyeColor.RED),
                "Owner: " + ChatColor.ITALIC + "<OWNER>", "Only owner may access");
        add(AccessControl.RESTRICTED, ChatColor.YELLOW, STBUtil.makeColouredMaterial(Material.WOOL, DyeColor.YELLOW),
                "Owner: " + ChatColor.ITALIC + "<OWNER>", "Only owner and owner's", "friends may access");
        setInitialValue(stb == null ? gui.getOwningBlock().getAccessControl() : stb.getAccessControl());
    }

    /**
     * Construct an access control gadget.
     *
     * @param gui the GUI to add the gadget to
     * @param slot the GUI slot to display the gadget in
     */
    public AccessControlGadget(InventoryGUI gui, int slot) {
        this(gui, slot, null);
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
