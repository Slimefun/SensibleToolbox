package io.github.thebusybiscuit.sensibletoolbox.items.itemroutermodules;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapelessRecipe;

import io.github.thebusybiscuit.sensibletoolbox.items.components.SubspaceTransponder;
import io.github.thebusybiscuit.sensibletoolbox.utils.UnicodeSymbol;

public class HyperSenderModule extends AdvancedSenderModule {

    public HyperSenderModule() {
        super();
    }

    public HyperSenderModule(ConfigurationSection conf) {
        super(conf);
    }

    @Override
    public Material getMaterial() {
        return Material.CYAN_DYE;
    }

    @Override
    public String getItemName() {
        return "I.R. Mod: Hypersender";
    }

    @Override
    public String[] getLore() {
        return new String[] { "Insert into an Item Router", "Sends items to a linked Receiver Module", "anywhere on any world", "L-Click item router with installed", " Receiver Module: " + ChatColor.WHITE + " Link Hyper Sender", UnicodeSymbol.ARROW_UP.toUnicode() + " + L-Click anywhere: " + ChatColor.WHITE + " Unlink Hyper Sender" };
    }

    @Override
    public Recipe getRecipe() {
        SenderModule sm = new SenderModule();
        SubspaceTransponder st = new SubspaceTransponder();
        registerCustomIngredients(sm, st);
        ShapelessRecipe recipe = new ShapelessRecipe(getKey(), toItemStack());
        recipe.addIngredient(sm.getMaterial());
        recipe.addIngredient(st.getMaterial());
        return recipe;
    }

    @Override
    protected boolean inRange(Location ourLoc) {
        return ourLoc != null;
    }
}
