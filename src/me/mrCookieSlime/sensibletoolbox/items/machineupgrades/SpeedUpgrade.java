package me.mrCookieSlime.sensibletoolbox.items.machineupgrades;

import me.mrCookieSlime.sensibletoolbox.items.components.SimpleCircuit;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.material.MaterialData;

public class SpeedUpgrade extends MachineUpgrade {
    private static final MaterialData md = new MaterialData(Material.SUGAR);

    public SpeedUpgrade() {
    }

    public SpeedUpgrade(ConfigurationSection conf) {
        super(conf);
    }

    @Override
    public MaterialData getMaterialData() {
        return md;
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
        return new String[]{
        	"Place in a machine block",
        	"Speed: x1.4",
        	"Power Usage: x1.6"
        };
    }

    @Override
    public Recipe getRecipe() {
        ShapedRecipe recipe = new ShapedRecipe(toItemStack());
        SimpleCircuit sc = new SimpleCircuit();
        registerCustomIngredients(sc);
        recipe.shape("ISI", "IBI", "IGI");
        recipe.setIngredient('I', Material.IRON_FENCE);
        recipe.setIngredient('S', sc.getMaterialData());
        recipe.setIngredient('B', Material.BLAZE_ROD);
        recipe.setIngredient('G', Material.GOLD_INGOT);
        return recipe;
    }
}
