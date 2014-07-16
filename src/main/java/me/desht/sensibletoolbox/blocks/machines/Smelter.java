package me.desht.sensibletoolbox.blocks.machines;

import me.desht.dhutils.ParticleEffect;
import me.desht.sensibletoolbox.SensibleToolboxPlugin;
import me.desht.sensibletoolbox.api.SensibleToolbox;
import me.desht.sensibletoolbox.api.items.AbstractIOMachine;
import me.desht.sensibletoolbox.api.items.BaseSTBItem;
import me.desht.sensibletoolbox.api.recipes.CustomRecipeManager;
import me.desht.sensibletoolbox.api.recipes.RecipeUtil;
import me.desht.sensibletoolbox.api.recipes.SimpleCustomRecipe;
import me.desht.sensibletoolbox.api.util.STBUtil;
import me.desht.sensibletoolbox.items.components.MachineFrame;
import me.desht.sensibletoolbox.items.components.SimpleCircuit;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.material.MaterialData;

import java.util.Iterator;

public class Smelter extends AbstractIOMachine {
    private static final MaterialData md = STBUtil.makeColouredMaterial(Material.STAINED_CLAY, DyeColor.LIGHT_BLUE);

    private static int getProcessingTime(ItemStack stack) {
        if (stack.getType().isEdible()) {
            return 40;  // food cooks a lot quicker than ores etc.
        }
        return 120;
    }

    public Smelter() {
    }

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
    public Material getProgressIcon() {
        return Material.FLINT_AND_STEEL;
    }

    @Override
    public MaterialData getMaterialData() {
        return md;
    }

    @Override
    public String getItemName() {
        return "Smelter";
    }

    @Override
    public String[] getLore() {
        return new String[]{"Smelts items", "Like a Furnace, but", "faster and more efficient"};
    }

    @Override
    public Recipe getRecipe() {
        SimpleCircuit sc = new SimpleCircuit();
        MachineFrame mf = new MachineFrame();
        registerCustomIngredients(sc, mf);
        ShapedRecipe recipe = new ShapedRecipe(toItemStack());
        recipe.shape("CSC", "IFI", "RGR");
        recipe.setIngredient('C', Material.BRICK);
        recipe.setIngredient('S', Material.FURNACE);
        recipe.setIngredient('I', sc.getMaterialData());
        recipe.setIngredient('F', mf.getMaterialData());
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
        getLocation().getWorld().playSound(getLocation(), Sound.FIRE, 1.0f, 1.0f);
    }

    @Override
    protected void playActiveParticleEffect() {
        if (SensibleToolboxPlugin.getInstance().isProtocolLibEnabled() && getTicksLived() % 20 == 0) {
            ParticleEffect.FLAME.play(getLocation().add(0.5, 0.5, 0.5), 0.4f, 0.4f, 0.4f, 0.001f, 10);
        }
    }
}
