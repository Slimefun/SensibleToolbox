package me.mrCookieSlime.sensibletoolbox.items.machineupgrades;

import me.mrCookieSlime.sensibletoolbox.items.components.SimpleCircuit;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.material.MaterialData;

public class RegulatorUpgrade extends MachineUpgrade {
    private static final MaterialData md = new MaterialData(Material.EYE_OF_ENDER);

    public RegulatorUpgrade() {
    }

    public RegulatorUpgrade(ConfigurationSection conf) {
        super(conf);
    }

    @Override
    public MaterialData getMaterialData() {
        return md;
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
        ShapedRecipe recipe = new ShapedRecipe(toItemStack());
        SimpleCircuit sc = new SimpleCircuit();
        registerCustomIngredients(sc);
        recipe.shape("ISI", "IEI", "IRI");
        recipe.setIngredient('I', Material.IRON_FENCE);
        recipe.setIngredient('S', sc.getMaterialData());
        recipe.setIngredient('E', Material.EYE_OF_ENDER);
        recipe.setIngredient('R', Material.REDSTONE);
        return recipe;
    }

    @Override
    public boolean hasGlow() {
        return true;
    }
}
