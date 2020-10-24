package io.github.thebusybiscuit.sensibletoolbox.items.upgrades;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;

import io.github.thebusybiscuit.sensibletoolbox.items.components.SimpleCircuit;

public class SpeedUpgrade extends MachineUpgrade {

    public SpeedUpgrade() {}

    public SpeedUpgrade(ConfigurationSection conf) {
        super(conf);
    }

    @Override
    public Material getMaterial() {
        return Material.SUGAR;
    }

    @Override
    public boolean hasGlow() {
        return true;
    }

    @Override
    public String getItemName() {
        return "Speed Upgrade";
    }

    @Override
    public String[] getLore() {
        return new String[] { "Place in a machine block", "Speed: x1.4", "Power Usage: x1.6" };
    }

    @Override
    public Recipe getRecipe() {
        ShapedRecipe recipe = new ShapedRecipe(getKey(), toItemStack());
        SimpleCircuit sc = new SimpleCircuit();
        registerCustomIngredients(sc);
        recipe.shape("ISI", "IBI", "IGI");
        recipe.setIngredient('I', Material.IRON_BARS);
        recipe.setIngredient('S', sc.getMaterial());
        recipe.setIngredient('B', Material.BLAZE_ROD);
        recipe.setIngredient('G', Material.GOLD_INGOT);
        return recipe;
    }
}
