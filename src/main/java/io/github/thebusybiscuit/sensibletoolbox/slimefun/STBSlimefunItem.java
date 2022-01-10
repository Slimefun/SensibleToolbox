package io.github.thebusybiscuit.sensibletoolbox.slimefun;

import javax.annotation.ParametersAreNonnullByDefault;

import org.bukkit.inventory.ItemStack;

import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.attributes.NotConfigurable;
import io.github.thebusybiscuit.slimefun4.core.attributes.NotPlaceable;

public class STBSlimefunItem extends SlimefunItem implements NotPlaceable, NotConfigurable {

    @ParametersAreNonnullByDefault
    public STBSlimefunItem(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);

        setUseableInWorkbench(true);
    }

}
