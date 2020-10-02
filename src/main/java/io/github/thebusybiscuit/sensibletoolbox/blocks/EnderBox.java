package io.github.thebusybiscuit.sensibletoolbox.blocks;

import java.util.UUID;

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

import io.github.thebusybiscuit.sensibletoolbox.api.STBInventoryHolder;
import io.github.thebusybiscuit.sensibletoolbox.api.enderstorage.EnderStorage;
import io.github.thebusybiscuit.sensibletoolbox.api.enderstorage.EnderStorageHolder;
import io.github.thebusybiscuit.sensibletoolbox.api.enderstorage.EnderTunable;
import io.github.thebusybiscuit.sensibletoolbox.api.items.BaseSTBBlock;
import io.github.thebusybiscuit.sensibletoolbox.util.STBUtil;
import io.github.thebusybiscuit.sensibletoolbox.util.UnicodeSymbol;

public class EnderBox extends BaseSTBBlock implements EnderTunable, STBInventoryHolder {

    private int frequency;
    private boolean global;
    private final String signLabel[] = new String[4];

    public EnderBox() {
        setEnderFrequency(1);
        setGlobal(false);
    }

    public EnderBox(ConfigurationSection conf) {
        super(conf);
        setEnderFrequency(conf.getInt("frequency"));
        setGlobal(conf.getBoolean("global"));
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
        update(false);
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
        update(false);
        updateAttachedLabelSigns();
    }

    @Override
    public Material getMaterial() {
        return Material.ENDER_CHEST;
    }

    @Override
    public String getItemName() {
        return "Ender Box";
    }

    @Override
    public String getDisplaySuffix() {
        return (isGlobal() ? "Global" : "Personal") + " " + UnicodeSymbol.NUMBER.toUnicode() + getEnderFrequency();
    }

    @Override
    public String[] getLore() {
        return new String[] { "Extra-dimensional storage", "Right-click with an", "Ender Tuner to set", "its frequency" };
    }

    @Override
    protected String[] getSignLabel(BlockFace face) {
        String[] label = super.getSignLabel(face);
        System.arraycopy(signLabel, 1, label, 1, 3);
        return label;
    }

    @Override
    public Recipe getRecipe() {
        ShapedRecipe recipe = new ShapedRecipe(getKey(), toItemStack(1));
        recipe.shape("GDG", "GEG", "GGG");
        recipe.setIngredient('G', Material.GOLD_INGOT);
        recipe.setIngredient('D', Material.DIAMOND);
        recipe.setIngredient('E', Material.ENDER_CHEST);
        return recipe;
    }

    @Override
    public void onInteractBlock(PlayerInteractEvent event) {
        super.onInteractBlock(event);

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && !event.getPlayer().isSneaking()) {
            Player player = event.getPlayer();

            if (!hasAccessRights(player)) {
                STBUtil.complain(player, "That " + getItemName() + " is private!");
            }
            else {
                Inventory inv = isGlobal() ? EnderStorage.getEnderInventory(getEnderFrequency()) : EnderStorage.getEnderInventory(player, getEnderFrequency());
                player.openInventory(inv);
                player.playSound(getLocation(), Sound.BLOCK_CHEST_OPEN, 0.5f, 1.0f);
            }
            event.setCancelled(true);
        }
    }

    @Override
    public int insertItems(ItemStack item, BlockFace face, boolean sorting, UUID uuid) {
        if (hasAccessRights(uuid)) {
            return getInventoryHolderFor(uuid).insertItems(item, face, sorting, uuid);
        }
        else {
            return 0;
        }
    }

    @Override
    public ItemStack extractItems(BlockFace face, ItemStack receiver, int amount, UUID uuid) {
        if (hasAccessRights(uuid)) {
            return getInventoryHolderFor(uuid).extractItems(face, receiver, amount, uuid);
        }
        else {
            return null;
        }
    }

    @Override
    public Inventory showOutputItems(UUID uuid) {
        return hasAccessRights(uuid) ? getInventoryHolderFor(uuid).showOutputItems(uuid) : null;
    }

    @Override
    public void updateOutputItems(UUID uuid, Inventory inventory) {
        if (hasAccessRights(uuid)) {
            getInventoryHolderFor(uuid).updateOutputItems(uuid, inventory);
        }
    }

    @Override
    public Inventory getInventory() {
        return getInventoryHolderFor(getOwner()).getInventory();
    }

    private EnderStorageHolder getInventoryHolderFor(UUID uuid) {
        return isGlobal() ? EnderStorage.getEnderStorageHolder(getEnderFrequency()) : EnderStorage.getEnderStorageHolder(Bukkit.getOfflinePlayer(uuid), getEnderFrequency());
    }
}
