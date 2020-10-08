package io.github.thebusybiscuit.sensibletoolbox.api.gui;

import java.util.List;
import java.util.UUID;

import javax.annotation.Nonnull;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.google.common.collect.Lists;

import io.github.thebusybiscuit.sensibletoolbox.api.items.BaseSTBBlock;
import io.github.thebusybiscuit.sensibletoolbox.api.items.BaseSTBItem;
import io.github.thebusybiscuit.sensibletoolbox.utils.STBUtil;

/**
 * A gadget which can cycle through the values of the given parameterised enum
 * and apply that value to the GUI's owning item. To use this class, extend
 * it with a concrete enum type, implement the abstract methods, and possibly
 * override non-abstract methods.
 *
 * @param <T>
 *            an enum type
 */
public abstract class CyclerGadget<T extends Enum<T>> extends ClickableGadget {

    private T currentValue;
    private ItemStack[] stacks;
    private final BaseSTBItem stbItem;
    private final String label;

    /**
     * Constructs a cycler gadget.
     * <p/>
     * The <em>item</em> parameter would typically refer to a separate STB
     * block from the GUI's owner. This allows a GUI on one item to
     * configure a setting in a separate block.
     *
     * @param gui
     *            the GUI to add the gadget to
     * @param slot
     *            the GUI slot to display the gadget in
     * @param label
     *            the primary tooltip for the gadget
     * @param item
     *            the item this gadget should apply to
     */
    protected CyclerGadget(InventoryGUI gui, int slot, String label, BaseSTBItem item) {
        super(gui, slot);
        stbItem = item == null ? gui.getOwningItem() : item;
        this.label = label;
    }

    /**
     * Constructs a cycler gadget.
     *
     * @param gui
     *            the GUI to add the gadget to
     * @param slot
     *            the GUI slot to display the gadget in
     * @param label
     *            the primary tooltip for the gadget
     */
    protected CyclerGadget(InventoryGUI gui, int slot, String label) {
        this(gui, slot, label, null);
    }

    /**
     * Add a definition (label, button texture) for a particular value of the
     * enum. This method should be called from the subclass's constructor for
     * each value of the enum; for any value for which it is not called, a
     * default texture will be created.
     *
     * @param what
     *            the enum value for which the texture is being defined
     * @param color
     *            color to use in the texture's tooltip
     * @param texture
     *            material to use for the texture
     * @param lore
     *            extra tooltip text
     */
    public void add(T what, ChatColor color, Material type, String... lore) {
        if (currentValue == null) {
            currentValue = what;
            stacks = new ItemStack[what.getClass().getEnumConstants().length];
        }

        stacks[what.ordinal()] = makeTexture(what, type, color, lore);
    }

    /**
     * Define the initial value of this gadget. This method should be called
     * from the subclass's constructor. Typically this method will copy the
     * STB item's current state.
     *
     * @param what
     *            the initial enum value to display
     */
    public void setInitialValue(T what) {
        currentValue = what;
    }

    /**
     * Check if any player other than the block owner (assuming this gadget
     * is on a block's GUI) may use this gadget.
     *
     * @return true if only the block's owner may use the gadget
     */
    protected abstract boolean ownerOnly();

    /**
     * Check if the item to which this gadget applies actually supports the
     * given value.
     *
     * @param stbItem
     *            the STB item
     * @param what
     *            the value to check
     * @return true if the value is applicable to the item; false otherwise
     */
    protected boolean supported(BaseSTBItem stbItem, T what) {
        return true;
    }

    /**
     * Go ahead and update the STB item with the given value.
     *
     * @param stbItem
     *            the STB item
     * @param newValue
     *            the new value
     */
    protected abstract void apply(BaseSTBItem stbItem, T newValue);

    @SuppressWarnings("unchecked")
    @Override
    public void onClicked(InventoryClickEvent event) {
        if (ownerOnly() && !mayOverride(event.getWhoClicked()) && stbItem instanceof BaseSTBBlock) {
            BaseSTBBlock stb = (BaseSTBBlock) stbItem;
            if (!event.getWhoClicked().getUniqueId().equals(stb.getOwner())) {
                return;
            }
        }

        int b = currentValue.ordinal();
        int n = b;
        int adjust = event.isRightClick() ? -1 : 1;

        do {
            n = (n + adjust) % stacks.length;

            if (n < 0) {
                n += stacks.length;
            }

            // noinspection unchecked
            currentValue = (T) currentValue.getClass().getEnumConstants()[n];

            if (n == b) {
                // avoid infinite loop due to no supported behaviour
                break;
            }
        }
        while (!supported(stbItem, currentValue));
        event.setCurrentItem(getTexture());
        apply(stbItem, currentValue);
    }

    private boolean mayOverride(HumanEntity whoClicked) {
        return whoClicked instanceof Player && whoClicked.hasPermission("stb.access.modify");
    }

    @Override
    public ItemStack getTexture() {
        if (stacks[currentValue.ordinal()] == null) {
            stacks[currentValue.ordinal()] = makeTexture(currentValue, Material.STONE, ChatColor.GRAY);
        }
        return stacks[currentValue.ordinal()];
    }

    private ItemStack makeTexture(T what, Material type, ChatColor color, String... lore) {
        ItemStack stack = new ItemStack(type);
        ItemMeta meta = stack.getItemMeta();
        meta.setDisplayName(ChatColor.WHITE.toString() + ChatColor.UNDERLINE + label + ":" + color + " " + what.toString());

        if (lore.length > 0) {
            String ownerName = getOwnerName();
            List<String> l = Lists.newArrayListWithCapacity(lore.length);

            for (String s : lore) {
                l.add(ChatColor.GRAY + s.replace("<OWNER>", ownerName));
            }

            meta.setLore(l);
        }

        stack.setItemMeta(meta);
        return stack;
    }

    @Nonnull
    private String getOwnerName() {
        if (stbItem instanceof BaseSTBBlock) {
            UUID id = ((BaseSTBBlock) stbItem).getOwner();
            String name = STBUtil.getPlayerNameFromUUID(id);
            return name == null ? id.toString() : name;
        } else {
            return "";
        }
    }
}
