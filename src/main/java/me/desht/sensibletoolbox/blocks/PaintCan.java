package me.desht.sensibletoolbox.blocks;

import me.desht.dhutils.Debugger;
import me.desht.dhutils.MiscUtil;
import me.desht.sensibletoolbox.SensibleToolboxPlugin;
import me.desht.sensibletoolbox.items.BaseSTBItem;
import me.desht.sensibletoolbox.items.PaintBrush;
import me.desht.sensibletoolbox.util.RelativePosition;
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
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.metadata.FixedMetadataValue;

public class PaintCan extends BaseSTBBlock {
	public static final String STB_PAINT_CAN = "STB_Paint_Can";
	public static final int MAX_PAINT_LEVEL = 200;
	private static final int PAINT_PER_DYE = 25;
	private int paintLevel;
	private DyeColor colour;

	public PaintCan(ConfigurationSection conf) {
		super(conf);
		setPaintLevel(conf.getInt("paintLevel"));
		setColour(DyeColor.valueOf(conf.getString("paintColour")));
	}

	public PaintCan() {
		paintLevel = 0;
		colour = DyeColor.WHITE;
	}

	public static String getMixerTitle() {
		return ChatColor.DARK_BLUE + "Paint Mixer";
	}

	public static int getMaxPaintLevel() {
		return MAX_PAINT_LEVEL;
	}

	public YamlConfiguration freeze() {
		YamlConfiguration conf = super.freeze();
		conf.set("paintColour", getColour().toString());
		conf.set("paintLevel", getPaintLevel());
		return conf;
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
				"R-click block with Paint Brush",
				" to refill the brush",
				"R-click block with anything else",
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
	public void onInteractBlock(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		ItemStack stack = player.getItemInHand();
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			PaintBrush brush = BaseSTBItem.getItemFromItemStack(stack, PaintBrush.class);
			if (!player.isSneaking() && brush == null) {
				// open inventory to mix more paint
				Inventory inv = Bukkit.createInventory(player, 9, getMixerTitle());
				player.openInventory(inv);
				// make a note of which block this inventory is connected to
				player.setMetadata(STB_PAINT_CAN,
						new FixedMetadataValue(SensibleToolboxPlugin.getInstance(), getLocation()));
				event.setCancelled(true);
			}
		} else if (event.getAction() == Action.LEFT_CLICK_BLOCK && stack.getType() == Material.SIGN) {
			attachLabelSign(event);
		}
	}

	@Override
	public String getDisplaySuffix() {
		return getPaintLevel() + " " + STBUtil.toChatColor(getColour()) + getColour();
	}

	@Override
	public RelativePosition[] getBlockStructure() {
		return new RelativePosition[] { new RelativePosition(0, 1, 0) };
	}

	@Override
	public void onBlockPlace(BlockPlaceEvent event) {
		// ensure there's enough room
		if (!event.getBlockPlaced().getRelative(BlockFace.UP).isEmpty()) {
			MiscUtil.errorMessage(event.getPlayer(), "Not enough room here (need one empty block above).");
			event.setCancelled(true);
			return;
		}

		super.onBlockPlace(event);

		Block above = event.getBlock().getRelative(BlockFace.UP);
		Skull skull = STBUtil.setSkullHead(above, "MHF_OakLog", event.getPlayer());
		skull.update();
	}

