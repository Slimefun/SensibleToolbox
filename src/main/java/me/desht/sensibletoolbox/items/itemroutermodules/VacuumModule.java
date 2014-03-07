package me.desht.sensibletoolbox.items.itemroutermodules;

import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.material.Dye;
import org.bukkit.material.MaterialData;
import org.bukkit.util.Vector;

public class VacuumModule extends DirectionalItemRouterModule {
	private static final Dye md = makeDye(DyeColor.BLACK);
	private static final int RADIUS = 6;

	public VacuumModule() {}

	public VacuumModule(ConfigurationSection conf) {
		super(conf);
	}

	@Override
	public MaterialData getMaterialData() {
		return md;
	}

	@Override
	public String getItemName() {
		return "I.R. Mod: Vacuum";
	}

	@Override
	public String[] getLore() {
		return new String[] { "Insert into an Item Router", "Sucks up items within a " + RADIUS + "-block radius "};
	}

	@Override
	public Recipe getRecipe() {
		registerCustomIngredients(new BlankModule());
		ShapelessRecipe recipe = new ShapelessRecipe(toItemStack());
		recipe.addIngredient(Material.PAPER);
		recipe.addIngredient(Material.HOPPER);
		recipe.addIngredient(Material.EYE_OF_ENDER);
		return recipe;
	}

	@Override
	public boolean execute(Location loc) {
		int dist = RADIUS * RADIUS;
		loc.add(0.5, 0.5, 0.5);
		ItemStack buffer = getItemRouter().getBufferItem();
		boolean acted = false;
		for (Item item : getItemRouter().getLocation().getWorld().getEntitiesByClass(Item.class)) {
			double d = loc.distanceSquared(item.getLocation());
			ItemStack onGround = item.getItemStack();
			if (!getFilter().shouldPass(onGround) || !rightDirection(item, loc)) {
				continue;
			}
			if (d < 2.0) {
				// slurp
				if (buffer == null) {
					getItemRouter().setBufferItem(onGround);
					buffer = getItemRouter().getBufferItem();
					item.remove();
				} else if (buffer.isSimilar(onGround)) {
					int toSlurp = Math.min(onGround.getAmount(), buffer.getType().getMaxStackSize() - buffer.getAmount());
					getItemRouter().setBufferAmount(buffer.getAmount() + toSlurp);
					buffer = getItemRouter().getBufferItem();
					onGround.setAmount(onGround.getAmount() - toSlurp);
					if (onGround.getAmount() == 0) {
						item.remove();
					} else {
						item.setItemStack(onGround);
					}
				}
				acted = true;
			} else if (d < dist) {
				Vector vel = loc.subtract(item.getLocation()).toVector().normalize().multiply(Math.min(d * 0.06, 1.0));
				item.setVelocity(vel);
			}
		}
		return acted;
	}

	private boolean rightDirection(Item item, Location loc) {
		if (getDirection() == null) {
			return true;
		}
		Location itemLoc = item.getLocation();
		switch (getDirection()) {
			case NORTH: return itemLoc.getZ() < loc.getZ();
			case EAST: return itemLoc.getX() > loc.getX();
			case SOUTH: return itemLoc.getZ() > loc.getZ();
			case WEST: return itemLoc.getX() > loc.getX();
			case UP: return itemLoc.getY() > loc.getY();
			case DOWN: return itemLoc.getY() < loc.getY();
			default: return true;
		}
	}
}
