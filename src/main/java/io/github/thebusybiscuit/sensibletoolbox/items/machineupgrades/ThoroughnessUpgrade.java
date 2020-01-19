package io.github.thebusybiscuit.sensibletoolbox.items.machineupgrades;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.material.MaterialData;

import io.github.thebusybiscuit.sensibletoolbox.items.components.IntegratedCircuit;

public class ThoroughnessUpgrade extends MachineUpgrade {
    private static final MaterialData md = new MaterialData(Material.SPIDER_EYE);

    public static final int BONUS_OUTPUT_CHANCE = 8; // percent

    public ThoroughnessUpgrade() {
    }

    public ThoroughnessUpgrade(ConfigurationSection conf) {
        super(conf);
    }

    @Override
    public MaterialData getMaterialData() {
        return md;
    }

    @Override
    public String getItemName() {
        return "Thoroughness Upgrade";
    }

    @Override
    public String[] getLore() {
        return new String[] {
                "Place in a machine block",
                "Speed: x0.7",
                "Power Usage: x1.6",
                "Bonus Output: +" + BONUS_OUTPUT_CHANCE + "%"
        };
    }

    @Override
    public Recipe getRecipe() {
        ShapedRecipe recipe = new ShapedRecipe(toItemStack());
        recipe.shape("ICI", "IEI", "IGI");
        IntegratedCircuit ic = new IntegratedCircuit();
        registerCustomIngredients(ic);
        recipe.setIngredient('I', Material.IRON_FENCE);
        recipe.setIngredient('C', ic.getMaterialData());
        recipe.setIngredient('E', Material.SPIDER_EYE);
        recipe.setIngredient('G', Material.THIN_GLASS);
        return recipe;
    }

    @Override
    public boolean hasGlow() {
        return true;
    }
}
