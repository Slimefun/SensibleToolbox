package io.github.thebusybiscuit.sensibletoolbox.items.components;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapelessRecipe;

import io.github.thebusybiscuit.sensibletoolbox.api.items.BaseSTBItem;

public class EnergizedIronDust extends BaseSTBItem {

    public EnergizedIronDust() {
    }

    public EnergizedIronDust(ConfigurationSection conf) {

    }

    @Override
    public Material getMaterial() {
        return Material.GUNPOWDER;
    }

    @Override
    public String getItemName() {
        return "Energized Iron Dust";
    }

    @Override
    public String[] getLore() {
        return new String[] { "Smelt to get an energized iron ingot" };
    }

    @Override
    public Recipe getRecipe() {
        ShapelessRecipe recipe = new ShapelessRecipe(getKey(), toItemStack(1));
        InfernalDust dust1 = new InfernalDust();
        IronDust dust2 = new IronDust();
        registerCustomIngredients(dust1, dust2);
        recipe.addIngredient(dust1.getMaterial());
        recipe.addIngredient(dust2.getMaterial());
        return recipe;
    }

    @Override
    public boolean hasGlow() {
        return true;
    }

    @Override
    public ItemStack getSmeltingResult() {
        return new EnergizedIronIngot().toItemStack();
    }
}
