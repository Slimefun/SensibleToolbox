package io.github.thebusybiscuit.sensibletoolbox.items.itemroutermodules;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapelessRecipe;

public class SpeedModule extends ItemRouterModule {

    public SpeedModule() {}

    public SpeedModule(ConfigurationSection conf) {
        super(conf);
    }

    @Override
    public String getItemName() {
        return "I.R. Mod: Speed Upgrade";
    }

    @Override
    public String[] getLore() {
        return new String[] { "Insert into an Item Router", "Passive module; increases router speed:", "0 modules = 1 operation / 20 ticks", "1 = 1/15, 2 = 1/10, 3 = 1/5", "Any modules over 3 are ignored." };
    }

    @Override
    public Recipe getRecipe() {
        BlankModule bm = new BlankModule();
        registerCustomIngredients(bm);
        ShapelessRecipe recipe = new ShapelessRecipe(getKey(), toItemStack());
        recipe.addIngredient(bm.getMaterial());
        recipe.addIngredient(Material.BLAZE_POWDER);
        recipe.addIngredient(Material.EMERALD);
        return recipe;
    }

    @Override
    public Material getMaterial() {
        return Material.BLAZE_POWDER;
    }
}
