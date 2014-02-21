package me.desht.sensibletoolbox.items.machineupgrades;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.material.MaterialData;

public class EjectorUpgrade extends MachineUpgrade {
	private static final MaterialData md = new MaterialData(Material.QUARTZ);

	private BlockFace direction;

	public EjectorUpgrade() {
		direction = BlockFace.SELF;
	}

	public EjectorUpgrade(ConfigurationSection conf) {
		super(conf);
		direction = BlockFace.valueOf(conf.getString("direction"));
	}

	public YamlConfiguration freeze() {
		YamlConfiguration conf = super.freeze();
		conf.set("direction", getDirection().toString());
		return conf;
	}

	public BlockFace getDirection() {
		return direction;
	}

	public void setDirection(BlockFace direction) {
		this.direction = direction;
	}

	@Override
	public boolean hasGlow() {
		return true;
	}

	@Override
	public MaterialData getMaterialData() {
		return md;
	}

	@Override
	public String getItemName() {
		return "Ejector Upgrade";
	}

	@Override
	public String getDisplaySuffix() {
		return direction != null && direction != BlockFace.SELF ? direction.toString() : null;
	}

	@Override
	public String[] getLore() {
		return new String[] { "Place in a machine block ", "Auto-ejects finished items", "L-Click block: set ejection direction" };
	}

	@Override
	public Recipe getRecipe() {
		ShapedRecipe recipe = new ShapedRecipe(toItemStack());
		recipe.shape("IRI", "IBI", "IGI");
		recipe.setIngredient('I', Material.IRON_FENCE);
		recipe.setIngredient('R', Material.REDSTONE);
		recipe.setIngredient('B', Material.PISTON_BASE);
		recipe.setIngredient('G', Material.GOLD_INGOT);
		return recipe;
	}

	@Override
	public void onInteractItem(PlayerInteractEvent event) {
		if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
			setDirection(event.getBlockFace().getOppositeFace());
			event.getPlayer().setItemInHand(toItemStack(event.getPlayer().getItemInHand().getAmount()));
			event.setCancelled(true);
		}
	}
}
