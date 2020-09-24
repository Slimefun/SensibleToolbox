package me.desht.sensibletoolbox.items;

import me.desht.sensibletoolbox.api.items.BaseSTBItem;
import me.desht.sensibletoolbox.api.util.STBUtil;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Recipe;
import org.bukkit.material.MaterialData;

public class BagOfHolding extends BaseSTBItem {
    private static final MaterialData md = new MaterialData(Material.ENDER_PORTAL_FRAME);

    public BagOfHolding() {
    }

    public BagOfHolding(ConfigurationSection conf) {

    }

    @Override
    public MaterialData getMaterialData() {
        return md;
    }

    @Override
    public String getItemName() {
        return "Bag Of Holding (obsolete)";
    }

    @Override
    public String[] getLore() {
        return new String[]{"Place in a crafting grid", "to get an Ender Bag"};
    }

    @Override
    public Recipe getRecipe() {
        return null;
    }

    @Override
    public void onInteractItem(PlayerInteractEvent event) {
        if (event.getAction() != Action.PHYSICAL) {
            STBUtil.complain(event.getPlayer(), "This item no longer works.  Craft it in a crafting grid to receive an Ender Bag.");
            event.setCancelled(true);
        }
    }
}
