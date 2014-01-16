package me.desht.sensibletoolbox.items;

import me.desht.dhutils.MiscUtil;
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

public class TapeMeasure extends BaseSTBItem {
	private String world;
	private int x, y, z;

	public TapeMeasure() {
		world = null;
		x = y = z = 0;
	}

	public TapeMeasure(ConfigurationSection conf) {
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
	public Material getBaseMaterial() {
		return Material.STRING;
	}

	@Override
	public String getItemName() {
		return "Tape Measure";
	}

	@Override
	public String[] getLore() {
		return new String[] { "⇧ + R-click block: set anchor", "R-click block: get measurement" };
	}

	@Override
	public String[] getExtraLore() {
		if (world != null) {
			return new String[] { ChatColor.WHITE + "Anchor point: " + ChatColor.GOLD + world + "," + x + "," + y + "," + z };
		} else {
			return new String[0];
		}
	}

	@Override
	public Recipe getRecipe() {
		ShapedRecipe recipe = new ShapedRecipe(toItemStack(1));
		recipe.shape("SSS", "SIS", "SSS");
		recipe.setIngredient('S', Material.STRING);
		recipe.setIngredient('I', Material.IRON_INGOT);
		return recipe;
	}

	@Override
	public void handleItemInteraction(PlayerInteractEvent event) {
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			if (event.getPlayer().isSneaking()) {
				setAnchor(event.getClickedBlock());
				event.getPlayer().setItemInHand(toItemStack(1));
				MiscUtil.statusMessage(event.getPlayer(), "Tape measure anchor point set.");
				event.setCancelled(true);
			} else {
				System.out.println("make measurement");
				makeMeasurement(event.getPlayer(), event.getClickedBlock());
				event.getPlayer().updateInventory();
				event.setCancelled(true);
			}
		} else if (event.getAction() == Action.RIGHT_CLICK_AIR) {
			setAnchor(null);
			event.getPlayer().setItemInHand(toItemStack(1));
			MiscUtil.statusMessage(event.getPlayer(), "Tape measure anchor point cleared.");
			event.setCancelled(true);
		}
	}

	private void makeMeasurement(Player p, Block b) {
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
		} else {
			world = null;
		}
	}
}
