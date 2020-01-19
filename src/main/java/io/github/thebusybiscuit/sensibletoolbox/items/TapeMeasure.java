package io.github.thebusybiscuit.sensibletoolbox.items;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;

import io.github.thebusybiscuit.sensibletoolbox.api.items.BaseSTBItem;
import io.github.thebusybiscuit.sensibletoolbox.util.UnicodeSymbol;
import me.desht.dhutils.Debugger;
import me.desht.dhutils.MiscUtil;

public class TapeMeasure extends BaseSTBItem {
	
    private String world;
    private int x, y, z;

    public TapeMeasure() {
        super();
        world = null;
        x = y = z = 0;
    }

    public TapeMeasure(ConfigurationSection conf) {
        super(conf);
        world = conf.getString("world");
        x = conf.getInt("x");
        y = conf.getInt("y");
        z = conf.getInt("z");
    }

    public YamlConfiguration freeze() {
        YamlConfiguration res = super.freeze();
        res.set("world", world);
        res.set("x", x);
        res.set("y", y);
        res.set("z", z);
        return res;
    }

    @Override
    public Material getMaterial() {
        return Material.STRING;
    }

    @Override
    public String getItemName() {
        return "Tape Measure";
    }

    @Override
    public String[] getLore() {
        return new String[] {UnicodeSymbol.ARROW_UP.toUnicode() + " + R-click block: set anchor", "R-click block: get measurement"};
    }

    @Override
    public String[] getExtraLore() {
        if (world != null) {
            return new String[]{ChatColor.WHITE + "Anchor point: " + ChatColor.GOLD + world + "," + x + "," + y + "," + z};
        } 
        else {
            return new String[0];
        }
    }

    @Override
    public Recipe getRecipe() {
        ShapedRecipe recipe = new ShapedRecipe(getKey(), toItemStack());
        recipe.shape("SSS", "SIS", "SSS");
        recipe.setIngredient('S', Material.STRING);
        recipe.setIngredient('I', Material.IRON_INGOT);
        return recipe;
    }
    
	@Override
    public void onInteractItem(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (event.getPlayer().isSneaking()) {
                setAnchor(event.getClickedBlock());
                event.getPlayer().setItemInHand(toItemStack());
                MiscUtil.statusMessage(event.getPlayer(), "Tape measure anchor point set.");
            } 
            else {
                makeMeasurement(event.getPlayer(), event.getClickedBlock());
            }
            event.getPlayer().updateInventory();
        } 
        else if (event.getAction() == Action.RIGHT_CLICK_AIR) {
            setAnchor(null);
            event.getPlayer().setItemInHand(toItemStack());
            MiscUtil.statusMessage(event.getPlayer(), "Tape measure anchor point cleared.");
        }
        event.setCancelled(true);
    }

    private void makeMeasurement(Player p, Block b) {
        Debugger.getInstance().debug(this + ": make measurement at " + b);
        if (world != null && world.equals(b.getWorld().getName())) {
            int xOff = b.getX() - x;
            int yOff = b.getY() - y;
            int zOff = b.getZ() - z;
            Location anchorLoc = new Location(b.getWorld(), x, y, z);
            double dist = b.getLocation().distance(anchorLoc);
            MiscUtil.statusMessage(p,
                    String.format("Measurement: " + ChatColor.WHITE + "X=%d Y=%d Z=%d total=%.2f",
                            xOff, yOff, zOff, dist));
        }
    }

    private void setAnchor(Block clickedBlock) {
        if (clickedBlock != null) {
            world = clickedBlock.getWorld().getName();
            x = clickedBlock.getX();
            y = clickedBlock.getY();
            z = clickedBlock.getZ();
        } 
        else {
            world = null;
        }
    }
}
