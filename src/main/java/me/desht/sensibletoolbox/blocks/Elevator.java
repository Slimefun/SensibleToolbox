package me.desht.sensibletoolbox.blocks;

import me.desht.sensibletoolbox.SensibleToolboxPlugin;
import me.desht.sensibletoolbox.items.BaseSTBItem;
import me.desht.sensibletoolbox.items.PaintBrush;
import org.apache.commons.lang.Validate;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.material.Dye;

import java.util.Map;

public class Elevator extends BaseSTBBlock {
	private DyeColor color;

	public Elevator() {
		color = DyeColor.WHITE;
	}

	public Elevator(Configuration conf) {
		color = DyeColor.valueOf(conf.getString("color"));
	}

	public Map<String, Object> serialize() {
		Map<String, Object> map = super.serialize();
		map.put("color", color.toString());
		return map;
	}

	public static Elevator deserialize(Map<String, Object> map) {
		return new Elevator(getConfigFromMap(map));
	}

	public DyeColor getColor() {
		return color;
	}

	public void setColor(DyeColor color) {
		this.color = color;
	}

	@Override
	public Material getBaseMaterial() {
		return Material.STAINED_CLAY;
	}

	@Override
	public Byte getBaseBlockData() {
		return color.getWoolData();
	}

	@Override
	public String getItemName() {
		return "Elevator";
	}

	@Override
	public String[] getLore() {
		return new String[] {
				"Links to other elevators", " directly above or below",
				"Press Space to go up", "Press Shift to go down"
		};
	}

	@Override
	public Recipe getRecipe() {
		ShapedRecipe recipe = new ShapedRecipe(toItemStack(1));
		recipe.shape("WWW", "WPW", "WWW");
		recipe.setIngredient('W', Material.WOOL);
		recipe.setIngredient('P', Material.ENDER_PEARL);
		return recipe;
	}

	public Elevator findOtherElevator(BlockFace direction) {
		Validate.isTrue(direction == BlockFace.UP || direction == BlockFace.DOWN, "direction must be UP or DOWN");

		Block b = getBaseLocation().getBlock();
		Elevator res = null;
		while (b.getY() > 0 && b.getY() < b.getWorld().getMaxHeight()) {
			b = b.getRelative(direction);
			if (b.getType().isSolid()) {
				res = SensibleToolboxPlugin.getInstance().getLocationManager().get(b.getLocation(), Elevator.class);
				break;
			}
		}
		return (res != null && res.getColor() == getColor()) ? res : null;
	}

	@Override
	public void handleBlockInteraction(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		ItemStack stack = player.getItemInHand();
		if (stack.getType() == Material.INK_SACK) {
			DyeColor newColor = DyeColor.getByDyeData((byte)stack.getDurability());
			if (newColor == getColor()) {
				return;
			}
			if (stack.getAmount() > 1) {
				stack.setAmount(stack.getAmount() - 1);
			} else {
				stack = null;
			}
			player.setItemInHand(stack);
			setColor(newColor);
			updateBlock();
			event.setCancelled(true);
		} else {
			PaintBrush brush = BaseSTBItem.getItemFromItemStack(player.getItemInHand(), PaintBrush.class);
			if (brush != null && brush.getPaintLevel() > 0 && brush.getColour() != getColor()) {
				brush.setPaintLevel(brush.getPaintLevel() - 1);
				player.setItemInHand(brush.toItemStack(1));
				setColor(brush.getColour());
				updateBlock();
				event.setCancelled(true);
			}
		}
	}
}
