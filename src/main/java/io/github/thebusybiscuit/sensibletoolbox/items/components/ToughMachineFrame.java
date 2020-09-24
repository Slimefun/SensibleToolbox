package io.github.thebusybiscuit.sensibletoolbox.items.components;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;

import io.github.thebusybiscuit.sensibletoolbox.api.items.BaseSTBItem;

public class ToughMachineFrame extends BaseSTBItem {

    public ToughMachineFrame() {}

    public ToughMachineFrame(ConfigurationSection conf) {}

    @Override
    public Material getMaterial() {
        return Material.IRON_BLOCK;
    }

    @Override
    public String getItemName() {
        return "Tough Machine Frame";
    }

    @Override
    public String[] getLore() {
        return new String[] { "Used in fabrication of", "some more advanced machines." };
    }

    @Override
    public Recipe getRecipe() {
        ShapedRecipe recipe = new ShapedRecipe(getKey(), toItemStack());
        EnergizedIronIngot ingot = new EnergizedIronIngot();
        MachineFrame frame = new MachineFrame();
        registerCustomIngredients(ingot, frame);
        recipe.shape(" I ", "IFI", " I ");
        recipe.setIngredient('F', frame.getMaterial());
        recipe.setIngredient('I', ingot.getMaterial());
        return recipe;
    }
}
