package io.github.thebusybiscuit.sensibletoolbox.items.itemroutermodules;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.material.MaterialData;

public class StackModule extends ItemRouterModule {
    private static final MaterialData md = new MaterialData(Material.CLAY_BRICK);

    public StackModule() {
    }

    public StackModule(ConfigurationSection conf) {
        super(conf);
    }

    @Override
    public String getItemName() {
        return "I.R. Mod: Stack Upgrade";
    }

    @Override
    public String[] getLore() {
        return new String[]{
                "Insert into an Item Router",
                "Passive module; each stack module",
                "doubles the number of items moved",
                "per operation, up to the item's max",
                "stack size.",
                "Any modules over 6 are ignored.",
        };
    }

    @Override
    public Recipe getRecipe() {
        BlankModule bm = new BlankModule();
        registerCustomIngredients(bm);
        ShapelessRecipe recipe = new ShapelessRecipe(toItemStack());
        recipe.addIngredient(bm.getMaterialData());
        recipe.addIngredient(Material.BRICK);
        return recipe;
    }

    @Override
    public MaterialData getMaterialData() {
        return md;
    }
}
