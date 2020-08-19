package io.github.thebusybiscuit.sensibletoolbox.blocks.machines;

import java.util.Iterator;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;

import io.github.thebusybiscuit.sensibletoolbox.api.SensibleToolbox;
import io.github.thebusybiscuit.sensibletoolbox.api.items.AbstractIOMachine;
import io.github.thebusybiscuit.sensibletoolbox.api.items.BaseSTBItem;
import io.github.thebusybiscuit.sensibletoolbox.api.recipes.CustomRecipeManager;
import io.github.thebusybiscuit.sensibletoolbox.api.recipes.RecipeUtil;
import io.github.thebusybiscuit.sensibletoolbox.api.recipes.SimpleCustomRecipe;
import io.github.thebusybiscuit.sensibletoolbox.items.components.MachineFrame;
import io.github.thebusybiscuit.sensibletoolbox.items.components.SimpleCircuit;

public class Smelter extends AbstractIOMachine {

    private static int getProcessingTime(ItemStack stack) {
        if (stack.getType().isEdible()) {
            return 40; // food cooks a lot quicker than ores etc.
        }

        return 120;
    }

    public Smelter() {}

    public Smelter(ConfigurationSection conf) {
        super(conf);
    }

    @Override
    public void addCustomRecipes(CustomRecipeManager crm) {
        // add a corresponding smelter recipe for every known vanilla furnace recipe
        Iterator<Recipe> iter = Bukkit.recipeIterator();
        while (iter.hasNext()) {
            Recipe r = iter.next();

            if (r instanceof FurnaceRecipe) {
                FurnaceRecipe fr = (FurnaceRecipe) r;
                if (RecipeUtil.isVanillaSmelt(fr.getInput().getType())) {
                    crm.addCustomRecipe(new SimpleCustomRecipe(this, fr.getInput(), fr.getResult(), getProcessingTime(fr.getInput())));
                }
            }
        }

        // add a processing recipe for any STB item which reports itself as smeltable
        for (String key : SensibleToolbox.getItemRegistry().getItemIds()) {
            BaseSTBItem item = SensibleToolbox.getItemRegistry().getItemById(key);

            if (item.getSmeltingResult() != null) {
                ItemStack stack = item.toItemStack();
                crm.addCustomRecipe(new SimpleCustomRecipe(this, stack, item.getSmeltingResult(), getProcessingTime(stack)));
            }
        }
    }

    @Override
    public int getProgressItemSlot() {
        return 12;
    }

    @Override
    public int getProgressCounterSlot() {
        return 3;
    }

    @Override
    public ItemStack getProgressIcon() {
        return new ItemStack(Material.FLINT_AND_STEEL);
    }

    @Override
    public Material getMaterial() {
        return Material.LIGHT_BLUE_TERRACOTTA;
    }

    @Override
    public String getItemName() {
        return "Smelter";
    }

    @Override
    public String[] getLore() {
        return new String[] { "Smelts items", "Like a Furnace, but", "faster and more efficient" };
    }

    @Override
    public Recipe getRecipe() {
        SimpleCircuit sc = new SimpleCircuit();
        MachineFrame mf = new MachineFrame();
        registerCustomIngredients(sc, mf);
        ShapedRecipe recipe = new ShapedRecipe(getKey(), toItemStack());
        recipe.shape("CSC", "IFI", "RGR");
        recipe.setIngredient('C', Material.BRICK);
        recipe.setIngredient('S', Material.FURNACE);
        recipe.setIngredient('I', sc.getMaterial());
        recipe.setIngredient('F', mf.getMaterial());
        recipe.setIngredient('R', Material.REDSTONE);
        recipe.setIngredient('G', Material.GOLD_INGOT);
        return recipe;
    }

    @Override
    public int[] getInputSlots() {
        return new int[] { 10 };
    }

    @Override
    public int[] getOutputSlots() {
        return new int[] { 14 };
    }

    @Override
    public int[] getUpgradeSlots() {
        return new int[] { 41, 42, 43, 44 };
    }

    @Override
    public int getUpgradeLabelSlot() {
        return 40;
    }

    @Override
    public int getMaxCharge() {
        return 1000;
    }

    @Override
    public int getChargeRate() {
        return 20;
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
    protected void onMachineStartup() {
        if (SensibleToolbox.getPluginInstance().getConfigCache().isNoisyMachines()) {
            getLocation().getWorld().playSound(getLocation(), Sound.BLOCK_FIRE_AMBIENT, 1.0f, 1.0f);
        }
    }

    @Override
    protected void playActiveParticleEffect() {
        if (getTicksLived() % 20 == 0) {
            getLocation().getWorld().playEffect(getLocation(), Effect.MOBSPAWNER_FLAMES, 1);
        }
    }
}
