package io.github.thebusybiscuit.sensibletoolbox.items.components;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.Recipe;

import io.github.thebusybiscuit.sensibletoolbox.api.items.BaseSTBItem;

public class EnergizedGoldIngot extends BaseSTBItem {

    public EnergizedGoldIngot() {}

    public EnergizedGoldIngot(ConfigurationSection conf) {

    }

    @Override
    public Material getMaterial() {
        return Material.GOLD_INGOT;
    }

    @Override
    public String getItemName() {
        return "Energized Gold Ingot";
    }

    @Override
    public String[] getLore() {
        return new String[] { "Strangely glowing..." };
    }

    @Override
    public Recipe getRecipe() {
        return null;
    }

    @Override
    public boolean hasGlow() {
        return true;
    }
}
