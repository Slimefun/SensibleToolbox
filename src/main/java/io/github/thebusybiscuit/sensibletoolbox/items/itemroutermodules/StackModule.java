package io.github.thebusybiscuit.sensibletoolbox.items.itemroutermodules;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapelessRecipe;

public class StackModule extends ItemRouterModule {

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
        ShapelessRecipe recipe = new ShapelessRecipe(getKey(), toItemStack());
        recipe.addIngredient(bm.getMaterial());
        recipe.addIngredient(Material.BRICK);
        return recipe;
    }

    @Override
    public Material getMaterial() {
        return Material.BRICK;
    }
}
