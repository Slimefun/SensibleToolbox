package me.desht.sensibletoolbox.blocks;

import me.desht.sensibletoolbox.SensibleToolboxPlugin;
import me.desht.sensibletoolbox.api.EnderTunable;
import me.desht.sensibletoolbox.enderstorage.EnderStorageManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.material.MaterialData;

public class EnderBox extends BaseSTBBlock implements EnderTunable {
    private static final MaterialData md = new MaterialData(Material.ENDER_CHEST);

    private int frequency;
    private final String signLabel[] = new String[4];

    public EnderBox() {
        setEnderFrequency(1);
        signLabel[0] = makeItemLabel();
    }

    public EnderBox(ConfigurationSection conf) {
        super(conf);
        setEnderFrequency(conf.getInt("frequency"));
        signLabel[0] = makeItemLabel();
    }

    @Override
    public YamlConfiguration freeze() {
        YamlConfiguration conf = super.freeze();
        conf.set("frequency", getEnderFrequency());
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
        return "ƒ " + getEnderFrequency();
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
            EnderStorageManager esm = SensibleToolboxPlugin.getInstance().getEnderStorageManager();
            Inventory inv = esm.getPlayerInventory(player, getEnderFrequency());
            player.openInventory(inv);
            player.playSound(getLocation(), Sound.CHEST_OPEN, 0.5f, 1.0f);
            event.setCancelled(true);
        }
    }
}