	@Override
	protected String[] getSignLabel() {
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
		int woolSlot = -1;

		// first try to find a milk bucket and a dye
		for (int i = 0; i < inventory.getSize(); i++) {
			if (inventory.getItem(i) != null) {
				if (inventory.getItem(i).getType() == Material.MILK_BUCKET && bucketSlot == -1) {
					bucketSlot = i;
				} else if (inventory.getItem(i).getType() == Material.INK_SACK && dyeSlot == -1) {
					dyeSlot = i;
				} else if (inventory.getItem(i).getType() == Material.WOOL && woolSlot == -1) {
					woolSlot = i;
				} else {
					// eject the item
					getLocation().getWorld().dropItemNaturally(getLocation(), inventory.getItem(i));
				}
			}
		}
		Debugger.getInstance().debug(this + ": wool=" + woolSlot + " dye=" + dyeSlot + " milk=" + bucketSlot);

		if (bucketSlot >= 0 && dyeSlot >= 0) {
			// ok, we have the items - mix it up!
			DyeColor newColour = DyeColor.getByDyeData((byte)inventory.getItem(dyeSlot).getDurability());
			int amount = inventory.getItem(dyeSlot).getAmount();
			int toUse = Math.min((getMaxPaintLevel() - getPaintLevel()) / PAINT_PER_DYE, amount);
			if (toUse == 0) {
				// not enough room for any mixing
				return false;
			}
			if (getColour() != newColour && getPaintLevel() > 0) {
				// two different colours - do they mix?
				DyeColor mixedColour = mixDyes(getColour(), newColour);
				if (mixedColour == null) {
					// no - just replace the can with the new colour
					toUse = Math.min(getMaxPaintLevel() / PAINT_PER_DYE, amount);
					setColour(newColour);
					setPaintLevel(PAINT_PER_DYE * toUse);
				} else {
					// yes, they mix
					setColour(mixedColour);
					setPaintLevel(Math.min(getMaxPaintLevel(), getPaintLevel() + PAINT_PER_DYE * toUse));
				}
			} else {
				setColour(newColour);
				setPaintLevel(Math.min(getMaxPaintLevel(), getPaintLevel() + PAINT_PER_DYE * toUse));
			}
			Debugger.getInstance().debug(this + ": paint mixed! now " + getPaintLevel() + " " + getColour());
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
		} else if (woolSlot >= 0 && getPaintLevel() > 0) {
			// soak up any paint with the wool, dye it and return it
			Debugger.getInstance().debug(this + ": wool soak!");
			setPaintLevel(0);
			ItemStack stack = inventory.getItem(woolSlot);
			stack.setDurability(getColour().getWoolData());
			inventory.setItem(woolSlot, stack);
			return true;
		} else {
			return false;
		}
	}

	private void maybeUpdateSign() {
		Block b = getLocation().getBlock();
		for (BlockFace face : new BlockFace[] { BlockFace.EAST, BlockFace.NORTH, BlockFace.WEST, BlockFace.SOUTH} ) {
			if (b.getRelative(face).getType() == Material.WALL_SIGN) {
				Debugger.getInstance().debug(this + ": update sign " + face);
				Sign sign = (Sign) b.getRelative(face).getState();
				org.bukkit.material.Sign s = (org.bukkit.material.Sign) sign.getData();
				if (sign.getLine(0).equals(getItemName())) {
					if (s.getAttachedFace() == face.getOppositeFace()) {
						String[] text = getSignLabel();
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

	private DyeColor mixDyes(DyeColor dye1, DyeColor dye2) {
		if (dye1.compareTo(dye2) > 0) {
			DyeColor tmp = dye2;
			dye2 = dye1;
			dye1 = tmp;
		} else if (dye1 == dye2) {
			return dye1;
		}
		Debugger.getInstance().debug(this + ": try mixing: " + dye1 + " " + dye2);
		if (dye1 == DyeColor.YELLOW && dye2 == DyeColor.RED) {
			return DyeColor.ORANGE;
		} else if (dye1 == DyeColor.WHITE && dye2 == DyeColor.RED) {
			return DyeColor.PINK;
		} else if (dye1 == DyeColor.BLUE && dye2 == DyeColor.GREEN) {
			return DyeColor.CYAN;
		} else if (dye1 == DyeColor.BLUE && dye2 == DyeColor.RED) {
			return DyeColor.PURPLE;
		}  else if (dye1 == DyeColor.WHITE && dye2 == DyeColor.BLACK) {
			return DyeColor.GRAY;
		} else if (dye1 == DyeColor.WHITE && dye2 == DyeColor.BLUE) {
			return DyeColor.LIGHT_BLUE;
		} else if (dye1 == DyeColor.WHITE && dye2 == DyeColor.GREEN) {
			return DyeColor.LIME;
		} else if (dye1 == DyeColor.PINK && dye2 == DyeColor.PURPLE) {
			return DyeColor.MAGENTA;
		} else if (dye1 == DyeColor.WHITE && dye2 == DyeColor.GRAY) {
			return DyeColor.SILVER;
		} else {
			// colours don't mix
			return null;
		}
	}
}
