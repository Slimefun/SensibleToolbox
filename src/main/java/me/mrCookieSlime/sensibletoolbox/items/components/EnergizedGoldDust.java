package me.mrCookieSlime.sensibletoolbox.items.components;

import me.mrCookieSlime.sensibletoolbox.api.items.BaseSTBItem;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.material.MaterialData;

public class EnergizedGoldDust extends BaseSTBItem {
    public static final MaterialData md = new MaterialData(Material.GLOWSTONE_DUST);

    public EnergizedGoldDust() {
    }

    public EnergizedGoldDust(ConfigurationSection conf) {
    }

    @Override
    public MaterialData getMaterialData() {
        return md;
    }

    @Override
    public String getItemName() {
        return "Energized Gold Dust";
    }

    @Override
    public String[] getLore() {
        return new String[] { "Smelt to get an energized gold ingot" };
    }

    @Override
    public Recipe getRecipe() {
        ShapelessRecipe recipe = new ShapelessRecipe(toItemStack(1));
        InfernalDust dust1 = new InfernalDust();
        GoldDust dust2 = new GoldDust();
        registerCustomIngredients(dust1, dust2);
        recipe.addIngredient(dust1.getMaterialData());
        recipe.addIngredient(dust2.getMaterialData());
        return recipe;
    }

    @Override
    public boolean hasGlow() {
        return true;
    }

    @Override
    public ItemStack getSmeltingResult() {
        return new EnergizedGoldIngot().toItemStack();
    }
}
