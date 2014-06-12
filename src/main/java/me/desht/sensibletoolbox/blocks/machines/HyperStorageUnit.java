package me.desht.sensibletoolbox.blocks.machines;

import me.desht.dhutils.ItemNames;
import me.desht.sensibletoolbox.items.components.SimpleCircuit;
import me.desht.sensibletoolbox.util.STBUtil;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.TreeSpecies;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.material.MaterialData;

import java.util.Arrays;

public class HyperStorageUnit extends BigStorageUnit {
    private static final MaterialData md = STBUtil.makeLog(TreeSpecies.ACACIA);

    public HyperStorageUnit() {
        super();
    }

    public HyperStorageUnit(ConfigurationSection conf) {
        super(conf);
    }

    @Override
    public MaterialData getMaterialData() {
        return md;
    }

    @Override
    public String getItemName() {
        return "HSU";
    }

    @Override
    public Recipe getRecipe() {
        SimpleCircuit sc = new SimpleCircuit();
        BigStorageUnit bsu = new BigStorageUnit();
        registerCustomIngredients(sc, bsu);
        ShapedRecipe recipe = new ShapedRecipe(toItemStack());
        recipe.shape("OEO", "SBS", "RGR");
        recipe.setIngredient('O', Material.OBSIDIAN);
        recipe.setIngredient('S', sc.getMaterialData());
        recipe.setIngredient('B', bsu.getMaterialData());
        recipe.setIngredient('E', Material.ENDER_PEARL);
        recipe.setIngredient('R', Material.REDSTONE);
        recipe.setIngredient('G', Material.GOLD_INGOT);
        return recipe;
    }

    @Override
    public Recipe[] getExtraRecipes() {
        return new Recipe[0];
    }

    @Override
    public int getMaxCharge() {
        return 1000;
    }

    @Override
    public int getChargeRate() {
        return 10;
    }

    @Override
    public int getChargeMeterSlot() {
        return 35;
    }

    public int getStackCapacity() {
        return 33554431;  // 2^31 items for a 64-item stack
    }

    public int getEnergyCellSlot() {
        return 36;
    }

    public int getChargeDirectionSlot() {
        return 37;
    }

    @Override
    public int getInventoryGUISize() {
        return 45;
    }

    @Override
    public boolean acceptsEnergy(BlockFace face) {
        return true;
    }

    @Override
    protected boolean dropsItemsOnBreak() {
        return false;
    }

    @Override
    public String[] getLore() {
        return new String[]{
                "Hyper Storage Unit",
                "Stores up to " + getStackCapacity() + " stacks",
                "of a single item type",
                "Keeps storage when broken",
                "Needs power to function"
        };
    }

    @Override
    public String[] getExtraLore() {
        String[] l = super.getExtraLore();
        if (getTotalAmount() > 0) {
            String[] l2 = Arrays.copyOf(l, l.length + 1);
            l2[l2.length - 1] = ChatColor.WHITE + "Stored: " + ChatColor.YELLOW + getTotalAmount() + " " + ItemNames.lookup(getStoredItemType());
            return l2;
        } else {
            return l;
        }
    }

    @Override
    public double getChargePerOperation(int nItems) {
        return 0.05 * nItems;
    }

    @Override
    public void setLocation(Location loc) {
        if (loc == null) {
            // move all output items into storage so they don't get dropped
            ItemStack output = getOutputItem();
            if (output != null) {
                setStorageAmount(getStorageAmount() + output.getAmount());
                setOutputAmount(0);
                setOutputItem(null);
            }
        }
        super.setLocation(loc);
    }
}
