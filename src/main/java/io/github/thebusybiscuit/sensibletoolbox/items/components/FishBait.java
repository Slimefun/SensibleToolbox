package io.github.thebusybiscuit.sensibletoolbox.items.components;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.Recipe;

import io.github.thebusybiscuit.sensibletoolbox.api.items.BaseSTBItem;

public class FishBait extends BaseSTBItem {

    public FishBait() {}

    public FishBait(ConfigurationSection conf) {}

    @Override
    public Material getMaterial() {
        return Material.ROTTEN_FLESH;
    }

    @Override
    public String getItemName() {
        return "Fish Bait";
    }

    @Override
    public String[] getLore() {
        return new String[] { "Used in a Fishing Net", "to catch Fish" };
    }

    @Override
    public Recipe getMainRecipe() {
        return null;
    }
}
