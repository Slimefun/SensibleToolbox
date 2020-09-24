package me.desht.sensibletoolbox.items.components;

import me.desht.sensibletoolbox.api.items.BaseSTBItem;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.Recipe;
import org.bukkit.material.MaterialData;

public class InfernalDust extends BaseSTBItem {
    private static final MaterialData md = new MaterialData(Material.BLAZE_POWDER);

    public InfernalDust() {
    }

    public InfernalDust(ConfigurationSection conf) {
    }

    @Override
    public MaterialData getMaterialData() {
        return md;
    }

    @Override
    public String getItemName() {
        return "Infernal Dust";
    }

    @Override
    public String[] getLore() {
        return new String[] { "Sometimes dropped from blazes.", "Looting enchantment may help.", "Combine this with iron or gold dust."};
    }

    @Override
    public Recipe getRecipe() {
        // no vanilla recipe to make infernal dust, but a custom recipe will be added
        return null;
    }

    @Override
    public boolean hasGlow() {
        return true;
    }
}
