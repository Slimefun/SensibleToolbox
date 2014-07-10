package me.desht.sensibletoolbox.items;

import com.comphenix.attribute.AttributeStorage;
import com.google.common.collect.Maps;
import me.desht.dhutils.Debugger;
import me.desht.dhutils.ItemGlow;
import me.desht.dhutils.LogUtils;
import me.desht.dhutils.PermissionUtils;
import me.desht.sensibletoolbox.STBFreezable;
import me.desht.sensibletoolbox.SensibleToolboxPlugin;
import me.desht.sensibletoolbox.api.Chargeable;
import me.desht.sensibletoolbox.api.STBBlock;
import me.desht.sensibletoolbox.api.STBItem;
import me.desht.sensibletoolbox.blocks.*;
import me.desht.sensibletoolbox.blocks.machines.*;
import me.desht.sensibletoolbox.gui.InventoryGUI;
import me.desht.sensibletoolbox.items.components.*;
import me.desht.sensibletoolbox.items.energycells.FiftyKEnergyCell;
import me.desht.sensibletoolbox.items.energycells.TenKEnergyCell;
import me.desht.sensibletoolbox.items.itemroutermodules.*;
import me.desht.sensibletoolbox.items.machineupgrades.EjectorUpgrade;
import me.desht.sensibletoolbox.items.machineupgrades.RegulatorUpgrade;
import me.desht.sensibletoolbox.items.machineupgrades.SpeedUpgrade;
import me.desht.sensibletoolbox.storage.LocationManager;
import me.desht.sensibletoolbox.util.STBUtil;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.MemoryConfiguration;
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
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Constructor;
import java.sql.SQLException;
import java.util.*;

public abstract class BaseSTBItem implements STBFreezable, Comparable<STBItem>, InventoryGUI.InventoryGUIListener, STBItem {
    private static final UUID STB_ATTRIBUTE_ID = UUID.fromString("60884913-70bb-48b3-a81a-54952dec2e31");

    public static final ChatColor LORE_COLOR = ChatColor.GRAY;
    protected static final ChatColor DISPLAY_COLOR = ChatColor.YELLOW;
    private static final String STB_LORE_PREFIX = ChatColor.DARK_GRAY.toString() + ChatColor.ITALIC + "\u25b9";
    public static final String SUFFIX_SEPARATOR = " \uff1a ";

    private static final Map<String, Class<? extends BaseSTBItem>> id2class = Maps.newHashMap();
    private static final Map<String, Class<? extends STBItem>> craftingRestriction = Maps.newHashMap();
    private static final Map<String, String> permissionPrefix = Maps.newHashMap();
    private static final Map<String, String> id2plugin = Maps.newHashMap();

    public static final int MAX_ITEM_ID_LENGTH = 32;

    private final String typeID;
    private final String providerName;
    private Map<Enchantment, Integer> enchants;

