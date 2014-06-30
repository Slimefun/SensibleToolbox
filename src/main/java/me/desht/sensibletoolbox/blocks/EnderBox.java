package me.desht.sensibletoolbox.blocks;

import me.desht.sensibletoolbox.SensibleToolboxPlugin;
import me.desht.sensibletoolbox.api.EnderTunable;
import me.desht.sensibletoolbox.api.STBInventoryHolder;
import me.desht.sensibletoolbox.enderstorage.EnderStorageHolder;
import me.desht.sensibletoolbox.enderstorage.EnderStorageManager;
import me.desht.sensibletoolbox.util.STBUtil;
import me.desht.sensibletoolbox.util.VanillaInventoryUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.material.MaterialData;

import java.util.UUID;

public class EnderBox extends BaseSTBBlock implements EnderTunable, STBInventoryHolder {
    private static final MaterialData md = new MaterialData(Material.ENDER_CHEST);

    private int frequency;
    private boolean global;
    private final String signLabel[] = new String[4];

    public EnderBox() {
        setEnderFrequency(1);
        setGlobal(false);
        signLabel[0] = makeItemLabel();
    }

    public EnderBox(ConfigurationSection conf) {
        super(conf);
        setEnderFrequency(conf.getInt("frequency"));
        setGlobal(conf.getBoolean("global"));
        signLabel[0] = makeItemLabel();
    }

    @Override
    public YamlConfiguration freeze() {
        YamlConfiguration conf = super.freeze();
        conf.set("frequency", getEnderFrequency());
        conf.set("global", isGlobal());
        return conf;
    }

    @Override
    public int getEnderFrequency() {
        return frequency;
    }

    @Override
    public void setEnderFrequency(int frequency) {
        this.frequency = frequency;
        signLabel[2] = ChatColor.DARK_RED + getDisplaySuffix();
        updateBlock(false);
        updateAttachedLabelSigns();
    }

    @Override
    public boolean isGlobal() {
        return global;
    }

    @Override
    public void setGlobal(boolean global) {
        this.global = global;
        signLabel[2] = ChatColor.DARK_RED + getDisplaySuffix();
        updateBlock(false);
        updateAttachedLabelSigns();
    }

    @Override
    public MaterialData getMaterialData() {
        return md;
    }

    @Override
    public String getItemName() {
        return "Ender Box";
    }

    @Override
    public String getDisplaySuffix() {
        return (isGlobal() ? "Global" : "Personal") + " ƒ" + getEnderFrequency();
    }

    @Override
    public String[] getLore() {
        return new String[] {"Extra-dimensional storage", "Right-click with an", "Ender Tuner to set", "its frequency"};
    }

    @Override
    protected String[] getSignLabel() {
        return signLabel;
    }

    @Override
    public Recipe getRecipe() {
        ShapedRecipe recipe = new ShapedRecipe(toItemStack(1));
        recipe.shape("GDG", "GEG", "GGG");
        recipe.setIngredient('G', Material.GOLD_INGOT);
        recipe.setIngredient('D', Material.DIAMOND);
        recipe.setIngredient('E', Material.ENDER_CHEST);
        return recipe;
    }

    @Override
    public void onInteractBlock(PlayerInteractEvent event) {
        super.onInteractBlock(event);

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Player player = event.getPlayer();
            if (hasAccessRights(player)) {
                EnderStorageManager esm = SensibleToolboxPlugin.getInstance().getEnderStorageManager();
                Inventory inv = isGlobal() ?
                        esm.getGlobalInventory(getEnderFrequency()) :
                        esm.getPlayerInventory(player, getEnderFrequency());
                player.openInventory(inv);
                player.playSound(getLocation(), Sound.CHEST_OPEN, 0.5f, 1.0f);
            } else {
                STBUtil.complain(player, "That " + getItemName() + " is private!");
            }
            event.setCancelled(true);
        }
    }

    @Override
    public int insertItems(ItemStack item, BlockFace face, boolean sorting, UUID uuid) {
        if (hasAccessRights(uuid)) {
            Inventory inv = getInventoryFor(uuid);
            int nInserted = VanillaInventoryUtils.vanillaInsertion(inv, item, item.getAmount(), face, sorting);
            if (nInserted > 0) {
                EnderStorageHolder holder = (EnderStorageHolder) inv.getHolder();
                holder.setChanged();
            }
            return nInserted;
        } else {
            return 0;
        }
    }

    @Override
    public ItemStack extractItems(BlockFace face, ItemStack receiver, int amount, UUID uuid) {
        if (hasAccessRights(uuid)) {
            Inventory inv = getInventoryFor(uuid);
            ItemStack stack = VanillaInventoryUtils.pullFromInventory(inv, amount, receiver, null);
            if (stack != null) {
                EnderStorageHolder holder = (EnderStorageHolder) inv.getHolder();
                holder.setChanged();
            }
            return stack;
        } else {
            return null;
        }
    }

    @Override
    public Inventory showOutputItems() {
        Inventory source = getInventory();
        Inventory res = Bukkit.createInventory(source.getHolder(), source.getSize());
        res.setContents(source.getContents());
        return res;
    }

    @Override
    public void updateOutputItems(Inventory inventory) {
        Inventory target = getInventory();
        target.setContents(inventory.getContents());
        EnderStorageHolder holder = (EnderStorageHolder) inventory.getHolder();
        holder.setChanged();
    }

    @Override
    public Inventory getInventory() {
        return getInventoryFor(getOwner());
    }

    private Inventory getInventoryFor(UUID uuid) {
        EnderStorageManager esm = SensibleToolboxPlugin.getInstance().getEnderStorageManager();
        return isGlobal() ?
                esm.getGlobalInventory(getEnderFrequency()) :
                esm.getPlayerInventory(Bukkit.getOfflinePlayer(uuid), getEnderFrequency());
    }
}
