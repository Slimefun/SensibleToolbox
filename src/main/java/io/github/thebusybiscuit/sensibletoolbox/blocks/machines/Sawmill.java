package io.github.thebusybiscuit.sensibletoolbox.blocks.machines;

import java.util.Optional;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.Tag;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.RecipeChoice.MaterialChoice;
import org.bukkit.inventory.ShapedRecipe;

import io.github.thebusybiscuit.cscorelib2.materials.MaterialConverter;
import io.github.thebusybiscuit.sensibletoolbox.api.SensibleToolbox;
import io.github.thebusybiscuit.sensibletoolbox.api.items.AbstractIOMachine;
import io.github.thebusybiscuit.sensibletoolbox.api.recipes.CustomRecipeManager;
import io.github.thebusybiscuit.sensibletoolbox.api.recipes.SimpleCustomRecipe;
import io.github.thebusybiscuit.sensibletoolbox.items.components.MachineFrame;
import io.github.thebusybiscuit.sensibletoolbox.items.components.SimpleCircuit;

public class Sawmill extends AbstractIOMachine {

    public Sawmill() {}

    public Sawmill(ConfigurationSection conf) {
        super(conf);
    }

    @Override
    public void addCustomRecipes(CustomRecipeManager crm) {
        for (Material log : Tag.LOGS.getValues()) {
            Optional<Material> planks = MaterialConverter.getPlanksFromLog(log);

            if (planks.isPresent()) {
                crm.addCustomRecipe(new SimpleCustomRecipe(this, new ItemStack(log), new ItemStack(planks.get(), 6), 60));
            }
        }

        crm.addCustomRecipe(new SimpleCustomRecipe(this, new ItemStack(Material.WOOD_DOOR), new ItemStack(Material.WOOD, 6), 40));
        crm.addCustomRecipe(new SimpleCustomRecipe(this, new ItemStack(Material.TRAP_DOOR), new ItemStack(Material.WOOD, 3), 40));
        crm.addCustomRecipe(new SimpleCustomRecipe(this, new ItemStack(Material.WOOD_PLATE), new ItemStack(Material.WOOD, 2), 40));
        crm.addCustomRecipe(new SimpleCustomRecipe(this, new ItemStack(Material.WOOD_BUTTON), new ItemStack(Material.WOOD, 1), 40));
        crm.addCustomRecipe(new SimpleCustomRecipe(this, new ItemStack(Material.CRAFTING_TABLE), new ItemStack(Material.WOOD, 4), 40));
        crm.addCustomRecipe(new SimpleCustomRecipe(this, new ItemStack(Material.SIGN), new ItemStack(Material.WOOD, 2), 40));
        crm.addCustomRecipe(new SimpleCustomRecipe(this, new ItemStack(Material.CHEST), new ItemStack(Material.OAK_PLANKS, 8), 40));
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
        return new ItemStack(Material.GOLDEN_AXE);
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
    protected void onMachineStartup() {
        if (SensibleToolbox.getPluginInstance().getConfigCache().isNoisyMachines()) {
            getLocation().getWorld().playSound(getLocation(), Sound.ENTITY_HORSE_STEP_WOOD, 1.0f, 0.5f);
        }
    }

    @Override
    public Material getMaterial() {
        return Material.BROWN_TERRACOTTA;
    }

    @Override
    public String getItemName() {
        return "Sawmill";
    }

    @Override
    public String[] getLore() {
        return new String[] { "Processes wooden items" };
    }

    @Override
    public Recipe getRecipe() {
        SimpleCircuit sc = new SimpleCircuit();
        MachineFrame mf = new MachineFrame();
        registerCustomIngredients(sc, mf);
        ShapedRecipe recipe = new ShapedRecipe(getKey(), toItemStack());
        recipe.shape("WAW", "IFI", "RGR");
        recipe.setIngredient('W', new MaterialChoice(Tag.PLANKS));
        recipe.setIngredient('A', Material.IRON_AXE);
        recipe.setIngredient('I', sc.getMaterial());
        recipe.setIngredient('F', mf.getMaterial());
        recipe.setIngredient('R', Material.REDSTONE);
        recipe.setIngredient('G', Material.GOLD_INGOT);
        return recipe;
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
}
