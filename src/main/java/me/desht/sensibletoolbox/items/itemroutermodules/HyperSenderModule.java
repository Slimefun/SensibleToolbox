package me.desht.sensibletoolbox.items.itemroutermodules;

import me.desht.sensibletoolbox.items.components.EnergizedGoldIngot;
import me.desht.sensibletoolbox.items.components.IntegratedCircuit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.material.Dye;
import org.bukkit.material.MaterialData;

public class HyperSenderModule extends AdvancedSenderModule {
    private static final Dye md = makeDye(DyeColor.CYAN);

    public HyperSenderModule() {
        super();
    }

    public HyperSenderModule(ConfigurationSection conf) {
        super(conf);
    }

    @Override
    public MaterialData getMaterialData() {
        return md;
    }

    @Override
    public String getItemName() {
        return "I.R. Mod: Hypersender";
    }

    @Override
    public String[] getLore() {
        return new String[]{
                "Insert into an Item Router",
                "Sends items to a linked Receiver Module",
                "anywhere on any world",
                "L-Click item router with installed",
                " Receiver Module: " + ChatColor.RESET + " Link Hyper Sender",
                "⇧ + L-Click anywhere: " + ChatColor.RESET + " Unlink Hyper Sender"
        };
    }

    @Override
    public Recipe getRecipe() {
        AdvancedSenderModule sm = new AdvancedSenderModule();
        IntegratedCircuit sc = new IntegratedCircuit();
        EnergizedGoldIngot eg = new EnergizedGoldIngot();
        registerCustomIngredients(sm, sc, eg);
        ShapedRecipe recipe = new ShapedRecipe(toItemStack());
        recipe.shape(" C ", "GSG");
        recipe.setIngredient('S', sm.getMaterialData());
        recipe.setIngredient('C', sc.getMaterialData());
        recipe.setIngredient('G', eg.getMaterialData());
        return recipe;
    }

    protected boolean inRange(Location ourLoc) {
        return ourLoc != null;
    }
}
