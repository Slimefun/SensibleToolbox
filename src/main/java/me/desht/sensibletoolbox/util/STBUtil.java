package me.desht.sensibletoolbox.util;

import me.desht.dhutils.Debugger;
import me.desht.dhutils.ItemNames;
import me.desht.sensibletoolbox.SensibleToolboxPlugin;
import me.desht.sensibletoolbox.api.Chargeable;
import me.desht.sensibletoolbox.items.BaseSTBItem;
import org.bukkit.*;
import org.bukkit.block.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.metadata.Metadatable;

import java.util.*;

public class STBUtil {
	public static final BlockFace[] directFaces = {
			BlockFace.UP, BlockFace.NORTH, BlockFace.EAST, BlockFace.DOWN, BlockFace.SOUTH, BlockFace.WEST
	};
	public static final BlockFace[] horizontalFaces = new BlockFace[] {
			BlockFace.SELF,
			BlockFace.NORTH_EAST, BlockFace.EAST, BlockFace.SOUTH_EAST, BlockFace.SOUTH,
			BlockFace.SOUTH_WEST, BlockFace.WEST, BlockFace.NORTH_WEST, BlockFace.NORTH
	};
	private static final int gravelChance[] = new int[] {
			0, 14, 25, 100
	};

	/**
	 * Check if the given material is a crop which can grow and/or be harvested.
	 *
	 * @param m the material to check
	 * @return true if the material is a crop
	 */
	public static boolean isCrop(Material m) {
		return m == Material.CROPS || m == Material.CARROT || m == Material.POTATO || m == Material.PUMPKIN_STEM || m == Material.MELON_STEM;
	}

	/**
	 * Check if the given material is any growing plant (not including leaves or trees)
	 *
	 * @param type the material to check
	 * @return true if the material is a plant
	 */
	public static boolean isPlant(Material type) {
		if (isCrop(type)) {
			return true;
		}
		switch (type) {
			case LONG_GRASS:
			case DOUBLE_PLANT:
			case YELLOW_FLOWER:
			case RED_ROSE:
			case SUGAR_CANE_BLOCK:
			case BROWN_MUSHROOM:
			case RED_MUSHROOM:
			case DEAD_BUSH:
			case SAPLING:
				return true;
		}
		return false;
	}

	/**
	 * Get the blocks horizontally surrounding the given block.
	 *
	 * @param b the centre block
	 * @return array of all blocks around the given block, including the given block as the first element
	 */
	public static Block[] getSurroundingBlocks(Block b) {
		Block[] result = new Block[horizontalFaces.length];
		for (int i = 0; i < horizontalFaces.length; i++) {
			result[i] = b.getRelative(horizontalFaces[i]);
		}
		return result;
	}

	public static Material getCropType(Material seedType) {
		switch (seedType) {
			case SEEDS: return Material.CROPS;
			case POTATO_ITEM: return Material.POTATO;
			case CARROT_ITEM: return Material.CARROT;
			case PUMPKIN_SEEDS: return Material.PUMPKIN_STEM;
			case MELON_SEEDS: return Material.MELON_STEM;
			default: return null;
		}
	}

	public static Object getMetadataValue(Metadatable m, String key) {
		for (MetadataValue mv : m.getMetadata(key)) {
			if (mv.getOwningPlugin() == SensibleToolboxPlugin.getInstance()) {
				return mv.value();
			}
		}
		return null;
	}

	public static ChatColor toChatColor(DyeColor c) {
		switch (c) {
			case BLACK: return ChatColor.DARK_GRAY;
			case BLUE: return ChatColor.DARK_BLUE;
			case BROWN: return ChatColor.GOLD;
			case CYAN: return ChatColor.AQUA;
			case GRAY: return ChatColor.GRAY;
			case GREEN: return ChatColor.DARK_GREEN;
			case LIGHT_BLUE: return ChatColor.BLUE;
			case LIME: return ChatColor.GREEN;
			case MAGENTA: return ChatColor.LIGHT_PURPLE;
			case ORANGE: return ChatColor.GOLD;
			case PINK: return ChatColor.LIGHT_PURPLE;
			case PURPLE: return ChatColor.DARK_PURPLE;
			case RED: return ChatColor.DARK_RED;
			case SILVER: return ChatColor.GRAY;
			case WHITE: return ChatColor.WHITE;
			case YELLOW: return ChatColor.YELLOW;
			default: throw new IllegalArgumentException("unknown dye color"  + c);
		}
	}

