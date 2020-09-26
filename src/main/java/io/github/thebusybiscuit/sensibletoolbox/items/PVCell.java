package io.github.thebusybiscuit.sensibletoolbox.items;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import io.github.thebusybiscuit.sensibletoolbox.api.SensibleToolbox;
import io.github.thebusybiscuit.sensibletoolbox.api.items.BaseSTBBlock;
import io.github.thebusybiscuit.sensibletoolbox.api.items.BaseSTBItem;
import io.github.thebusybiscuit.sensibletoolbox.blocks.machines.BasicSolarCell;
import io.github.thebusybiscuit.sensibletoolbox.items.components.SiliconWafer;
import io.github.thebusybiscuit.sensibletoolbox.util.STBUtil;

public class PVCell extends BaseSTBItem {

    public static final int MAX_LIFESPAN = 24000 * 9; // 9 minecraft days; 3 real hours
    // private static final int MAX_LIFESPAN = 2000; // 100 real seconds (testing)

    private int lifespan;

    public PVCell() {
        lifespan = MAX_LIFESPAN;
    }

    public PVCell(ConfigurationSection conf) {
        super(conf);
        lifespan = conf.getInt("lifespan");
    }

    @Override
    public YamlConfiguration freeze() {
        YamlConfiguration conf = super.freeze();
        conf.set("lifespan", lifespan);
        return conf;
    }

    public int getLifespan() {
        return lifespan;
    }

    public void setLifespan(int lifespan) {
        this.lifespan = Math.max(0, Math.min(MAX_LIFESPAN, lifespan));
    }

    public void reduceLifespan(int amount) {
        this.lifespan = Math.max(0, lifespan - amount);
    }

    @Override
    public Material getMaterial() {
        return Material.LEATHER_HELMET;
    }

    @Override
    public String getItemName() {
        return "PV Cell";
    }

    @Override
    public String[] getLore() {
        return new String[] { "Photovoltaic Cell", "Insert into a solar generator", "R-click solar: insert directly" };
    }

    @Override
    public String[] getExtraLore() {
        return new String[] { formatCellLife(lifespan) };
    }

    @Override
    public Recipe getRecipe() {
        ShapedRecipe recipe = new ShapedRecipe(getKey(), toItemStack());
        SiliconWafer sw = new SiliconWafer();
        registerCustomIngredients(sw);
        recipe.shape("LRL", "GSG");
        recipe.setIngredient('L', Material.LAPIS_LAZULI); // lapis
        recipe.setIngredient('R', Material.REDSTONE);
        recipe.setIngredient('G', Material.GOLD_NUGGET);
        recipe.setIngredient('S', sw.getMaterial());
        return recipe;
    }

    @Override
    public boolean isWearable() {
        return false;
    }

    @Override
    public void onInteractItem(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Player player = event.getPlayer();

            if (event.getClickedBlock() != null) {
                BaseSTBBlock stb = SensibleToolbox.getBlockAt(event.getClickedBlock().getLocation(), true);

                if (stb instanceof BasicSolarCell) {
                    int nInserted = ((BasicSolarCell) stb).insertItems(event.getItem(), event.getBlockFace(), false, player.getUniqueId());

                    if (nInserted > 0) {
                        player.getInventory().setItemInMainHand(null);
                        player.playSound(event.getClickedBlock().getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 0.6f);
                    }
                }
            }
            player.updateInventory();
            event.setCancelled(true);
        }
    }

    @Override
    public ItemStack toItemStack(int amount) {
        ItemStack res = super.toItemStack(amount);
        ItemMeta meta = res.getItemMeta();

        if (meta instanceof LeatherArmorMeta) {
            ((LeatherArmorMeta) meta).setColor(Color.NAVY);
            res.setItemMeta(meta);
        }

        STBUtil.levelToDurability(res, lifespan, MAX_LIFESPAN);
        return res;
    }

    /**
     * Create a nicely formatted string representing a cell's lifetime.
     *
     * @param lifespan
     *            the life span
     * @return a formatted string
     */
    public static String formatCellLife(int lifespan) {
        int sec = lifespan / 20;

        if (sec >= 60) {
            return ChatColor.WHITE + "Lifetime: " + ChatColor.YELLOW.toString() + (sec / 60) + " min";
        }
        else {
            return ChatColor.WHITE + "Lifetime: " + ChatColor.YELLOW.toString() + sec + " sec";
        }
    }
}
