package io.github.thebusybiscuit.sensibletoolbox.blocks.machines;

import java.util.Arrays;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;

import io.github.thebusybiscuit.cscorelib2.inventory.ItemUtils;
import io.github.thebusybiscuit.sensibletoolbox.items.components.IntegratedCircuit;

public class HyperStorageUnit extends BigStorageUnit {

    public HyperStorageUnit() {
        super();
    }

    public HyperStorageUnit(ConfigurationSection conf) {
        super(conf);
    }

    @Override
    public Material getMaterial() {
        return Material.ACACIA_LOG;
    }

    @Override
    public String getItemName() {
        return "HSU";
    }

    @Override
    public Recipe getRecipe() {
        IntegratedCircuit ic = new IntegratedCircuit();
        BigStorageUnit bsu = new BigStorageUnit();
        registerCustomIngredients(ic, bsu);
        ShapedRecipe recipe = new ShapedRecipe(getKey(), toItemStack());
        recipe.shape("OIO", "EBE", "RGR");
        recipe.setIngredient('O', Material.OBSIDIAN);
        recipe.setIngredient('I', ic.getMaterial());
        recipe.setIngredient('E', Material.ENDER_PEARL);
        recipe.setIngredient('B', bsu.getMaterial());
        recipe.setIngredient('R', Material.REDSTONE);
        recipe.setIngredient('G', Material.GOLD_INGOT);
        return recipe;
    }

    @Override
    public Recipe[] getExtraRecipes() {
        return new Recipe[0];
    }

    @Override
    public String getCraftingNotes() {
        return null;
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

    @Override
    public int getStackCapacity() {
        return 33554431; // 2^31 items for a 64-item stack
    }

    @Override
    public int getEnergyCellSlot() {
        return 36;
    }

    @Override
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
        return new String[] { "Hyper Storage Unit", "Stores up to " + getStackCapacity() + " stacks", "of a single item type", "Keeps storage when broken", "Needs power to function" };
    }

    @Override
    public String[] getExtraLore() {
        if (getTotalAmount() > 0) {
            String[] l = super.getExtraLore();
            String[] l2 = Arrays.copyOf(l, l.length + 1);
            l2[l2.length - 1] = ChatColor.WHITE + "Stored: " + ChatColor.YELLOW + getTotalAmount() + " " + ItemUtils.getItemName(getStoredItemType());
            return l2;
        } else {
            return super.getExtraLore();
        }
    }

    @Override
    public double getChargePerOperation(int nItems) {
        return 0.05 * nItems;
    }

    @Override
    public void onBlockUnregistered(Location loc) {
        // move all output items into storage so they don't get dropped
        ItemStack output = getOutputItem();

        if (output != null) {
            setStorageAmount(getStorageAmount() + output.getAmount());
            setOutputAmount(0);
            setOutputItem(null);
        }

        super.onBlockUnregistered(loc);
    }
}
