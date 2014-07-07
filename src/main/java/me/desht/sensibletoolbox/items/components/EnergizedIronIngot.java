package me.desht.sensibletoolbox.items.components;

import me.desht.sensibletoolbox.items.BaseSTBItem;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.Recipe;
import org.bukkit.material.MaterialData;

public class EnergizedIronIngot extends BaseSTBItem {
    public static final MaterialData md = new MaterialData(Material.IRON_INGOT);

    public EnergizedIronIngot() {
    }

    public EnergizedIronIngot(ConfigurationSection conf) {

    }

    @Override
    public MaterialData getMaterialData() {
        return md;
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
    public Recipe getRecipe() {
        return null;
    }

    @Override
    public boolean hasGlow() {
        return true;
    }
}