    public static void registerItems(SensibleToolboxPlugin plugin) {
        final String CONFIG_NODE = "items_enabled";
        final String PERMISSION_NODE = "stb";

        registerItem(new AngelicBlock(), plugin, CONFIG_NODE, PERMISSION_NODE);
        registerItem(new EnderLeash(), plugin, CONFIG_NODE, PERMISSION_NODE);
        registerItem(new RedstoneClock(), plugin, CONFIG_NODE, PERMISSION_NODE);
        registerItem(new BlockUpdateDetector(), plugin, CONFIG_NODE, PERMISSION_NODE);
        registerItem(new EnderBag(), plugin, CONFIG_NODE, PERMISSION_NODE);
        registerItem(new WateringCan(), plugin, CONFIG_NODE, PERMISSION_NODE);
        registerItem(new MoistureChecker(), plugin, CONFIG_NODE, PERMISSION_NODE);
        registerItem(new AdvancedMoistureChecker(), plugin, CONFIG_NODE, PERMISSION_NODE);
        registerItem(new WoodCombineHoe(), plugin, CONFIG_NODE, PERMISSION_NODE);
        registerItem(new IronCombineHoe(), plugin, CONFIG_NODE, PERMISSION_NODE);
        registerItem(new GoldCombineHoe(), plugin, CONFIG_NODE, PERMISSION_NODE);
        registerItem(new DiamondCombineHoe(), plugin, CONFIG_NODE, PERMISSION_NODE);
        registerItem(new TrashCan(), plugin, CONFIG_NODE, PERMISSION_NODE);
        registerItem(new PaintBrush(), plugin, CONFIG_NODE, PERMISSION_NODE);
        registerItem(new PaintRoller(), plugin, CONFIG_NODE, PERMISSION_NODE);
        registerItem(new PaintCan(), plugin, CONFIG_NODE, PERMISSION_NODE);
        registerItem(new Elevator(), plugin, CONFIG_NODE, PERMISSION_NODE);
        registerItem(new TapeMeasure(), plugin, CONFIG_NODE, PERMISSION_NODE);
        registerItem(new CircuitBoard(), plugin, CONFIG_NODE, PERMISSION_NODE);
        registerItem(new SimpleCircuit(), plugin, CONFIG_NODE, PERMISSION_NODE);
        registerItem(new MultiBuilder(), plugin, CONFIG_NODE, PERMISSION_NODE);
        registerItem(new Floodlight(), plugin, CONFIG_NODE, PERMISSION_NODE);
        registerItem(new MachineFrame(), plugin, CONFIG_NODE, PERMISSION_NODE);
        registerItem(new Smelter(), plugin, CONFIG_NODE, PERMISSION_NODE);
        registerItem(new Masher(), plugin, CONFIG_NODE, PERMISSION_NODE);
        registerItem(new Sawmill(), plugin, CONFIG_NODE, PERMISSION_NODE);
        registerItem(new IronDust(), plugin, CONFIG_NODE, PERMISSION_NODE);
        registerItem(new GoldDust(), plugin, CONFIG_NODE, PERMISSION_NODE);
        registerItem(new ItemRouter(), plugin, CONFIG_NODE, PERMISSION_NODE);
        registerItem(new BlankModule(), plugin, CONFIG_NODE, PERMISSION_NODE);
        registerItem(new PullerModule(), plugin, CONFIG_NODE, PERMISSION_NODE);
        registerItem(new DropperModule(), plugin, CONFIG_NODE, PERMISSION_NODE);
        registerItem(new SenderModule(), plugin, CONFIG_NODE, PERMISSION_NODE);
        registerItem(new DistributorModule(), plugin, CONFIG_NODE, PERMISSION_NODE);
        registerItem(new AdvancedSenderModule(), plugin, CONFIG_NODE, PERMISSION_NODE);
        registerItem(new ReceiverModule(), plugin, CONFIG_NODE, PERMISSION_NODE);
        registerItem(new SorterModule(), plugin, CONFIG_NODE, PERMISSION_NODE);
        registerItem(new VacuumModule(), plugin, CONFIG_NODE, PERMISSION_NODE);
        registerItem(new BreakerModule(), plugin, CONFIG_NODE, PERMISSION_NODE);
        registerItem(new StackModule(), plugin, CONFIG_NODE, PERMISSION_NODE);
        registerItem(new SpeedModule(), plugin, CONFIG_NODE, PERMISSION_NODE);
        registerItem(new TenKEnergyCell(), plugin, CONFIG_NODE, PERMISSION_NODE);
        registerItem(new FiftyKEnergyCell(), plugin, CONFIG_NODE, PERMISSION_NODE);
        registerItem(new FiftyKBatteryBox(), plugin, CONFIG_NODE, PERMISSION_NODE);
        registerItem(new SpeedUpgrade(), plugin, CONFIG_NODE, PERMISSION_NODE);
        registerItem(new EjectorUpgrade(), plugin, CONFIG_NODE, PERMISSION_NODE);
        registerItem(new RegulatorUpgrade(), plugin, CONFIG_NODE, PERMISSION_NODE);
        registerItem(new HeatEngine(), plugin, CONFIG_NODE, PERMISSION_NODE);
        registerItem(new BasicSolarCell(), plugin, CONFIG_NODE, PERMISSION_NODE);
        registerItem(new RecipeBook(), plugin, CONFIG_NODE, PERMISSION_NODE);
        registerItem(new AdvancedRecipeBook(), plugin, CONFIG_NODE, PERMISSION_NODE);
        registerItem(new Multimeter(), plugin, CONFIG_NODE, PERMISSION_NODE);
        registerItem(new BigStorageUnit(), plugin, CONFIG_NODE, PERMISSION_NODE);
        registerItem(new HyperStorageUnit(), plugin, CONFIG_NODE, PERMISSION_NODE);
        registerItem(new Pump(), plugin, CONFIG_NODE, PERMISSION_NODE);
        registerItem(new EnderTuner(), plugin, CONFIG_NODE, PERMISSION_NODE);
        registerItem(new EnderBox(), plugin, CONFIG_NODE, PERMISSION_NODE);
        registerItem(new BagOfHolding(), plugin, CONFIG_NODE, PERMISSION_NODE);
        registerItem(new InfernalDust(), plugin, CONFIG_NODE, PERMISSION_NODE);
        registerItem(new EnergizedIronDust(), plugin, CONFIG_NODE, PERMISSION_NODE);
        registerItem(new EnergizedGoldDust(), plugin, CONFIG_NODE, PERMISSION_NODE);
        registerItem(new EnergizedIronIngot(), plugin, CONFIG_NODE, PERMISSION_NODE);
        registerItem(new EnergizedGoldIngot(), plugin, CONFIG_NODE, PERMISSION_NODE);
        registerItem(new ToughMachineFrame(), plugin, CONFIG_NODE, PERMISSION_NODE);
        registerItem(new QuartzDust(), plugin, CONFIG_NODE, PERMISSION_NODE);
        registerItem(new SiliconWafer(), plugin, CONFIG_NODE, PERMISSION_NODE);
        registerItem(new IntegratedCircuit(), plugin, CONFIG_NODE, PERMISSION_NODE);
        registerItem(new LandMarker(), plugin, CONFIG_NODE, PERMISSION_NODE);
        if (plugin.isProtocolLibEnabled()) {
            registerItem(new SoundMuffler(), plugin, CONFIG_NODE, PERMISSION_NODE);
        }
    }

