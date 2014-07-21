package me.desht.sensibletoolbox.core;

import com.comphenix.attribute.AttributeStorage;
import com.google.common.collect.Maps;
import me.desht.dhutils.Debugger;
import me.desht.dhutils.LogUtils;
import me.desht.sensibletoolbox.SensibleToolboxPlugin;
import me.desht.sensibletoolbox.api.items.BaseSTBBlock;
import me.desht.sensibletoolbox.api.items.BaseSTBItem;
import me.desht.sensibletoolbox.api.util.STBUtil;
import me.desht.sensibletoolbox.blocks.*;
import me.desht.sensibletoolbox.blocks.machines.*;
import me.desht.sensibletoolbox.core.storage.LocationManager;
import me.desht.sensibletoolbox.items.*;
import me.desht.sensibletoolbox.items.components.*;
import me.desht.sensibletoolbox.items.energycells.FiftyKEnergyCell;
import me.desht.sensibletoolbox.items.energycells.TenKEnergyCell;
import me.desht.sensibletoolbox.items.itemroutermodules.*;
import me.desht.sensibletoolbox.items.machineupgrades.EjectorUpgrade;
import me.desht.sensibletoolbox.items.machineupgrades.RegulatorUpgrade;
import me.desht.sensibletoolbox.items.machineupgrades.SpeedUpgrade;
import me.desht.sensibletoolbox.items.machineupgrades.ThoroughnessUpgrade;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Constructor;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class ItemRegistry {
    public static final UUID STB_ATTRIBUTE_ID = UUID.fromString("60884913-70bb-48b3-a81a-54952dec2e31");
    public static final String LORE_PREFIX = ChatColor.DARK_GRAY.toString() + ChatColor.ITALIC + "\u25b9";
    public static final int MAX_ITEM_ID_LENGTH = 32;

    private final Map<String, Class<? extends BaseSTBItem>> id2class = Maps.newHashMap();
    private final Map<String, Class<? extends BaseSTBItem>> craftingRestrictions = Maps.newHashMap();
    private final Map<String, String> permissionPrefix = Maps.newHashMap();
    private final Map<String, String> id2plugin = Maps.newHashMap();

    private final SensibleToolboxPlugin plugin;

    public ItemRegistry(SensibleToolboxPlugin plugin) {
        this.plugin = plugin;
    }

    public void registerItems() {
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
        registerItem(new HyperSenderModule(), plugin, CONFIG_NODE, PERMISSION_NODE);
        registerItem(new ReceiverModule(), plugin, CONFIG_NODE, PERMISSION_NODE);
        registerItem(new SorterModule(), plugin, CONFIG_NODE, PERMISSION_NODE);
        registerItem(new VacuumModule(), plugin, CONFIG_NODE, PERMISSION_NODE);
        registerItem(new BreakerModule(), plugin, CONFIG_NODE, PERMISSION_NODE);
        registerItem(new StackModule(), plugin, CONFIG_NODE, PERMISSION_NODE);
        registerItem(new SpeedModule(), plugin, CONFIG_NODE, PERMISSION_NODE);
        registerItem(new TenKEnergyCell(), plugin, CONFIG_NODE, PERMISSION_NODE);
        registerItem(new FiftyKEnergyCell(), plugin, CONFIG_NODE, PERMISSION_NODE);
        registerItem(new TenKBatteryBox(), plugin, CONFIG_NODE, PERMISSION_NODE);
        registerItem(new FiftyKBatteryBox(), plugin, CONFIG_NODE, PERMISSION_NODE);
        registerItem(new SpeedUpgrade(), plugin, CONFIG_NODE, PERMISSION_NODE);
        registerItem(new EjectorUpgrade(), plugin, CONFIG_NODE, PERMISSION_NODE);
        registerItem(new RegulatorUpgrade(), plugin, CONFIG_NODE, PERMISSION_NODE);
        registerItem(new ThoroughnessUpgrade(), plugin, CONFIG_NODE, PERMISSION_NODE);
        registerItem(new HeatEngine(), plugin, CONFIG_NODE, PERMISSION_NODE);
        registerItem(new BasicSolarCell(), plugin, CONFIG_NODE, PERMISSION_NODE);
        registerItem(new DenseSolar(), plugin, CONFIG_NODE, PERMISSION_NODE);
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
        registerItem(new PVCell(), plugin, CONFIG_NODE, PERMISSION_NODE);
        registerItem(new AutoBuilder(), plugin, CONFIG_NODE, PERMISSION_NODE);
        if (plugin.isProtocolLibEnabled()) {
            registerItem(new SoundMuffler(), plugin, CONFIG_NODE, PERMISSION_NODE);
        }
    }

    public void registerItem(BaseSTBItem item, Plugin plugin) {
        registerItem(item, plugin, null, null);
    }

    public void registerItem(BaseSTBItem item, Plugin plugin, String configNode) {
        registerItem(item, plugin, configNode, null);
    }

    public void registerItem(BaseSTBItem item, Plugin plugin, String configNode, String permissionNode) {
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

        Bukkit.getPluginManager().addPermission(new Permission(permissionNode + ".craft." + id, PermissionDefault.TRUE));
        Bukkit.getPluginManager().addPermission(new Permission(permissionNode + ".interact." + id, PermissionDefault.TRUE));

        if (item instanceof BaseSTBBlock) {
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

    /**
     * Get a set of all known STB item ID's.
     *
     * @return all know STB item ID's
     */
    public Set<String> getItemIds() {
        return id2class.keySet();
    }

    /**
     * Construct and return an STB item from a supplied ItemStack.
     *
     * @param stack the item stack
     * @return the STB item, or null if the item stack is not an STB item
     */
    public BaseSTBItem fromItemStack(ItemStack stack) {
        if (!isSTBItem(stack)) {
            return null;
        }
        Configuration conf = getItemAttributes(stack);
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
     * @param <T>   the parameterised type; a subclass of BaseSTBItem
     * @return the STB item, or null if the item stack is not an STB item of the desired class
     */
    public <T extends BaseSTBItem> T fromItemStack(ItemStack stack, Class<T> type) {
        BaseSTBItem item = fromItemStack(stack);
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
    public BaseSTBItem getItemById(String id) {
        return getItemById(id, null);
    }

    /**
     * Construct and return an STB item.
     *
     * @param id   the item ID
     * @param conf item's frozen configuration data
     * @return the STB item
     */
    public BaseSTBItem getItemById(String id, ConfigurationSection conf) {
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
    public boolean isSTBItem(ItemStack stack) {
        return isSTBItem(stack, null);
    }

    /**
     * Check if the given item stack is an STB item of the given STB subclass
     *
     * @param stack the item stack to check
     * @param c     a subclass of BaseSTBItem
     * @return true if this item stack is an STB item of (or extending) the given class
     */
    public boolean isSTBItem(ItemStack stack, Class<? extends BaseSTBItem> c) {
        if (stack == null || !stack.hasItemMeta()) {
            return false;
        }
        ItemMeta im = stack.getItemMeta();
        if (im.hasLore()) {
            List<String> lore = im.getLore();
            if (!lore.isEmpty() && lore.get(0).startsWith(LORE_PREFIX)) {
                if (c != null) {
                    Configuration conf = getItemAttributes(stack);
                    Class<? extends BaseSTBItem> c2 = id2class.get(conf.getString("*TYPE"));
                    return c2 != null && c.isAssignableFrom(c2);
                } else {
                    return true;
                }
            }
        }
        return false;
    }


    private Configuration getItemAttributes(ItemStack stack) {
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

    public String getProviderName(BaseSTBItem item) {
        return id2plugin.get(item.getItemTypeID());
    }

    public String getPermissionPrefix(BaseSTBItem item) {
        return permissionPrefix.get(item.getItemTypeID());
    }

    public Class<? extends BaseSTBItem> getCraftingRestriction(BaseSTBItem item, Material mat) {
        return craftingRestrictions.get(item.getItemTypeID() + ":" + mat);
    }

    public void addCraftingRestriction(BaseSTBItem item, Material mat, Class<? extends BaseSTBItem> c) {
        craftingRestrictions.put(item.getItemTypeID() + ":" + mat, c);
    }
}
