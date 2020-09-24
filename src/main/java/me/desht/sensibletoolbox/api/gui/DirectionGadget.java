package me.desht.sensibletoolbox.api.gui;

import com.google.common.collect.Maps;
import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.block.BlockFace;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Directional;
import org.bukkit.material.Wool;

import java.util.Map;

/**
 * A GUI gadget which allows the facing direction of a directional item to be
 * changed.  This is actually a compound gadget: a central button with six
 * surrounding toggle buttons.
 */
public class DirectionGadget extends ClickableGadget {
    private final ItemStack mainTexture;
    private final Map<Integer,BlockFace> directionSlots = Maps.newHashMap();
    private boolean allowSelf;

    public DirectionGadget(InventoryGUI gui, int slot, ItemStack mainTexture) {
        super(gui, slot);
        Validate.isTrue(gui.getOwningItem() instanceof Directional, "DirectionalGadget can only be used on a directional item!");
        Validate.isTrue(slot >= 9 && slot < gui.getInventory().getSize() - 9 && slot % 9 > 0 && slot % 9 < 8,
                "DirectionalGadget can't be placed at edge of inventory window!");
        this.mainTexture = mainTexture;
        this.allowSelf = true;
        directionSlots.put(slot - 10, BlockFace.UP);
        directionSlots.put(slot - 9, BlockFace.NORTH);
        directionSlots.put(slot - 1, BlockFace.WEST);
        directionSlots.put(slot + 1, BlockFace.EAST);
        directionSlots.put(slot + 8, BlockFace.DOWN);
        directionSlots.put(slot + 9, BlockFace.SOUTH);

        for (Map.Entry<Integer,BlockFace> e : directionSlots.entrySet()) {
            gui.addGadget(makeDirectionButton(gui, e.getKey(), e.getValue()));
        }
    }

    @Override
    public void onClicked(InventoryClickEvent event) {
        // this is just the central button click handler (clear direction)
        // specific direction handlers are dealt with in makeDirectionButton()
        if (allowSelf) {
            ((Directional) getGUI().getOwningItem()).setFacingDirection(BlockFace.SELF);
            for (int slot : directionSlots.keySet()) {
                ((ToggleButton) getGUI().getGadget(slot)).setValue(false);
            }
        }
    }

    @Override
    public ItemStack getTexture() {
        return mainTexture;
    }

    /**
     * Check if this gadget allows SELF (i.e. no direction) to be used as a
     * direction for the owning item.
     *
     * @return true if SELF is allowed; false otherwise
     */
    public boolean allowSelf() {
        return allowSelf;
    }

    /**
     * Set whether this gadget allows SELF (i.e. no direction) to be used as a
     * direction for the owning item.
     *
     * @param allowSelf true if SELF is a valid direction for the item; false otherwise
     */
    public void setAllowSelf(boolean allowSelf) {
        this.allowSelf = allowSelf;
    }

    private ToggleButton makeDirectionButton(final InventoryGUI gui, final int slot, final BlockFace face) {
        ItemStack trueStack = GUIUtil.makeTexture(new Wool(DyeColor.ORANGE), ChatColor.YELLOW + face.toString());
        ItemStack falseStack = GUIUtil.makeTexture(new Wool(DyeColor.SILVER), ChatColor.YELLOW + face.toString());
        final Directional owner = (Directional) gui.getOwningItem();
        return new ToggleButton(gui, slot, owner.getFacing() == face, trueStack, falseStack, new ToggleButton.ToggleListener() {
            @Override
            public boolean run(boolean newValue) {
                // acts sort of like a radio button - switching one on switches all other
                // off, but switching one off leaves all switch off
                if (!newValue && !allowSelf) {
                    return false;
                }
                if (newValue) {
                    owner.setFacingDirection(face);
                    for (int otherSlot : directionSlots.keySet()) {
                        if (slot != otherSlot) {
                            ((ToggleButton) gui.getGadget(otherSlot)).setValue(false);
                        }
                    }
                } else {
                    owner.setFacingDirection(BlockFace.SELF);
                }
                return true;
            }
        });
    }
}
