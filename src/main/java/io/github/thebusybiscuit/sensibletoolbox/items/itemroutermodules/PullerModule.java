package io.github.thebusybiscuit.sensibletoolbox.items.itemroutermodules;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapelessRecipe;

public class PullerModule extends DirectionalItemRouterModule {

    public PullerModule() {}

    public PullerModule(ConfigurationSection conf) {
        super(conf);
    }

    @Override
    public String getItemName() {
        return "I.R. Mod: Puller";
    }

    @Override
    public String[] getLore() {
        return makeDirectionalLore("Insert into an Item Router", "Pulls items from an adjacent inventory");
    }

    @Override
    public Recipe getRecipe() {
        BlankModule bm = new BlankModule();
        registerCustomIngredients(bm);
        ShapelessRecipe recipe = new ShapelessRecipe(getKey(), toItemStack());
        recipe.addIngredient(bm.getMaterial());
        recipe.addIngredient(Material.STICKY_PISTON);
        return recipe;
    }

    @Override
    public Material getMaterial() {
        return Material.LIME_DYE;
    }

    @Override
    public boolean execute(Location loc) {
        return getItemRouter() != null && doPull(getFacing(), loc);
    }
}
