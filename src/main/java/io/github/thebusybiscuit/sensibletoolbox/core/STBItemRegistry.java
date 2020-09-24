package io.github.thebusybiscuit.sensibletoolbox.core;

import java.io.StringReader;
import java.lang.reflect.Constructor;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.Plugin;

import io.github.thebusybiscuit.cscorelib2.data.PersistentDataAPI;
import io.github.thebusybiscuit.sensibletoolbox.api.ItemRegistry;
import io.github.thebusybiscuit.sensibletoolbox.api.items.BaseSTBBlock;
import io.github.thebusybiscuit.sensibletoolbox.api.items.BaseSTBItem;
import io.github.thebusybiscuit.sensibletoolbox.core.storage.LocationManager;
import me.desht.dhutils.LogUtils;

public class STBItemRegistry implements ItemRegistry, Keyed {

    public static final String LORE_PREFIX = ChatColor.DARK_GRAY.toString() + ChatColor.ITALIC + "\u25b9";
    public static final int MAX_ITEM_ID_LENGTH = 32;

    private final Map<String, ReflectionDetails> reflectionDetailsMap = new HashMap<>();
    private final Map<String, Class<? extends BaseSTBItem>> craftingRestrictions = new HashMap<>();
    private final Map<String, String> permissionPrefix = new HashMap<>();
    private final Map<String, Plugin> id2plugin = new HashMap<>();
    private final NamespacedKey namespacedKey;

    @ParametersAreNonnullByDefault
    public STBItemRegistry(Plugin plugin, String registryKey) {
        Validate.notNull(plugin, "The Plugin cannot be null");
        Validate.notNull(registryKey, "The registry cannot be null");

        this.namespacedKey = new NamespacedKey(plugin, registryKey);
    }

    @Override
    public NamespacedKey getKey() {
        return namespacedKey;
    }

    @Override
    public void registerItem(BaseSTBItem item, Plugin plugin) {
        registerItem(item, plugin, null, null);
    }

    @Override
    public void registerItem(BaseSTBItem item, Plugin plugin, String configNode) {
        registerItem(item, plugin, configNode, null);
    }

    @Override
    public void registerItem(BaseSTBItem item, Plugin plugin, String configPrefix, String permissionPrefix) {
        String id = item.getItemTypeID();

        if (configPrefix != null) {
            if (!plugin.getConfig().contains(configPrefix + "." + item.getItemTypeID())) {
                plugin.getConfig().set(configPrefix + "." + item.getItemTypeID(), true);
                plugin.saveConfig();
            }
            if (!plugin.getConfig().getBoolean(configPrefix + "." + item.getItemTypeID())) return;
        }

        Validate.isTrue(id.length() <= MAX_ITEM_ID_LENGTH, "Item ID '" + id + "' is too long! (32 chars max)");
        Constructor<? extends BaseSTBItem> ctor0, ctor1;

        try {
            ctor0 = item.getClass().getConstructor();
        }
        catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("class " + item.getClass() + " does not have a 0-argument constructor!");
        }
        try {
            ctor1 = item.getClass().getConstructor(ConfigurationSection.class);
        }
        catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("class " + item.getClass() + " does not have a 1-argument (ConfigurationSection) constructor!");
        }
        ReflectionDetails details = new ReflectionDetails(item.getClass(), ctor0, ctor1);
        reflectionDetailsMap.put(id, details);
        id2plugin.put(id, plugin);

        if (permissionPrefix == null) {
            permissionPrefix = "stb";
        }
        this.permissionPrefix.put(id, permissionPrefix);

        // parent permission node
        Bukkit.getPluginManager().addPermission(new Permission(permissionPrefix + ".allow." + id, PermissionDefault.TRUE));

        registerPermission(permissionPrefix, BaseSTBItem.ItemAction.CRAFT, id);
        registerPermission(permissionPrefix, BaseSTBItem.ItemAction.INTERACT, id);

        if (item instanceof BaseSTBBlock) {
            registerPermission(permissionPrefix, BaseSTBItem.ItemAction.PLACE, id);
            registerPermission(permissionPrefix, BaseSTBItem.ItemAction.BREAK, id);
            registerPermission(permissionPrefix, BaseSTBItem.ItemAction.INTERACT_BLOCK, id);
            try {
                LocationManager.getManager().loadDeferredBlocks(id);
            }
            catch (SQLException e) {
                LogUtils.severe("There was a problem restoring blocks of type '" + id + "' from persisted storage:");
                e.printStackTrace();
            }
        }
    }

    private void registerPermission(String permissionPrefix, BaseSTBItem.ItemAction action, String id) {
        Permission perm = new Permission(permissionPrefix + "." + action.getNode() + "." + id, PermissionDefault.TRUE);
        perm.addParent(permissionPrefix + ".allow." + id, true);
        Bukkit.getPluginManager().addPermission(perm);
    }

    @Override
    public Set<String> getItemIds() {
        return reflectionDetailsMap.keySet();
    }

    @Override
    public BaseSTBItem fromItemStack(ItemStack stack) {
        Configuration conf = getItemAttributes(stack);
        BaseSTBItem item = getItemById(conf.getString("*TYPE"), conf);

        if (item != null) {
            item.storeEnchants(stack);
        }

        return item;
    }

    @Nonnull
    private Configuration getItemAttributes(@Nonnull ItemStack stack) {
        Validate.notNull(stack, "ItemStack cannot be null!");

        Optional<String> optional = PersistentDataAPI.getOptionalString(stack.getItemMeta(), namespacedKey);

        if (optional.isPresent()) {
            return YamlConfiguration.loadConfiguration(new StringReader(optional.get()));
        }
        else {
            return new MemoryConfiguration();
        }
    }

    @Override
    public <T extends BaseSTBItem> T fromItemStack(ItemStack stack, Class<T> type) {
        BaseSTBItem item = fromItemStack(stack);

        if (item != null && type.isAssignableFrom(item.getClass())) {
            return type.cast(item);
        }
        else {
            return null;
        }
    }

    @Override
    public BaseSTBItem getItemById(String id) {
        return getItemById(id, null);
    }

    @Override
    public BaseSTBItem getItemById(String id, ConfigurationSection conf) {
        ReflectionDetails details = reflectionDetailsMap.get(id);

        if (details == null) {
            return null;
        }

        try {
            return conf == null ? details.ctor0arg.newInstance() : details.ctor1arg.newInstance(conf);
        }
        catch (Exception e) {
            LogUtils.warning("failed to create STB item from item ID: " + id);
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean isSTBItem(ItemStack stack) {
        return isSTBItem(stack, null);
    }

    @Override
    public boolean isSTBItem(ItemStack stack, Class<? extends BaseSTBItem> c) {
        BaseSTBItem item = fromItemStack(stack);
        if (c == null) {
            return item != null;
        }
        else {
            return c.isInstance(item);
        }
    }

    public Plugin getPlugin(BaseSTBItem item) {
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

    private class ReflectionDetails {

        private final Class<? extends BaseSTBItem> clazz;
        private final Constructor<? extends BaseSTBItem> ctor0arg;
        private final Constructor<? extends BaseSTBItem> ctor1arg;

        private ReflectionDetails(Class<? extends BaseSTBItem> clazz, Constructor<? extends BaseSTBItem> ctor0arg, Constructor<? extends BaseSTBItem> ctor1arg) {
            this.clazz = clazz;
            this.ctor0arg = ctor0arg;
            this.ctor1arg = ctor1arg;
        }
    }
}
