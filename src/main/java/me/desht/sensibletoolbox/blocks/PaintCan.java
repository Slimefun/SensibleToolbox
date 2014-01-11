package me.desht.sensibletoolbox.blocks;

import me.desht.dhutils.MiscUtil;
import me.desht.sensibletoolbox.SensibleToolboxPlugin;
import me.desht.sensibletoolbox.items.BaseSTBItem;
import me.desht.sensibletoolbox.items.PaintBrush;
import me.desht.sensibletoolbox.util.STBUtil;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.Skull;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

import java.util.Map;

public class PaintCan extends BaseSTBBlock {
	public static final String STB_PAINT_CAN = "STB_Paint_Can";
	public static final int MAX_PAINT_LEVEL = 200;
	private static final int PAINT_PER_DYE = 25;
	private int paintLevel;
	private DyeColor colour;

	public PaintCan(Configuration conf) {
		setPaintLevel(conf.getInt("paintLevel"));
		setColour(DyeColor.valueOf(conf.getString("paintColour")));
	}

	public PaintCan() {
		paintLevel = 0;
		colour = DyeColor.WHITE;
	}

	public static PaintCan deserialize(Map<String,Object> map) {
		return new PaintCan(getConfigFromMap(map));
	}

	public static String getMixerTitle() {
		return ChatColor.DARK_BLUE + "Paint Mixer";
	}

	public static int getMaxPaintLevel() {
		return MAX_PAINT_LEVEL;
	}

