package me.mrCookieSlime.sensibletoolbox.items.itemroutermodules;

import me.mrCookieSlime.sensibletoolbox.items.components.SubspaceTransponder;

import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapelessRecipe;
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
        SenderModule sm = new SenderModule();
        SubspaceTransponder st = new SubspaceTransponder();
        registerCustomIngredients(sm, st);
        ShapelessRecipe recipe = new ShapelessRecipe(toItemStack());
        recipe.addIngredient(sm.getMaterialData());
        recipe.addIngredient(st.getMaterialData());
        return recipe;
    }

    protected boolean inRange(Location ourLoc) {
        return ourLoc != null;
    }
}
