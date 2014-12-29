package me.mrCookieSlime.sensibletoolbox.items.itemroutermodules;

import me.mrCookieSlime.sensibletoolbox.api.items.BaseSTBItem;
import me.mrCookieSlime.sensibletoolbox.blocks.ItemRouter;

import org.bukkit.DyeColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.material.Dye;

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
