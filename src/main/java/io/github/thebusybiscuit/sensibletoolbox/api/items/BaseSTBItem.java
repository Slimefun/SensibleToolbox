package io.github.thebusybiscuit.sensibletoolbox.api.items;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import io.github.thebusybiscuit.cscorelib2.data.PersistentDataAPI;
import io.github.thebusybiscuit.sensibletoolbox.SensibleToolboxPlugin;
import io.github.thebusybiscuit.sensibletoolbox.api.SensibleToolbox;
import io.github.thebusybiscuit.sensibletoolbox.api.energy.Chargeable;
import io.github.thebusybiscuit.sensibletoolbox.api.gui.InventoryGUIListener;
import io.github.thebusybiscuit.sensibletoolbox.core.STBItemRegistry;
import io.github.thebusybiscuit.sensibletoolbox.util.STBUtil;
import me.desht.dhutils.ItemGlow;

/**
 * Represents an STB item. This is the superclass for all STB items.
 */
public abstract class BaseSTBItem implements Comparable<BaseSTBItem>, InventoryGUIListener, Keyed {

    public static final ChatColor LORE_COLOR = ChatColor.GRAY;
    public static final ChatColor DISPLAY_COLOR = ChatColor.YELLOW;
    public static final String SUFFIX_SEPARATOR = " \uff1a ";

    private final String typeID;
    private final Plugin providerPlugin;
    private Map<Enchantment, Integer> enchants;

    protected BaseSTBItem() {
        typeID = getClass().getSimpleName().toLowerCase();
        providerPlugin = SensibleToolboxPlugin.getInstance().getItemRegistry().getPlugin(this);
    }

    protected BaseSTBItem(ConfigurationSection conf) {
        typeID = getClass().getSimpleName().toLowerCase();
        providerPlugin = SensibleToolboxPlugin.getInstance().getItemRegistry().getPlugin(this);
    }

    @Override
    public NamespacedKey getKey() {
        return new NamespacedKey(SensibleToolboxPlugin.getInstance(), typeID);
    }

    /**
     * You should not need to call this method directly. It is used internally to ensure
     * enchantments on an item stack are preserved when the STB item is converted
     * back to the item stack.
     *
     * @param stack
     *            an item stack
     */
    public final void storeEnchants(@Nonnull ItemStack stack) {
        enchants = stack.getEnchantments();
    }

    @ParametersAreNonnullByDefault
    protected void updateHeldItemStack(Player player, EquipmentSlot hand) {
        PlayerInventory inv = player.getInventory();
        if (hand == EquipmentSlot.HAND) {
            ItemStack item = inv.getItemInMainHand();
            inv.setItemInMainHand(toItemStack(item.getAmount()));
        }
        else if (hand == EquipmentSlot.OFF_HAND) {
            ItemStack item = inv.getItemInOffHand();
            inv.setItemInOffHand(toItemStack(item.getAmount()));
        }
        else {
            throw new IllegalArgumentException(hand.name() + " is not a hand! (HAND, OFF_HAND)");
        }
    }

    /**
     * Check that the given player has permission to carry out the given
     * action on or with this item.
     *
     * @param player
     *            the player to check
     * @param action
     *            the action to take
     * @return true if the player has permission to take the action; false otherwise
     */
    public final boolean checkPlayerPermission(Player player, ItemAction action) {
        String prefix = SensibleToolboxPlugin.getInstance().getItemRegistry().getPermissionPrefix(this);
        Validate.notNull(prefix, "Can't determine permission node prefix for " + getItemTypeID());
        return player.hasPermission(prefix + "." + action.getNode() + "." + getItemTypeID());
    }

    /**
     * Given a material name, return the type of STB item that crafting ingredients of this type
     * must be to count as a valid crafting ingredient for this item.
     *
     * @param mat
     *            the ingredient material
     * @return null for no restriction, or a BaseSTBItem subclass to specify a restriction
     */
    public final Class<? extends BaseSTBItem> getCraftingRestriction(Material mat) {
        return SensibleToolboxPlugin.getInstance().getItemRegistry().getCraftingRestriction(this, mat);
    }

    /**
     * Register one or more STB items as custom ingredients in the crafting recipe for
     * this item. This will ensure that only these items, and not the vanilla item which
     * uses the same material, will work in the crafting recipe.
     *
     * @param items
     *            the STB items to register as custom ingredients
     */
    protected final void registerCustomIngredients(BaseSTBItem... items) {
        for (BaseSTBItem item : items) {
            SensibleToolboxPlugin.getInstance().getItemRegistry().addCraftingRestriction(this, item.getMaterial(), item.getClass());
        }
    }

    /**
     * Check if this item is used as an ingredient for the given resulting item.
     *
     * @param result
     *            the resulting item
     * @return true if this item may be used, false otherwise
     */
    public final boolean isIngredientFor(ItemStack result) {
        BaseSTBItem item = SensibleToolbox.getItemRegistry().fromItemStack(result);
        if (item == null) {
            return false;
        }
        Class<? extends BaseSTBItem> c = item.getCraftingRestriction(getMaterial());
        return c == getClass();
    }

