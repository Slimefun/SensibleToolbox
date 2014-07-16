package me.desht.sensibletoolbox.items.components;

import me.desht.sensibletoolbox.api.items.BaseSTBItem;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.Recipe;
import org.bukkit.material.MaterialData;

public class EnergizedGoldIngot extends BaseSTBItem {
    public static final MaterialData md = new MaterialData(Material.GOLD_INGOT);

    public EnergizedGoldIngot() {
    }

    public EnergizedGoldIngot(ConfigurationSection conf) {

    }

    @Override
    public MaterialData getMaterialData() {
        return md;
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
