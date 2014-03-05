package me.desht.sensibletoolbox.items;

import me.desht.dhutils.Debugger;
import me.desht.sensibletoolbox.api.STBBlock;
import me.desht.sensibletoolbox.blocks.PaintCan;
import me.desht.sensibletoolbox.storage.LocationManager;
import me.desht.sensibletoolbox.util.STBUtil;
import org.bukkit.Art;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Painting;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.material.Colorable;
import org.bukkit.material.MaterialData;

import java.util.HashSet;
import java.util.Set;

public class PaintBrush extends BaseSTBItem {
	private static final MaterialData md = new MaterialData(Material.GOLD_SPADE);
	private int paintLevel;
	private DyeColor colour;

	public PaintBrush() {
		colour = DyeColor.WHITE;
		paintLevel = 0;
	}

	public PaintBrush(ConfigurationSection conf) {
		setPaintLevel(conf.getInt("paintLevel"));
		setColour(DyeColor.valueOf(conf.getString("colour")));
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
	public boolean isEnchantable() {
		return false;
	}

	@Override
	public YamlConfiguration freeze() {
		YamlConfiguration res = super.freeze();
		res.set("paintLevel", paintLevel);
		res.set("colour", colour == null ? "" : colour.toString());
		return res;
	}

	@Override
	public MaterialData getMaterialData() {
		return md;
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
				"R-click block: paint up to " + getMaxBlocksAffected() + " blocks",
				"⇧ + R-click block: paint block",
				"⇧ + R-click air: empty brush",
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
		ShapedRecipe recipe = new ShapedRecipe(toItemStack());
		recipe.shape("R", "S", "S");
		recipe.setIngredient('R', Material.STRING);
		recipe.setIngredient('S', Material.STICK);
		return recipe;
	}

	protected int getMaxBlocksAffected() {
		return 9;
	}

	@Override
	public void onInteractItem(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			Block b = event.getClickedBlock();
			STBBlock stb = LocationManager.getManager().get(b.getLocation());
			if (stb instanceof PaintCan) {
				refillFromCan((PaintCan) stb);
			} else if (okToColor(b, stb)) {
				int painted;
				// Bukkit Colorable interface doesn't cover all colorable blocks at this time, only Wool
				if (player.isSneaking()) {
					// paint a single block
					painted = paintBlocks(b);
				} else {
					// paint multiple blocks around the clicked block
					Block[] blocks = findBlocksAround(b);
					painted = paintBlocks(blocks);
				}
				if (painted > 0) {
					player.playSound(player.getLocation(), Sound.WATER, 1.0f, 1.5f);
				}
			}
		} else if (event.getAction() == Action.RIGHT_CLICK_AIR && player.isSneaking()) {
			setPaintLevel(0);
		}
		player.setItemInHand(toItemStack());
		event.setCancelled(true);
	}

	private boolean okToColor(Block b, STBBlock stb) {
		if (stb != null && !(stb instanceof Colorable)) {
			// we don't want blocks which happen to use a Colorable material to be paintable
			return false;
		}
		return (STBUtil.isColorable(b.getType()) || b.getType() == Material.GLASS || b.getType() == Material.THIN_GLASS)
				&& getBlockColour(b) != getColour() && getPaintLevel() > 0;
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
		Debugger.getInstance().debug(can + " has " + can.getPaintLevel() + " of " + can.getColour());
		Debugger.getInstance().debug("try to fill brush with " + needed + ", actual = " + actual);
		if (actual > 0) {
			this.setColour(can.getColour());
			this.setPaintLevel(this.getPaintLevel() + actual);
			can.setPaintLevel(can.getPaintLevel() - actual);
			Debugger.getInstance().debug("brush now = " + this.getPaintLevel() + " " + this.getColour());
			Debugger.getInstance().debug("can now = " + can.getPaintLevel() + " " + can.getColour());
		}
	}

	@Override
	public void onInteractEntity(PlayerInteractEntityEvent event) {
		event.setCancelled(true);
		if (getPaintLevel() <= 0) {
			return;
		}
		boolean painted = false;
		Entity e = event.getRightClicked();
		if (e instanceof Colorable) {
			((Colorable) e).setColor(getColour());
			painted = true;
		} else if (e instanceof Painting) {
			Painting painting = (Painting) e;
			Art art = findNextArtwork(painting);
			painting.setArt(art);
			painted = true;
		}
		if (painted) {
			setPaintLevel(getPaintLevel() - 1);
			event.getPlayer().setItemInHand(toItemStack());
			event.getPlayer().playSound(e.getLocation(), Sound.WATER, 1.0f, 1.5f);
		}
	}

	private Art findNextArtwork(Painting painting) {
		Art current = painting.getArt();
		int i = (current.ordinal() + 1) % Art.values().length;
		while (i != current.ordinal()) {
			Art a = Art.values()[i];
			if (a.getBlockHeight() == current.getBlockHeight() && a.getBlockWidth() == current.getBlockWidth()) {
				return a;
			}
			i = (i + 1) % Art.values().length;
		}
		return current;
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
		return STBUtil.isColorable(b.getType()) ? DyeColor.getByWoolData(b.getData()) : null;
	}

	private int paintBlocks(Block... blocks) {
		int painted = 0;
		for (Block b : blocks) {
			Debugger.getInstance().debug(2, "painting! " + b + "  " + getPaintLevel() + " " + getColour());
			STBBlock stb = LocationManager.getManager().get(b.getLocation());
			if (stb != null && stb instanceof Colorable) {
				((Colorable) stb).setColor(getColour());
			} else {
				if (b.getType() == Material.GLASS) {
					b.setType(Material.STAINED_GLASS);
				} else if (b.getType() == Material.THIN_GLASS) {
					b.setType(Material.STAINED_GLASS_PANE);
				}
				b.setData(getColour().getWoolData());
			}
			painted++;
			setPaintLevel(getPaintLevel() - 1);
			if (getPaintLevel() <= 0) {
				break;
			}
		}
		return painted;
	}
}
