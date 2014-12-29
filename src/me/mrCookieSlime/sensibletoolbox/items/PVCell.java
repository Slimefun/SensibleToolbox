package me.mrCookieSlime.sensibletoolbox.items;

import me.mrCookieSlime.sensibletoolbox.api.SensibleToolbox;
import me.mrCookieSlime.sensibletoolbox.api.items.BaseSTBBlock;
import me.mrCookieSlime.sensibletoolbox.api.items.BaseSTBItem;
import me.mrCookieSlime.sensibletoolbox.api.util.STBUtil;
import me.mrCookieSlime.sensibletoolbox.blocks.machines.BasicSolarCell;
import me.mrCookieSlime.sensibletoolbox.items.components.SiliconWafer;

import org.bukkit.*;
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
import org.bukkit.material.MaterialData;

public class PVCell extends BaseSTBItem {
    private static final MaterialData md = new MaterialData(Material.LEATHER_HELMET);

    public static final int MAX_LIFESPAN = 24000 * 9;  // 9 minecraft days; 3 real hours
//    private static final int MAX_LIFESPAN = 2000; // 100 real seconds (testing)

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
    public MaterialData getMaterialData() {
        return md;
    }

    @Override
    public String getItemName() {
        return "PV Cell";
    }

    @Override
    public String[] getLore() {
        return new String[] { "Photovoltaic Cell", "Insert into a solar generator", "R-click solar: insert directly"};
    }

    @Override
    public String[] getExtraLore() {
        return new String[] { formatCellLife(lifespan) };
    }

    @Override
    public Recipe getRecipe() {
        ShapedRecipe recipe = new ShapedRecipe(toItemStack());
        SiliconWafer sw = new SiliconWafer();
        registerCustomIngredients(sw);
        recipe.shape("LRL", "GSG");
        recipe.setIngredient('L', STBUtil.makeColouredMaterial(Material.INK_SACK, DyeColor.BLUE)); // lapis
        recipe.setIngredient('R', Material.REDSTONE);
        recipe.setIngredient('G', Material.GOLD_NUGGET);
        recipe.setIngredient('S', sw.getMaterialData());
        return recipe;
    }


    @Override
    public boolean isWearable() {
        return false;
    }

    @SuppressWarnings("deprecation")
	@Override
    public void onInteractItem(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Player player = event.getPlayer();
            if (event.getClickedBlock() != null) {
                BaseSTBBlock stb = SensibleToolbox.getBlockAt(event.getClickedBlock().getLocation(), true);
                if (stb instanceof BasicSolarCell) {
                    int nInserted = ((BasicSolarCell) stb).insertItems(event.getItem(), event.getBlockFace(), false, player.getUniqueId());
                    if (nInserted > 0) {
                        player.setItemInHand(null);
                        player.playSound(event.getClickedBlock().getLocation(), Sound.CLICK, 1.0f, 0.6f);
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
     * @param lifespan the life span
     * @return a formatted string
     */
    public static String formatCellLife(int lifespan) {
        int sec = lifespan / 20;
        if (sec >= 60) {
            return ChatColor.RESET + "Lifetime: " + ChatColor.YELLOW.toString() + (sec / 60) + " min";
        } else {
            return ChatColor.RESET + "Lifetime: " + ChatColor.YELLOW.toString() + sec + " sec";
        }
    }
}
