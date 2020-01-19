package io.github.thebusybiscuit.sensibletoolbox.items.itemroutermodules;

import org.bukkit.DyeColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.material.Dye;

import io.github.thebusybiscuit.sensibletoolbox.api.items.BaseSTBItem;
import io.github.thebusybiscuit.sensibletoolbox.blocks.ItemRouter;

public abstract class ItemRouterModule extends BaseSTBItem {
    private ItemRouter itemRouter;

    protected static Dye makeDye(DyeColor color) {
        Dye dye = new Dye();
        dye.setColor(color);
        return dye;
    }

    protected ItemRouterModule() {
    }

    public ItemRouterModule(ConfigurationSection conf) {
        super(conf);
    }

    @Override
    public boolean hasGlow() {
        return true;
    }

    public ItemRouter getItemRouter() {
        return itemRouter;
    }

    public void setItemRouter(ItemRouter owner) {
        this.itemRouter = owner;
    }

}