    /**
     * Define the material and data used to represent this item.
     *
     * @return the material data
     */
    public abstract Material getMaterial();

    /**
     * Define the item's displayed name.
     *
     * @return the item name
     */
    public abstract String getItemName();

    /**
     * Define the base lore to display for the item.
     *
     * @return the item lore
     */
    public abstract String[] getLore();

    /**
     * Define a suffix to be appended to the item's displayed name. Override this in
     * implementing classes where you wish to represent some or all of the item's state
     * in the display name.
     *
     * @return the display suffix, or null for no suffix
     */
    public String getDisplaySuffix() {
        return null;
    }

    /**
     * Define extra lore to be appended to the base lore. Override this in
     * implementing classes where you wish to represent some or all of the item's
     * state in the item lore.
     *
     * @return the extra item lore
     */
    public String[] getExtraLore() {
        return new String[0];
    }

    /**
     * Define the vanilla crafting recipe used to create the item.
     *
     * @return the recipe, or null if the item does not have a vanilla crafting recipe
     */
    public abstract Recipe getRecipe();

    /**
     * Define any alternative vanilla crafting recipes used to create the item.
     *
     * @return an array of recipes
     */
    public Recipe[] getExtraRecipes() {
        return new Recipe[0];
    }

    /**
     * Define whether the item should glow. The default is for items not to glow.
     * <p/>
     * This will only work if ProtocolLib is installed.
     *
     * @return true if the item should glow
     */
    public boolean hasGlow() {
        return false;
    }

    /**
     * Called when a player interacts with a block or air while holding an STB item.
     *
     * @param event
     *            the interaction event.
     */
    public void onInteractItem(PlayerInteractEvent event) {}

    /**
     * Called when a player attempts to consume an STB item (which must be food or potion).
     *
     * @param event
     *            the consume event
     */
    public void onItemConsume(PlayerItemConsumeEvent event) {}

    /**
     * Called when a player interacts with an entity while holding an STB item.
     *
     * @param event
     *            the interaction event
     */
    public void onInteractEntity(PlayerInteractEntityEvent event) {}

    /**
     * Called when a player rolls the mouse wheel while sneaking and holding an STB item.
     *
     * @param event
     *            the held item change event
     */
    public void onItemHeld(PlayerItemHeldEvent event) {}

    /**
     * Define the item into which this item would be smelted in a vanilla
     * furnace, if any.
     *
     * @return the resulting item stack, or null if this object does not smelt
     */
    public ItemStack getSmeltingResult() {
        return null;
    }

    /**
     * Define whether this item can be enchanted normally in a vanilla enchanting
     * table.
     *
     * @return true if the item can be enchanted; false otherwise
     */
    public boolean isEnchantable() {
        return true;
    }

    /**
     * Define any additional crafting hints for this item. This information
     * can be displayed in a recipe book, for example.
     *
     * @return some crafting notes, or null for no notes
     */
    public String getCraftingNotes() {
        return null;
    }

    /**
     * Called when a block is broken while holding an STB item. If the block being broken is an STB
     * block, this event handler will be called before the event handler for the block being broken.
     * The handler is called with EventPriority.MONITOR, so the event outcome must not be altered by
     * this handler.
     *
     * @param event
     *            the block break event
     */
    public void onBreakBlockWithItem(BlockBreakEvent event) {}

    /**
     * Create an ItemStack with one item from this STB item, serializing any item-specific data into the ItemStack.
     *
     * @return the new ItemStack
     */
    public ItemStack toItemStack() {
        return toItemStack(1);
    }

    /**
     * Create an ItemStack from this STB item, serializing any item-specific data into the ItemStack.
     *
     * @param amount
     *            number of items in the stack
     * @return the new ItemStack
     */
    public ItemStack toItemStack(int amount) {
        ItemStack res = new ItemStack(getMaterial(), amount);

        if (enchants != null) {
            res.addUnsafeEnchantments(enchants);
        }

        ItemMeta im = res.getItemMeta();
        String suffix = getDisplaySuffix() == null ? "" : SUFFIX_SEPARATOR + getDisplaySuffix();
        im.setDisplayName(DISPLAY_COLOR + getItemName() + suffix);
        im.setLore(buildLore());

        // any serialized data from the object goes in the ItemStack attributes
        YamlConfiguration conf = freeze();
        if (!isStackable()) {
            // add a (hopefully) unique hidden field to ensure the item can't stack
            conf.set("*nostack", System.nanoTime() ^ ThreadLocalRandom.current().nextLong());
        }
        conf.set("*TYPE", getItemTypeID());
        PersistentDataAPI.setString(im, SensibleToolboxPlugin.getInstance().getItemRegistry().getKey(), conf.saveToString());

        res.setItemMeta(im);

        if (SensibleToolboxPlugin.getInstance().isGlowingEnabled()) {
            ItemGlow.setGlowing(res, hasGlow());
        }

        if (this instanceof Chargeable && res.getType().getMaxDurability() > 0) {
            // encode the STB item's charge level into the itemstack's damage bar
            Chargeable ch = (Chargeable) this;
            STBUtil.levelToDurability(res, (int) ch.getCharge(), ch.getMaxCharge());
        }

        return res;
    }

