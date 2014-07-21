package me.desht.sensibletoolbox.items;

import me.desht.dhutils.MiscUtil;
import me.desht.sensibletoolbox.api.items.BaseSTBItem;
import me.desht.sensibletoolbox.items.components.SimpleCircuit;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.material.MaterialData;

import java.util.UUID;

public class LandMarker extends BaseSTBItem {
    private static final MaterialData md = new MaterialData(Material.FIREWORK);
    private Location loc;

    public LandMarker() {
        loc = null;
    }

    public LandMarker(ConfigurationSection conf) {
        if (conf.contains("worldId")) {
            UUID worldId = UUID.fromString(conf.getString("worldId"));
            World w = Bukkit.getWorld(worldId);
            if (w != null) {
                loc = new Location(w, conf.getInt("x"), conf.getInt("y"), conf.getInt("z"));
            } else {
                loc = null;
            }
        } else {
            loc = null;
        }
    }

    @Override
    public YamlConfiguration freeze() {
        YamlConfiguration conf = super.freeze();

        if (loc != null) {
            conf.set("worldId", loc.getWorld().getUID().toString());
            conf.set("x", loc.getBlockX());
            conf.set("y", loc.getBlockY());
            conf.set("z", loc.getBlockZ());
        }

        return conf;
    }

    @Override
    public MaterialData getMaterialData() {
        return md;
    }

    @Override
    public String getItemName() {
        return "Land Marker";
    }

    @Override
    public String[] getLore() {
        return new String[] { "Stores positions via Sensible GPS","R-Click block: store position","R-Click air: clear position"};
    }

    @Override
    public Recipe getRecipe() {
        ShapedRecipe recipe = new ShapedRecipe(toItemStack());
        recipe.shape(" T ", " C ", " S ");
        SimpleCircuit sc = new SimpleCircuit();
        registerCustomIngredients(sc);
        recipe.setIngredient('T', Material.REDSTONE_TORCH_ON);
        recipe.setIngredient('C', sc.getMaterialData());
        recipe.setIngredient('S', Material.STICK);
        return recipe;
    }

    @Override
    public String getDisplaySuffix() {
        return loc == null ? null : MiscUtil.formatLocation(loc);
    }

    public Location getLocation() {
        return loc;
    }

    public void setLocation(Location loc) {
        this.loc = loc;
    }

    @Override
    public void onInteractItem(PlayerInteractEvent event) {
        if ((event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_AIR) && getLocation() != null) {
            setLocation(null);
            event.getPlayer().setItemInHand(toItemStack());
        } else if ((event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.LEFT_CLICK_BLOCK) && !event.getClickedBlock().getLocation().equals(loc)) {
            setLocation(event.getClickedBlock().getLocation());
            event.getPlayer().setItemInHand(toItemStack());
        }
        event.setCancelled(true);
    }
}