	public Map<String, Object> serialize() {
		Map<String, Object> res = super.serialize();
		res.put("paintColour", getColour().toString());
		res.put("paintLevel", getPaintLevel());
		return res;
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
	public Material getBaseMaterial() {
		return getPaintLevel() > 0 ? Material.WOOL : Material.STAINED_GLASS;
	}

	@Override
	public String getItemName() {
		return "Paint Can";
	}

	@Override
	public String[] getLore() {
		return new String[] {
				"Right-click block with Paint Brush",
				" to refill the brush",
				"Right-click block with anything else",
				" to open mixer; place milk bucket and",
				" a dye inside to mix some paint"
		};
	}

	@Override
	public Recipe getRecipe() {
		ShapedRecipe recipe = new ShapedRecipe(toItemStack(1));
		recipe.shape("ISI", "I I", "III");
		recipe.setIngredient('S', Material.WOOD_STEP);
		recipe.setIngredient('I', Material.IRON_INGOT);
		return recipe;
	}

	@Override
	public Byte getBaseBlockData() {
		return colour.getWoolData();
	}

	@Override
	public void handleBlockInteraction(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			BaseSTBItem item = BaseSTBItem.getItemFromItemStack(player.getItemInHand());
			if (!player.isSneaking() && (item == null || !(item instanceof PaintBrush))) {
				// open inventory to mix more paint
				Inventory inv = Bukkit.createInventory(player, 9, getMixerTitle());
				player.openInventory(inv);
				// make a note of which block this inventory is connected to
				player.setMetadata(STB_PAINT_CAN,
						new FixedMetadataValue(SensibleToolboxPlugin.getInstance(), getBaseLocation()));
				event.setCancelled(true);
			}
		} else if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
			ItemStack stack = player.getItemInHand();
			if (stack.getType() == Material.SIGN) {
				// attach a label sign
				Block signBlock = event.getClickedBlock().getRelative(event.getBlockFace());
				signBlock.setType(Material.WALL_SIGN);
				Sign sign = (Sign) signBlock.getState();
				org.bukkit.material.Sign s = (org.bukkit.material.Sign) sign.getData();
				s.setFacingDirection(event.getBlockFace());
				sign.setData(s);
				String[] text = getSignText();
				for (int i = 0; i < text.length; i++) {
					sign.setLine(i, text[i]);
				}
				sign.update();
				if (stack.getAmount() > 1) {
					stack.setAmount(stack.getAmount() - 1);
					player.setItemInHand(stack);
				} else {
					player.setItemInHand(null);
				}
			}
		}
	}

	@Override
	public String getDisplaySuffix() {
		return getPaintLevel() + " " + STBUtil.toChatColor(getColour()) + getColour();
	}

	@Override
	public Vector[] getBlockStructure() {
		return new Vector[] { new Vector(0, 1, 0) };
	}

	@Override
	public void handleBlockPlace(BlockPlaceEvent event) {
		// ensure there's enough room
		if (!event.getBlockPlaced().getRelative(BlockFace.UP).isEmpty()) {
			MiscUtil.errorMessage(event.getPlayer(), "Not enough room here (need one empty block above).");
			event.setCancelled(true);
			return;
		}

		super.handleBlockPlace(event);

		Block above = event.getBlock().getRelative(BlockFace.UP);
		Skull skull = STBUtil.setSkullHead(above, "MHF_OakLog", event.getPlayer());
		skull.update();
	}

	private String[] getSignText() {
		String res[] = new String[4];
		ChatColor cc = STBUtil.toChatColor(getColour());
		res[0] = getItemName();
		res[1] = cc.toString() + getColour();
		res[2] = getPaintLevel() + "/" + getMaxPaintLevel();
		res[3] = cc + StringUtils.repeat("\u25fc", (getPaintLevel() * 13) / getMaxPaintLevel());
		return res;
	}

	/**
	 * Attempt to refill the can from the contents of the given inventory.  The mixer needs to find
	 * a milk bucket and at least one dye.  If mixing is successful, the bucket is replace with an
	 * empty bucket and one dye is removed, the method returns true, and it is up to the caller to dispose
	 * of any remaining items in the inventory as appropriate (either returning them or destroying them).
	 *
	 * @param inventory the inventory to supply items from
	 * @return true if mixing was successful, false otherwise
	 */
	public boolean tryMix(Inventory inventory) {
		int bucketSlot = -1;
		int dyeSlot = -1;
		// first try to find a milk bucket and a dye
		for (int i = 0; i < inventory.getSize(); i++) {
			if (inventory.getItem(i) != null) {
				if (inventory.getItem(i).getType() == Material.MILK_BUCKET && bucketSlot == -1) {
					bucketSlot = i;
				} else if (inventory.getItem(i).getType() == Material.INK_SACK && dyeSlot == -1) {
					dyeSlot = i;
				} else {
					// eject the item
					getBaseLocation().getWorld().dropItemNaturally(getBaseLocation(), inventory.getItem(i));
				}
			}
		}

		if (bucketSlot >= 0 && dyeSlot >= 0) {
			// ok, we have the items - mix it up!
			DyeColor newColour = DyeColor.getByDyeData((byte)inventory.getItem(dyeSlot).getDurability());
			int amount = inventory.getItem(dyeSlot).getAmount();
			int toUse = Math.min((getMaxPaintLevel() - getPaintLevel()) / PAINT_PER_DYE, amount);
			if (getColour() != newColour) {
				// maybe one day we'll allow mixing colours but for now just replace it
				setColour(newColour);
				setPaintLevel(PAINT_PER_DYE * toUse);
			} else {
				setPaintLevel(Math.min(getMaxPaintLevel(), getPaintLevel() + PAINT_PER_DYE * toUse));
			}
			System.out.println("paint mixed! now " + getPaintLevel() + " " + getColour());
			updateBlock();
			inventory.setItem(bucketSlot, new ItemStack(Material.BUCKET));
			ItemStack dyeStack = inventory.getItem(dyeSlot);
			if (dyeStack.getAmount() > 1) {
				dyeStack.setAmount(dyeStack.getAmount() - toUse);
				inventory.setItem(dyeSlot, dyeStack);
			} else {
				inventory.setItem(dyeSlot, null);
			}
			return true;
		} else {
			return false;
		}
	}

	private void maybeUpdateSign() {
		Block b = getBaseLocation().getBlock();
		for (BlockFace face : new BlockFace[] { BlockFace.EAST, BlockFace.NORTH, BlockFace.WEST, BlockFace.SOUTH} ) {
			if (b.getRelative(face).getType() == Material.WALL_SIGN) {
				System.out.println(" update sign " + face);
				Sign sign = (Sign) b.getRelative(face).getState();
				org.bukkit.material.Sign s = (org.bukkit.material.Sign) sign.getData();
				if (sign.getLine(0).equals(getItemName())) {
					if (s.getAttachedFace() == face.getOppositeFace()) {
						String[] text = getSignText();
						for (int i = 0; i < text.length; i++) {
							sign.setLine(i, text[i]);
						}
						sign.update();
						break;
					}
				}
			}
		}
	}

	@Override
	public void updateBlock() {
		super.updateBlock();
		maybeUpdateSign();
	}
}
