package me.mrCookieSlime.sensibletoolbox.items.components;

import me.mrCookieSlime.sensibletoolbox.api.items.BaseSTBItem;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.material.MaterialData;

public class MachineFrame extends BaseSTBItem {
    private static final MaterialData md = new MaterialData(Material.IRON_BLOCK);

    public MachineFrame() {
    }

    public MachineFrame(ConfigurationSection conf) {
    }

    @Override
    public MaterialData getMaterialData() {
        return md;
    }

    @Override
    public String getItemName() {
        return "Machine Frame";
    }

    @Override
    public String[] getLore() {
        return new String[]{"Used in fabrication of", "various machines."};
    }

    @Override
    public Recipe getRecipe() {
        ShapedRecipe recipe = new ShapedRecipe(toItemStack());
        recipe.shape("IBI", "B B", "IBI");
        recipe.setIngredient('B', Material.IRON_FENCE);
        recipe.setIngredient('I', Material.IRON_INGOT);
        return recipe;
    }

}
