package io.github.thebusybiscuit.sensibletoolbox.blocks.machines;

import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.RecipeChoice.MaterialChoice;
import org.bukkit.inventory.ShapedRecipe;

import io.github.thebusybiscuit.sensibletoolbox.api.items.AbstractIOMachine;
import io.github.thebusybiscuit.sensibletoolbox.api.recipes.CustomRecipeManager;
import io.github.thebusybiscuit.sensibletoolbox.api.recipes.SimpleCustomRecipe;
import io.github.thebusybiscuit.sensibletoolbox.items.components.EnergizedQuartz;
import io.github.thebusybiscuit.sensibletoolbox.items.components.FishBait;
import io.github.thebusybiscuit.sensibletoolbox.items.components.MachineFrame;

public class Fermenter extends AbstractIOMachine {
	
    public Fermenter() {
    }

    public Fermenter(ConfigurationSection conf) {
        super(conf);
    }

    @Override
    public void addCustomRecipes(CustomRecipeManager crm) {
    	FishBait bait = new FishBait();

        crm.addCustomRecipe(new SimpleCustomRecipe(this, new ItemStack(Material.SPIDER_EYE), new ItemStack(Material.FERMENTED_SPIDER_EYE), 220));
        crm.addCustomRecipe(new SimpleCustomRecipe(this, new ItemStack(Material.ROTTEN_FLESH), bait.toItemStack(), 200));
    }

    @Override
    public Material getMaterial() {
        return Material.LIME_TERRACOTTA;
    }

    @Override
    public String getItemName() {
        return "Fermenter";
    }

    @Override
    public String[] getLore() {
        return new String[] {"Ferments various Item and is also", "used for the Creation of Fish Bait"};
    }

    @Override
    public Recipe getRecipe() {
        MachineFrame mf = new MachineFrame();
        EnergizedQuartz q = new EnergizedQuartz();
        registerCustomIngredients(mf, q);
        ShapedRecipe recipe = new ShapedRecipe(getKey(), toItemStack());
        recipe.shape("SQM", "IFI", "RGR");
        recipe.setIngredient('S', Material.SUGAR);
        recipe.setIngredient('Q', q.getMaterial());
        recipe.setIngredient('M', new MaterialChoice(Material.RED_MUSHROOM, Material.BROWN_MUSHROOM));
        recipe.setIngredient('I', Material.IRON_INGOT);
        recipe.setIngredient('F', mf.getMaterial());
        recipe.setIngredient('R', Material.REDSTONE);
        recipe.setIngredient('G', Material.GOLD_INGOT);
        return recipe;
    }

    @Override
    public int[] getInputSlots() {
        return new int[]{10};
    }

    @Override
    public int[] getOutputSlots() {
        return new int[]{14};
    }

    @Override
    public int[] getUpgradeSlots() {
        return new int[]{41, 42, 43, 44};
    }

    @Override
    public int getUpgradeLabelSlot() {
        return 40;
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
    public int getMaxCharge() {
        return 1000;
    }

    @Override
    public int getChargeRate() {
        return 20;
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
        return new ItemStack(Material.GOLDEN_HOE);
    }
    
    @Override
    protected void playActiveParticleEffect() {
        if (getTicksLived() % 20 == 0) {
            getLocation().getWorld().playEffect(getLocation(), Effect.STEP_SOUND, Material.OAK_LEAVES);
        }
    }
}
