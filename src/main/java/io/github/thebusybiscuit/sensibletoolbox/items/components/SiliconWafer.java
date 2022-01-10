package io.github.thebusybiscuit.sensibletoolbox.items.components;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.Recipe;

import io.github.thebusybiscuit.sensibletoolbox.api.items.BaseSTBItem;

public class SiliconWafer extends BaseSTBItem {

    public SiliconWafer() {}

    public SiliconWafer(ConfigurationSection conf) {}

    @Override
    public Material getMaterial() {
        return Material.FIREWORK_STAR;
    }

    @Override
    public String getItemName() {
        return "Silicon Wafer";
    }

    @Override
    public String[] getLore() {
        return new String[] { "Used in the fabrication", "of more advanced", "electronic circuits" };
    }

    @Override
    public Recipe getMainRecipe() {
        // made in a smelter
        return null;
    }
}