    public static void registerItem(BaseSTBItem item, Plugin plugin) {
        registerItem(item, plugin, null, null);
    }

    public static void registerItem(BaseSTBItem item, Plugin plugin, String configNode) {
        registerItem(item, plugin, configNode, null);
    }

    public static void registerItem(BaseSTBItem item, Plugin plugin, String configNode, String permissionNode) {
        String id = item.getItemTypeID();

        if (configNode != null && !plugin.getConfig().getBoolean(configNode + "." + item.getItemTypeID())) {
            return;
        }

        Validate.isTrue(id.length() <= MAX_ITEM_ID_LENGTH, "Item ID '" + id + "' is too long! (32 chars max)");
        id2class.put(id, item.getClass());
        id2plugin.put(id, plugin.getDescription().getName());

        if (permissionNode == null) {
            permissionNode = "stb";
        }
        permissionPrefix.put(id, permissionNode);

        Bukkit.getPluginManager().addPermission(new Permission(permissionNode + ".interact." + id, PermissionDefault.TRUE));

        if (item instanceof STBBlock) {
            Bukkit.getPluginManager().addPermission(new Permission(permissionNode + ".place." + id, PermissionDefault.TRUE));
            Bukkit.getPluginManager().addPermission(new Permission(permissionNode + ".break." + id, PermissionDefault.TRUE));
            Bukkit.getPluginManager().addPermission(new Permission(permissionNode + ".interact_block." + id, PermissionDefault.TRUE));
            try {
                LocationManager.getManager().loadDeferredBlocks(id);
            } catch (SQLException e) {
                LogUtils.severe("There was a problem restoring blocks of type '" + id + "' from persisted storage:");
                e.printStackTrace();
            }
        }
    }

    public boolean checkPlayerPermission(Player player, String action) {
        String prefix = permissionPrefix.get(getItemTypeID());
        Validate.notNull(prefix, "Can't determine permission node prefix for " + getItemTypeID());
        return PermissionUtils.isAllowedTo(player, prefix + "." + action + "." + getItemTypeID());
    }

    /**
     * Get a set of all known STB item ID's.
     *
     * @return all know STB item ID's
     */
    public static Set<String> getItemIds() {
        return id2class.keySet();
    }

    /**
     * Construct and return an STB item from a supplied ItemStack.
     *
     * @param stack the item stack
     * @return the STB item, or null if the item stack is not an STB item
     */
    public static BaseSTBItem fromItemStack(ItemStack stack) {
        if (!isSTBItem(stack)) {
            return null;
        }
        Configuration conf = BaseSTBItem.getItemAttributes(stack);
        BaseSTBItem item = getItemById(conf.getString("*TYPE"), conf);
        if (item != null) {
            item.storeEnchants(stack);
        }
        return item;
    }

    /**
     * Construct and return an STB item from a supplied ItemStack.  The item must be an instance of
     * the supplied class (or a subclass of the supplied class).
     *
     * @param stack the ItemStack
     * @param type  the required class
     * @param <T>   parametrised type, a subclass of BaseSTBItem
     * @return the STB item, or null if the item stack is not an STB item of the desired class
     */
    public static <T extends BaseSTBItem> T fromItemStack(ItemStack stack, Class<T> type) {
        STBItem item = fromItemStack(stack);
        if (item != null && type.isAssignableFrom(item.getClass())) {
            return type.cast(item);
        } else {
            return null;
        }
    }

