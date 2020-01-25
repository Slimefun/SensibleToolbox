package io.github.thebusybiscuit.sensibletoolbox.items.machineupgrades;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;

import io.github.thebusybiscuit.sensibletoolbox.items.components.SimpleCircuit;

public class RegulatorUpgrade extends MachineUpgrade {

    public RegulatorUpgrade() {}

    public RegulatorUpgrade(ConfigurationSection conf) {
        super(conf);
    }

    @Override
    public Material getMaterial() {
        return Material.ENDER_EYE;
    }

    @Override
    public String getItemName() {
        return "Regulator Upgrade";
    }

    @Override
    public String[] getLore() {
        return new String[] {
            "Adds intelligence to machines",
            "for more efficient resource",
            "usage.  Effect varies by machine."
        };
    }

    @Override
    public Recipe getRecipe() {
        ShapedRecipe recipe = new ShapedRecipe(getKey(), toItemStack());
        SimpleCircuit sc = new SimpleCircuit();
        registerCustomIngredients(sc);
        recipe.shape("ISI", "IEI", "IRI");
        recipe.setIngredient('I', Material.IRON_BARS);
        recipe.setIngredient('S', sc.getMaterialData());
        recipe.setIngredient('E', Material.ENDER_EYE);
        recipe.setIngredient('R', Material.REDSTONE);
        return recipe;
    }

    @Override
    public boolean hasGlow() {
        return true;
    }
}
