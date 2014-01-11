package me.desht.sensibletoolbox.items;

import me.desht.sensibletoolbox.SensibleToolboxPlugin;
import me.desht.sensibletoolbox.blocks.BaseSTBBlock;
import me.desht.sensibletoolbox.blocks.PaintCan;
import me.desht.sensibletoolbox.util.STBUtil;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.material.Colorable;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PaintBrush extends BaseSTBItem {
	private int paintLevel = 0;
	private DyeColor colour;

	public PaintBrush(Configuration conf) {
		setPaintLevel(conf.getInt("paintLevel"));
		setColour(DyeColor.valueOf(conf.getString("colour")));
	}

	public PaintBrush() {
		colour = DyeColor.WHITE;
	}

	public int getMaxPaintLevel() {
		return 25;
	}

	public int getPaintLevel() {
		return paintLevel;
	}

	public void setPaintLevel(int paintLevel) {
		this.paintLevel = paintLevel;
	}

	public DyeColor getColour() {
		return colour;
	}

	public void setColour(DyeColor colour) {
		this.colour = colour;
	}

	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> res = super.serialize();
		res.put("paintLevel", paintLevel);
		res.put("colour", colour == null ? "" : colour.toString());
		return res;
	}

	@Override
	public Material getBaseMaterial() {
		return Material.GOLD_SPADE;
	}

	@Override
	public String getItemName() {
		return "Paintbrush";
	}

	@Override
	public String[] getLore() {
		return new String[] {
				"Paints colourable blocks:",
				" Wool, carpet, stained clay/glass",
				"Right-click block: paint up to " + getMaxBlocksAffected() + " blocks",
				"Shift-right-click block: paint block",
				"Shift-right-click air: empty brush",
		};
	}

	@Override
	public ItemStack toItemStack(int amount) {
		ItemStack stack = super.toItemStack(amount);
		short max = stack.getType().getMaxDurability();
		float d = getPaintLevel() / (float) getMaxPaintLevel();
		short dur = (short)(max * d);
		stack.setDurability((short)(max - dur));
		return stack;
	}

	@Override
	public String getDisplaySuffix() {
		return getPaintLevel() > 0 ? getPaintLevel() + " " + STBUtil.toChatColor(getColour()) + getColour() : null;
	}

	@Override
	public Recipe getRecipe() {
		ShapedRecipe recipe = new ShapedRecipe(toItemStack(1));
		recipe.shape("R", "S", "S");
		recipe.setIngredient('R', Material.STRING);
		recipe.setIngredient('S', Material.STICK);
		return recipe;
	}

	protected int getMaxBlocksAffected() {
		return 9;
	}

	@Override
	public void handleItemInteraction(PlayerInteractEvent event) {
		System.out.println("paintbrush interact " + event.getAction());

		Player player = event.getPlayer();
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			Block b = event.getClickedBlock();
			BaseSTBBlock stb = SensibleToolboxPlugin.getInstance().getLocationManager().get(b.getLocation());
			if (stb != null) {
				if (stb instanceof PaintCan) {
					refillFromCan((PaintCan) stb);
					player.setItemInHand(this.toItemStack(1));
				}
				event.setCancelled(true);
			} else if (STBUtil.isColorable(b.getType()) && getBlockColour(b) != getColour() && getPaintLevel() > 0) {
				if (player.isSneaking()) {
					// paint a single block
					paintBlocks(b);
				} else {
					// paint multiple blocks around the clicked block
					Block[] blocks = findBlocksAround(b);
					paintBlocks(blocks);
				}
				player.setItemInHand(toItemStack(1));
				event.setCancelled(true);
			}
		} else if (event.getAction() == Action.RIGHT_CLICK_AIR && player.isSneaking()) {
			setPaintLevel(0);
			player.setItemInHand(toItemStack(1));
		}
	}

	private void refillFromCan(PaintCan can) {
		int needed;
		if (this.getColour() == can.getColour()) {
			needed = this.getMaxPaintLevel() - this.getPaintLevel();
		} else {
			this.setPaintLevel(0);
			needed = this.getMaxPaintLevel();
		}
		int actual = Math.min(needed, can.getPaintLevel());
		System.out.println("paint can has " + can.getPaintLevel() + " of " + can.getColour());
		System.out.println("want to fill brush with " + needed + ", actual = " + actual);
		if (actual > 0) {
			this.setColour(can.getColour());
			this.setPaintLevel(this.getPaintLevel() + actual);
			can.setPaintLevel(can.getPaintLevel() - actual);
			can.updateBlock();
			System.out.println("brush now = " + this.getPaintLevel() + " " + this.getColour());
			System.out.println("can now = " + can.getPaintLevel() + " " + can.getColour());
		}
	}

	@Override
	public void handleEntityInteraction(PlayerInteractEntityEvent event) {
		Entity e = event.getRightClicked();
		if (e instanceof Colorable && this.getPaintLevel() > 0) {
			((Colorable) e).setColor(this.getColour());
			this.setPaintLevel(this.getPaintLevel() - 1);
			event.getPlayer().setItemInHand(toItemStack(1));
			event.getPlayer().playSound(e.getLocation(), Sound.SPLASH2, 1.0f, 1.5f);
		}
	}

	private Block[] findBlocksAround(Block b) {
		Set<Block> blocks = new HashSet<Block>();
		find(b, b.getType(), blocks, getMaxBlocksAffected());
		return blocks.toArray(new Block[blocks.size()]);
	}

	private void find(Block b, Material mat, Set<Block> blocks, int max) {
		if (b.getType() != mat || getBlockColour(b) == getColour()) {
			return;
		} else if (blocks.contains(b)) {
			return;
		} else if (blocks.size() > max) {
			return;
		}
		blocks.add(b);
		find(b.getRelative(BlockFace.UP), mat, blocks, max);
		find(b.getRelative(BlockFace.DOWN), mat, blocks, max);
		find(b.getRelative(BlockFace.EAST), mat, blocks, max);
		find(b.getRelative(BlockFace.NORTH), mat, blocks, max);
		find(b.getRelative(BlockFace.WEST), mat, blocks, max);
		find(b.getRelative(BlockFace.SOUTH), mat, blocks, max);
	}

	private DyeColor getBlockColour(Block b) {
		return DyeColor.getByWoolData(b.getData());
	}

	private void paintBlocks(Block... blocks) {
		for (Block b : blocks) {
			System.out.println("painting! " + b + "  " + getPaintLevel() + " " + getColour());
			DyeColor c = DyeColor.getByWoolData(b.getData());
			b.setData(getColour().getWoolData());
			setPaintLevel(getPaintLevel() - 1);
			if (getPaintLevel() <= 0) {
				break;
			}
		}
	}
}
