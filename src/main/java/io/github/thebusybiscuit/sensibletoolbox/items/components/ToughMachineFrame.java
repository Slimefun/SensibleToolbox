package io.github.thebusybiscuit.sensibletoolbox.items.components;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.material.MaterialData;

import io.github.thebusybiscuit.sensibletoolbox.api.items.BaseSTBItem;

public class ToughMachineFrame extends BaseSTBItem {
    private static final MaterialData md = new MaterialData(Material.IRON_BLOCK);

    public ToughMachineFrame() {
    }

    public ToughMachineFrame(ConfigurationSection conf) {
    }

    @Override
    public MaterialData getMaterialData() {
        return md;
    }

    @Override
    public String getItemName() {
        return "Tough Machine Frame";
    }

    @Override
    public String[] getLore() {
        return new String[]{"Used in fabrication of", "some more advanced machines."};
    }

    @Override
    public Recipe getRecipe() {
        ShapedRecipe recipe = new ShapedRecipe(toItemStack());
        EnergizedIronIngot ingot = new EnergizedIronIngot();
        MachineFrame frame = new MachineFrame();
        registerCustomIngredients(ingot, frame);
        recipe.shape(" I ", "IFI", " I ");
        recipe.setIngredient('F', frame.getMaterialData());
        recipe.setIngredient('I', ingot.getMaterialData());
        return recipe;
    }
}
