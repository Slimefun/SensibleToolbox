package io.github.thebusybiscuit.sensibletoolbox.items.components;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.Recipe;

import io.github.thebusybiscuit.sensibletoolbox.api.items.BaseSTBItem;

public class InfernalDust extends BaseSTBItem {
	
    public InfernalDust() {
    }

    public InfernalDust(ConfigurationSection conf) {
    }

    @Override
    public Material getMaterial() {
        return Material.BLAZE_POWDER;
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