	public static boolean isInteractive(Material mat) {
		if (!mat.isBlock()) {
			return false;
		}
		switch (mat) {
			case DISPENSER:
			case NOTE_BLOCK:
			case BED_BLOCK:
			case CHEST:
			case WORKBENCH:
			case FURNACE:
			case BURNING_FURNACE:
			case WOODEN_DOOR:
			case LEVER:
			case REDSTONE_ORE:
			case STONE_BUTTON:
			case JUKEBOX:
			case CAKE_BLOCK:
			case DIODE_BLOCK_ON:
			case DIODE_BLOCK_OFF:
			case TRAP_DOOR:
			case FENCE_GATE:
			case ENCHANTMENT_TABLE:
			case BREWING_STAND:
			case DRAGON_EGG:
			case ENDER_CHEST:
			case COMMAND:
			case BEACON:
			case WOOD_BUTTON:
			case ANVIL:
			case TRAPPED_CHEST:
			case REDSTONE_COMPARATOR_ON:
			case REDSTONE_COMPARATOR_OFF:
			case HOPPER:
			case DROPPER:
				return true;
			default:
				return false;
		}
	}

	public static boolean isColorable(Material mat) {
		switch (mat) {
			case STAINED_GLASS: case STAINED_GLASS_PANE: case STAINED_CLAY: case CARPET: case WOOL:
				return true;
			default:
				return false;
		}
	}

	/**
	 * Check if the given material is obtainable - if false, then even silk touch won't help.
	 *
	 * @param mat the material to check
	 * @return true if the material can be obtained normally (including with silk touch)
	 */
	public static boolean isObtainable(Material mat) {
		switch (mat) {
			case CAKE_BLOCK: case CARROT: case COCOA: case DOUBLE_STEP: case WOOD_DOUBLE_STEP:
				case FIRE: case SOIL: case MELON_STEM: case MOB_SPAWNER: case NETHER_WARTS:
				case POTATO: case PUMPKIN_STEM: case SNOW: case SUGAR_CANE_BLOCK: case LONG_GRASS:
				case DOUBLE_PLANT: case CROPS:
				return false;
		}
		return true;
	}

	public static boolean isPotionIngredient(MaterialData type) {
		switch (type.getItemType()) {
			case NETHER_STALK: case REDSTONE: case GHAST_TEAR: case GLOWSTONE_DUST: case SULPHUR:
				case SPECKLED_MELON: case MAGMA_CREAM: case BLAZE_POWDER: case SUGAR: case SPIDER_EYE:
				case FERMENTED_SPIDER_EYE: case GOLDEN_CARROT:
				return true;
		}
		if (type.getItemType() == Material.RAW_FISH && type.getData() == 3) {
			return true; // pufferfish
		}
		return false;
	}

	/**
	 * Create a skull with the given player skin
	 *
	 * @param b block in which to create the skull
	 * @param name name of the skin
	 * @param player if not null, skull will face the opposite direction the player face
	 * @return the skull (the caller should call skull.update() when ready)
	 */
	public static Skull setSkullHead(Block b, String name, Player player) {
		b.setType(Material.SKULL);
		Skull skull = (Skull) b.getState();
		skull.setSkullType(SkullType.PLAYER);
		skull.setOwner(name);
		org.bukkit.material.Skull sk = (org.bukkit.material.Skull) skull.getData();
		sk.setFacingDirection(BlockFace.SELF);
		skull.setData(sk);
		if (player != null) {
			skull.setRotation(getRotation(player.getLocation()));
		}
		return skull;
	}

	private static BlockFace getRotation(Location loc) {
		double rot = loc.getYaw() % 360;
		if (rot < 0) {
			rot += 360;
		}
		if ((0 <= rot && rot < 45) || (315 <= rot && rot < 360.0)) {
			return BlockFace.NORTH;
		} else if (45 <= rot && rot < 135) {
			return BlockFace.EAST;
		} else if (135 <= rot && rot < 225) {
			return BlockFace.SOUTH;
		} else if (225 <= rot && rot < 315) {
			return BlockFace.WEST;
		} else {
			throw new IllegalArgumentException("impossible rotation: " + rot);
		}
	}