    private List<String> buildLore() {
        String[] lore = getLore();
        String[] lore2 = getExtraLore();

        List<String> res = new ArrayList<>(lore.length + lore2.length + 1);
        res.add(STBItemRegistry.LORE_PREFIX + getProviderPlugin().getName() + " (STB) item");

        for (String l : lore) {
            res.add(LORE_COLOR + l);
        }
        for (String l : lore2) {
            res.add(LORE_COLOR + l);
        }
        return res;
    }

    /**
     * Get the short type identifier code for this item.
     *
     * @return the item's type ID
     */
    public final String getItemTypeID() {
        return typeID;
    }

    /**
     * Define whether this item is wearable. By default, any armour item will be
     * wearable, but if you wish to use an armour material for a non-wearable
     * item, then override this method to return false.
     *
     * @return true if the item is wearable
     */
    public boolean isWearable() {
        return STBUtil.isWearable(getMaterial());
    }

    /**
     * Define whether this item is stackable in an inventory. This can be
     * overridden to false for STB items which should not stack, but which
     * use a vanilla material that does stack.
     *
     * @return true if the item should be stackable; false otherwise
     */
    public boolean isStackable() {
        return true;
    }

    /**
     * Get the instance of the plugin which registered this STB item.
     *
     * @return the plugin which registered this item
     */
    public final Plugin getProviderPlugin() {
        return providerPlugin;
    }

    /**
     * Get the item-specific configuration from the providing plugin's config.
     * The configuration section prefix used will be
     * "<em>prefix</em>.<em>item-type-id</em>." where <em>item-type-id</em> is
     * the return value of {@link #getItemTypeID()}.
     *
     * @param prefix
     *            the configuration node prefix
     * @return the configuration section for this item's config
     */
    public final ConfigurationSection getItemConfig(String prefix) {
        return getProviderPlugin().getConfig().getConfigurationSection(prefix).getConfigurationSection(getItemTypeID());
    }

    /**
     * Get the item-specific configuration from the providing plugin's config.
     * The configuration section prefix used will be
     * "item_settings.<em>item-type-id</em>." where <em>item-type-id</em> is
     * the return value of {@link #getItemTypeID()}.
     *
     * @return the configuration section for this item's config
     */
    public final ConfigurationSection getItemConfig() {
        return getItemConfig("item_settings");
    }

    @Override
    public String toString() {
        return "STB Item [" + getItemName() + "]";
    }

    /**
     * Freeze this object's state into a YamlConfiguration object. If you
     * override this method to freeze additional object fields, your
     * overridden method must call super.freeze() to get the frozen base
     * object state and augment & return that.
     *
     * @return a YamlConfiguration representing this object's state
     */
    public YamlConfiguration freeze() {
        return new YamlConfiguration();
    }

    @Override
    public int compareTo(BaseSTBItem other) {
        return getItemName().compareTo(other.getItemName());
    }

    @Override
    public boolean onSlotClick(HumanEntity player, int slot, ClickType click, ItemStack inSlot, ItemStack onCursor) {
        return false;
    }

    @Override
    public boolean onPlayerInventoryClick(HumanEntity player, int slot, ClickType click, ItemStack inSlot, ItemStack onCursor) {
        return true;
    }

    @Override
    public int onShiftClickInsert(HumanEntity player, int slot, ItemStack toInsert) {
        return 0;
    }

    @Override
    public boolean onShiftClickExtract(HumanEntity player, int slot, ItemStack toExtract) {
        return true;
    }

    @Override
    public boolean onClickOutside(HumanEntity player) {
        return false;
    }

    @Override
    public void onGUIOpened(HumanEntity player) {}

    @Override
    public void onGUIClosed(HumanEntity player) {}

    /**
     * Called when an item is newly crafted. This method can be used to set
     * up some dynamic property of the item which would not otherwise be known
     * at construction time.
     *
     * @return true if this method changed a property of the item; false otherwise
     */
    public boolean onCrafted() {
        return false;
    }

    /**
     * Perform any extra crafting validation steps needed to craft this item.
     * For example, check if a certain ingredient has a specific enchantment.
     * By default this method returns true to allow crafting to continue, but
     * you may override this method for specific items.
     *
     * @param inventory
     *            crafting inventory for this crafting event
     * @return true if crafting should continue; false if it should be stopped
     */
    public boolean validateCrafting(CraftingInventory inventory) {
        return true;
    }

}