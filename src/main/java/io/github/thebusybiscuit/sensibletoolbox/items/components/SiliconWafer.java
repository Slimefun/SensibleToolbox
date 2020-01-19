package io.github.thebusybiscuit.sensibletoolbox.items.components;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.Recipe;
import org.bukkit.material.MaterialData;

import io.github.thebusybiscuit.sensibletoolbox.api.items.BaseSTBItem;

public class SiliconWafer extends BaseSTBItem {
    private static final MaterialData md = new MaterialData(Material.FIREWORK_CHARGE);

    public SiliconWafer() {
    }

    public SiliconWafer(ConfigurationSection conf) {
    }

    @Override
    public MaterialData getMaterialData() {
        return md;
    }

    @Override
    public String getItemName() {
        return "Silicon Wafer";
    }

    @Override
    public String[] getLore() {
        return new String[] { "Used in the fabrication", "of more advanced", "electronic circuits"};
    }

    @Override
    public Recipe getRecipe() {
        // made in a smelter
        return null;
    }
}