    /**
     * Construct and return an STB item.
     *
     * @param id the item ID
     * @return the STB item
     */
    public static STBItem getItemById(String id) {
        return getItemById(id, null);
    }

    /**
     * Construct and return an STB item.
     *
     * @param id   the item ID
     * @param conf item's frozen configuration data
     * @return the STB item
     */
    public static BaseSTBItem getItemById(String id, ConfigurationSection conf) {
        Class<? extends BaseSTBItem> c = id2class.get(id);
        if (c == null) {
            return null;
        }
        try {
            BaseSTBItem item;
            if (conf == null) {
                Constructor<? extends BaseSTBItem> cons = c.getConstructor();
                item = cons.newInstance();
            } else {
                Constructor<? extends BaseSTBItem> cons = c.getConstructor(ConfigurationSection.class);
                item = cons.newInstance(conf);
            }
            return item;
        } catch (Exception e) {
            LogUtils.warning("failed to create STB item from item ID: " + id);
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Check if the given item stack is an STB item.
     *
     * @param stack the item stack to check
     * @return true if this item stack is an STB item
     */
    public static boolean isSTBItem(ItemStack stack) {
        return isSTBItem(stack, null);
    }

    /**
     * Check if the given item stack is an STB item of the given STB subclass
     *
     * @param stack the item stack to check
     * @param c     a subclass of BaseSTBItem
     * @return true if this item stack is an STB item of (or extending) the given class
     */
    public static boolean isSTBItem(ItemStack stack, Class<? extends STBItem> c) {
        if (stack == null || !stack.hasItemMeta()) {
            return false;
        }
        ItemMeta im = stack.getItemMeta();
        if (im.hasLore()) {
            List<String> lore = im.getLore();
            if (!lore.isEmpty() && lore.get(0).startsWith(STB_LORE_PREFIX)) {
                if (c != null) {
                    Configuration conf = BaseSTBItem.getItemAttributes(stack);
                    Class<? extends STBItem> c2 = id2class.get(conf.getString("*TYPE"));
                    return c2 != null && c.isAssignableFrom(c2);
                } else {
                    return true;
                }
            }
        }
        return false;
    }

    private static Configuration getItemAttributes(ItemStack stack) {
        AttributeStorage storage = AttributeStorage.newTarget(stack, STB_ATTRIBUTE_ID);
        YamlConfiguration conf = new YamlConfiguration();
        try {
            String s = storage.getData("");
            if (s != null) {
                conf.loadFromString(s);
                if (Debugger.getInstance().getLevel() > 2) {
                    Debugger.getInstance().debug(3, "get item attributes for " + STBUtil.describeItemStack(stack) + ":");
                    for (String k : conf.getKeys(false)) {
                        Debugger.getInstance().debug(3, "- " + k + " = " + conf.get(k));
                    }
                }
                return conf;
            } else {
                throw new IllegalStateException("ItemStack " + stack + " has no STB attribute data!");
            }
        } catch (InvalidConfigurationException e) {
            e.printStackTrace();
            return new MemoryConfiguration();
        }
    }

    protected BaseSTBItem() {
        typeID = getClass().getSimpleName().toLowerCase();
        providerName = id2plugin.get(typeID);
    }

    protected BaseSTBItem(ConfigurationSection conf) {
        typeID = getClass().getSimpleName().toLowerCase();
        providerName = id2plugin.get(typeID);
    }

    private void storeEnchants(ItemStack stack) {
        enchants = stack.getEnchantments();
    }

    @Override
    public final Class<? extends STBItem> getCraftingRestriction(Material mat) {
        return craftingRestriction.get(getItemTypeID() + ":" + mat);
    }

    /**
     * Register one or more STB items as custom ingredients in the crafting recipe for
     * this item.  This will ensure that only these items, and not the vanilla item which
     * uses the same material, will work in the crafting recipe.
     *
     * @param items the STB items to register as custom ingredients
     */
    protected final void registerCustomIngredients(STBItem... items) {
        for (STBItem item : items) {
            craftingRestriction.put(getItemTypeID() + ":" + item.getMaterial(), item.getClass());
        }
    }

    @Override
    public final boolean isIngredientFor(ItemStack result) {
        STBItem item = BaseSTBItem.fromItemStack(result);
        if (item == null) {
            return false;
        }
        Class<? extends STBItem> c = item.getCraftingRestriction(getMaterial());
        return c == getClass();
    }

    @Override
    public final Material getMaterial() {
        return getMaterialData().getItemType();
    }

    @Override
    public String getDisplaySuffix() {
        return null;
    }

    @Override
    public String[] getExtraLore() {
        return new String[0];
    }

    @Override
    public Recipe[] getExtraRecipes() {
        return new Recipe[0];
    }

    @Override
    public boolean hasGlow() {
        return false;
    }

    /**
     * Called when a player interacts with a block or air while holding an STB item.
     *
     * @param event the interaction event.
     */
    public void onInteractItem(PlayerInteractEvent event) {
    }

    /**
     * Called when a player attempts to consume an STB item (which must be food or potion).
     *
     * @param event the consume event
     */
    public void onItemConsume(PlayerItemConsumeEvent event) {
    }

    /**
     * Called when a player interacts with an entity while holding an STB item.
     *
     * @param event the interaction event
     */
    public void onInteractEntity(PlayerInteractEntityEvent event) {
    }

    /**
     * Called when a player rolls the mouse wheel while sneaking and holding an STB item.
     *
     * @param event the held item change event
     */
    public void onItemHeld(PlayerItemHeldEvent event) {
    }

    @Override
    public ItemStack getSmeltingResult() {
        return null;
    }

    @Override
    public boolean isEnchantable() {
        return true;
    }

    /**
     * Called when a block is broken while holding an STB item.  If the block being broken is an STB
     * block, this event handler will be called before the event handler for the block being broken.
     * The handler is called with EventPriority.MONITOR, so the event outcome must not be altered by
     * this handler.
     *
     * @param event the block break event
     */
    public void onBreakBlockWithItem(BlockBreakEvent event) {
    }

    @Override
    public ItemStack toItemStack() {
        return toItemStack(1);
    }

    @Override
    public ItemStack toItemStack(int amount) {
        ItemStack res = getMaterialData().toItemStack(amount);

        ItemMeta im = res.getItemMeta();
        String suffix = getDisplaySuffix() == null ? "" : SUFFIX_SEPARATOR + getDisplaySuffix();
        im.setDisplayName(DISPLAY_COLOR + getItemName() + suffix);
        im.setLore(buildLore());
        res.setItemMeta(im);
        if (enchants != null) {
            res.addUnsafeEnchantments(enchants);
        }
        if (SensibleToolboxPlugin.getInstance().isProtocolLibEnabled()) {
            ItemGlow.setGlowing(res, hasGlow());
        }

        if (this instanceof Chargeable && res.getType().getMaxDurability() > 0) {
            // encode the STB item's charge level into the itemstack's damage bar
            Chargeable ch = (Chargeable) this;
            short max = res.getType().getMaxDurability();
            double d = ch.getCharge() / (double) ch.getMaxCharge();
            short dur = (short) (max * d);
            res.setDurability((short) (max - dur));
        }

        // any serialized data from the object goes in the ItemStack attributes
        YamlConfiguration conf = freeze();
        conf.set("*TYPE", getItemTypeID());
        AttributeStorage storage = AttributeStorage.newTarget(res, STB_ATTRIBUTE_ID);
        String data = conf.saveToString();
        storage.setData(data);
        Debugger.getInstance().debug(3, "serialize " + this + " to itemstack:\n" + data);
        return storage.getTarget();
    }

    private List<String> buildLore() {
        String[] lore = getLore();
        String[] lore2 = getExtraLore();
        List<String> res = new ArrayList<String>(lore.length + lore2.length + 1);
        res.add(STB_LORE_PREFIX + getProviderName() + " (STB) item");
        for (String l : lore) {
            res.add(LORE_COLOR + l);
        }
        Collections.addAll(res, lore2);
        return res;
    }

    @Override
    public YamlConfiguration freeze() {
        return new YamlConfiguration();
    }

    @Override
    public String toString() {
        return "STB Item [" + getItemName() + "]";
    }

    @Override
    public int compareTo(STBItem other) {
        return getItemName().compareTo(other.getItemName());
    }

    @Override
    public String getItemTypeID() {
        return typeID; // getClass().getSimpleName().toLowerCase();
    }

    @Override
    public boolean isWearable() {
        return STBUtil.isWearable(getMaterial());
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
    public void onGUIClosed(HumanEntity player) {
    }

    public String getProviderName() {
        return providerName;
    }
}
