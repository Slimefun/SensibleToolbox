package io.github.thebusybiscuit.sensibletoolbox.items;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;

public class PaintRoller extends PaintBrush {

    public PaintRoller() {
        super();
    }

    public PaintRoller(ConfigurationSection conf) {
        super(conf);
    }

    @Override
    public Material getMaterial() {
        return Material.IRON_SHOVEL;
    }

    @Override
    public String getItemName() {
        return "Paint Roller";
    }

    @Override
    public int getMaxPaintLevel() {
        return 100;
    }

    @Override
    public boolean hasGlow() {
        return true;
    }

    @Override
    public Recipe getRecipe() {
        ShapedRecipe recipe = new ShapedRecipe(getKey(), toItemStack());
        recipe.shape("WWW", "III", " S ");
        recipe.setIngredient('W', Material.WHITE_WOOL);
        recipe.setIngredient('I', Material.IRON_INGOT);
        recipe.setIngredient('S', Material.STICK);
        return recipe;
    }

    @Override
    protected int getMaxBlocksAffected() {
        return 25;
    }
}