	/**
	 * Get the drops which the given block would produce, given the tool used to break it,
	 * taking into account any silk touch or looting enchantment on the tool (but not caring
	 * about the type of tool).
	 *
	 * @param b the block
	 * @param tool the tool
	 * @return a list of items which would drop from the block, if broken
	 */
	public static List<ItemStack> calculateDrops(Block b, ItemStack tool) {
		List<ItemStack> res = new ArrayList<ItemStack>();
		if (tool.getEnchantmentLevel(Enchantment.SILK_TOUCH) == 1 && isObtainable(b.getType())) {
			res.add(b.getState().getData().toItemStack());
		} else if (tool.getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS) > 0) {
			Random r = new Random();
			res.addAll(b.getDrops());
			int fortune = tool.getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS);
			switch (b.getType()) {
				case DIAMOND_ORE: case COAL_ORE: case EMERALD_ORE: case QUARTZ_ORE: case LAPIS_ORE:
					int n = r.nextInt(fortune + 2);
					res.get(0).setAmount(Math.max(1, n));
					break;
				case REDSTONE_ORE: case NETHER_WARTS: case POTATO: case CARROT:
					res.get(0).setAmount(res.get(0).getAmount() + fortune);
					break;
				case GLOWSTONE:
					res.get(0).setAmount(Math.min(4, res.get(0).getAmount() + fortune));
					break;
				case MELON_BLOCK:
					res.get(0).setAmount(Math.min(9, res.get(0).getAmount() + fortune));
					break;
				case CROPS:
					for (ItemStack drop : res) {
						if (drop.getType() == Material.SEEDS) {
							drop.setAmount(drop.getAmount() + fortune);
						}
					}
					break;
				case GRAVEL:
					if (res.get(0).getType() == Material.GRAVEL) {
						if (r.nextInt(100) < gravelChance[Math.min(fortune, gravelChance.length)]) {
							res.set(0, new ItemStack(Material.FLINT));
						}
					}
					break;
			}
		} else {
			res.addAll(b.getDrops());
		}

		if (Debugger.getInstance().getLevel() >= 2) {
			Debugger.getInstance().debug(2, "Block " + b + " would drop:");
			for (ItemStack stack : res) { Debugger.getInstance().debug(2, " - " + stack); }
		}

		return res;
	}

	public static boolean isExposed(Block b, BlockFace face) {
		return !b.getRelative(face).getType().isOccluding();
	}

	public static boolean isExposed(Block b) {
		for (BlockFace face : directFaces) {
			if (isExposed(b, face)) {
				return true;
			}
		}
		return false;
	}

	public static BlockFace getFaceFromYaw(float yaw) {
		double rot = yaw % 360;
		if (rot < 0) {
			rot += 360;
		}
		if ((0 <= rot && rot < 45) || (315 <= rot && rot < 360.0)) {
			return BlockFace.SOUTH;
		} else if (45 <= rot && rot < 135) {
			return BlockFace.WEST;
		} else if (135 <= rot && rot < 225) {
			return BlockFace.NORTH;
		} else if (225 <= rot && rot < 315) {
			return BlockFace.EAST;
		} else {
			throw new IllegalArgumentException("impossible rotation: " + rot);
		}
	}

	public static String getChargeString(Chargeable ch) {
		double d = ch.getCharge() / ch.getMaxCharge();
		ChatColor cc;
		if (d < 0.333) {
			cc = ChatColor.RED;
		} else if (d < 0.666) {
			cc = ChatColor.GOLD;
		} else {
			cc = ChatColor.GREEN;
		}
		return ChatColor.WHITE + "\u2301 " + cc + Math.round(ch.getCharge()) + "/" + ch.getMaxCharge() + " SCU";
	}

	public static int roundUp(int n, int nearestMultiple) {
		return n + nearestMultiple - 1 - (n - 1) % nearestMultiple;
	}

	public static String describeItemStack(ItemStack stack) {
		if (stack == null) { return "nothing"; }
		String res = stack.getAmount() + " x ";
		if (stack.hasItemMeta() && stack.getItemMeta().hasDisplayName()) {
			res += stack.getItemMeta().getDisplayName();
		} else {
			res += ItemNames.lookup(stack);
		}
		return res;
	}

	/**
	 * Convenience method to get a coloured material: stained clay/glass/glass panes/wool.
	 * Also gets all the deprecated method calls into one place.
	 *
	 * @param mat the material
	 * @param colour the colour
	 * @return the material data object
	 */
	public static MaterialData makeColouredMaterial(Material mat, DyeColor colour) {
		if (mat == Material.GLASS) mat = Material.STAINED_GLASS;
		else if (mat == Material.THIN_GLASS) mat = Material.STAINED_GLASS_PANE;

		return new MaterialData(mat, colour.getWoolData());
	}

	/**
	 * Workaround method: ensure that the given inventory is refreshed to all its viewers.
	 * This is needed in some cases to avoid ghost items being left around (in particular
	 * if an item is shift-clicked into a machine inventory and then shortly after moved
	 * to a different slot).
	 *
	 * @param inv the inventory to refresh
	 */
	public static void forceInventoryRefresh(Inventory inv) {
		// workaround to avoid leaving ghost items in the input slot
		for (HumanEntity entity : inv.getViewers()) {
			if (entity instanceof Player) {
				((Player) entity).updateInventory();
			}
		}
	}

	public static ItemStack makeStack(MaterialData materialData, String title, String... lore) {
		ItemStack stack = materialData.toItemStack();
		if (title != null) {
			ItemMeta meta = stack.getItemMeta();
			meta.setDisplayName(title);
			List<String> newLore = new ArrayList<String>(lore.length);
			for (String l : lore) {
				newLore.add(BaseSTBItem.LORE_COLOR + l);
			}
			meta.setLore(newLore);
			stack.setItemMeta(meta);
		}
		return stack;
	}
}
