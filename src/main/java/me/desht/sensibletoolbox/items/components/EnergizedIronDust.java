package me.desht.sensibletoolbox.items.components;

import me.desht.sensibletoolbox.api.items.BaseSTBItem;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.material.MaterialData;

public class EnergizedIronDust extends BaseSTBItem {
    public static final MaterialData md = new MaterialData(Material.SULPHUR);

    public EnergizedIronDust() {
    }

    public EnergizedIronDust(ConfigurationSection conf) {

    }

    @Override
    public MaterialData getMaterialData() {
        return md;
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
        ShapelessRecipe recipe = new ShapelessRecipe(toItemStack(1));
        InfernalDust dust1 = new InfernalDust();
        IronDust dust2 = new IronDust();
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
        return new EnergizedIronIngot().toItemStack();
    }
}
