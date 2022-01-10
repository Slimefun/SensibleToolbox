package io.github.thebusybiscuit.sensibletoolbox.items.components;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.Recipe;

import io.github.thebusybiscuit.sensibletoolbox.api.items.BaseSTBItem;

public class EnergizedIronIngot extends BaseSTBItem {

    public EnergizedIronIngot() {}

    public EnergizedIronIngot(ConfigurationSection conf) {

    }

    @Override
    public Material getMaterial() {
        return Material.IRON_INGOT;
    }

    @Override
    public String getItemName() {
        return "Energized Iron Ingot";
    }

    @Override
    public String[] getLore() {
        return new String[] { "Strangely glowing..." };
    }

    @Override
    public Recipe getMainRecipe() {
        return null;
    }

    @Override
    public boolean hasGlow() {
        return true;
    }
}
