package io.github.thebusybiscuit.sensibletoolbox.api.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.Sound;
import org.bukkit.TreeSpecies;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Skull;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.Dye;
import org.bukkit.material.MaterialData;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.metadata.Metadatable;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;

import io.github.thebusybiscuit.sensibletoolbox.SensibleToolboxPlugin;
import io.github.thebusybiscuit.sensibletoolbox.api.SensibleToolbox;
import io.github.thebusybiscuit.sensibletoolbox.api.energy.Chargeable;
import io.github.thebusybiscuit.sensibletoolbox.api.items.BaseSTBItem;
import io.github.thebusybiscuit.sensibletoolbox.attributes.Attributes;
import me.desht.dhutils.DHUtilsException;
import me.desht.dhutils.Debugger;
import me.desht.dhutils.ItemGlow;
import me.desht.dhutils.MiscUtil;
import me.desht.dhutils.block.BlockUtil;

/**
 * A collection of miscellaneous utility methods.
 */
public class STBUtil {
    /**
     * The block faces directly adjacent to a block.
     */
    public static final BlockFace[] directFaces = {
            BlockFace.UP, BlockFace.NORTH, BlockFace.EAST, BlockFace.DOWN, BlockFace.SOUTH, BlockFace.WEST
    };
    /**
     * The block faces in the four cardinal horizontal directions.
     */
    public static final BlockFace[] mainHorizontalFaces = new BlockFace[]{
            BlockFace.EAST, BlockFace.SOUTH,
            BlockFace.WEST, BlockFace.NORTH
    };
    /**
     * The block faces in all eight compass directions, plus the block itself.
     */
    public static final BlockFace[] allHorizontalFaces = new BlockFace[]{
            BlockFace.SELF,
            BlockFace.NORTH_EAST, BlockFace.EAST, BlockFace.SOUTH_EAST, BlockFace.SOUTH,
            BlockFace.SOUTH_WEST, BlockFace.WEST, BlockFace.NORTH_WEST, BlockFace.NORTH
    };

    private static final int gravelChance[] = new int[]{
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
            case CACTUS:
                return true;
		default:
			break;
        }
        return false;
    }

    /**
     * Check if the given material is a wearable (armour) item.
     *
     * @param type the material to check
     * @return true if the material is wearable
     */
    public static boolean isWearable(Material type) {
        switch (type) {
            case LEATHER_HELMET:
            case LEATHER_BOOTS:
            case LEATHER_CHESTPLATE:
            case LEATHER_LEGGINGS:
            case IRON_HELMET:
            case IRON_BOOTS:
            case IRON_CHESTPLATE:
            case IRON_LEGGINGS:
            case GOLDEN_HELMET:
            case GOLDEN_BOOTS:
            case GOLDEN_CHESTPLATE:
            case GOLDEN_LEGGINGS:
            case DIAMOND_HELMET:
            case DIAMOND_BOOTS:
            case DIAMOND_CHESTPLATE:
            case DIAMOND_LEGGINGS:
            case CHAINMAIL_HELMET:
            case CHAINMAIL_BOOTS:
            case CHAINMAIL_CHESTPLATE:
            case CHAINMAIL_LEGGINGS:
                return true;
            default:
                return false;
        }
    }

    /**
     * Get the blocks horizontally surrounding the given block.
     *
     * @param b the centre block
     * @return array of all blocks around the given block, including the given block as the first element
     */
    public static Block[] getSurroundingBlocks(Block b) {
        Block[] result = new Block[allHorizontalFaces.length];
        for (int i = 0; i < allHorizontalFaces.length; i++) {
            result[i] = b.getRelative(allHorizontalFaces[i]);
        }
        return result;
    }

    public static Material getCropType(Material seedType) {
        switch (seedType) {
            case WHEAT_SEEDS:
                return Material.WHEAT;
            case POTATO:
                return Material.POTATOES;
            case CARROT:
                return Material.CARROTS;
            case PUMPKIN_SEEDS:
                return Material.PUMPKIN_STEM;
            case MELON_SEEDS:
                return Material.MELON_STEM;
            default:
                return null;
        }
    }

    /**
     * Convenience wrapper to get the metadata value set by STB.
     *
     * @param m the metadatable object
     * @param key the metadata key
     * @return the metadata value, or null if there is none
     */
    public static Object getMetadataValue(Metadatable m, String key) {
        for (MetadataValue mv : m.getMetadata(key)) {
            if (mv.getOwningPlugin() == SensibleToolboxPlugin.getInstance()) {
                return mv.value();
            }
        }
        return null;
    }

    /**
     * Convert a dye colour to the nearest matching chat colour.
     *
     * @param dyeColor the dye colour
     * @return the nearest matching chat colour
     */
    public static ChatColor dyeColorToChatColor(DyeColor dyeColor) {
        switch (dyeColor) {
            case BLACK:
                return ChatColor.DARK_GRAY;
            case BLUE:
                return ChatColor.DARK_BLUE;
            case BROWN:
                return ChatColor.GOLD;
            case CYAN:
                return ChatColor.AQUA;
            case GRAY:
                return ChatColor.GRAY;
            case GREEN:
                return ChatColor.DARK_GREEN;
            case LIGHT_BLUE:
                return ChatColor.BLUE;
            case LIME:
                return ChatColor.GREEN;
            case MAGENTA:
                return ChatColor.LIGHT_PURPLE;
            case ORANGE:
                return ChatColor.GOLD;
            case PINK:
                return ChatColor.LIGHT_PURPLE;
            case PURPLE:
                return ChatColor.DARK_PURPLE;
            case RED:
                return ChatColor.DARK_RED;
            case SILVER:
                return ChatColor.GRAY;
            case WHITE:
                return ChatColor.WHITE;
            case YELLOW:
                return ChatColor.YELLOW;
            default:
                throw new IllegalArgumentException("unknown dye color" + dyeColor);
        }
    }

    /**
     * Check if the given material is "interactive", i.e. a default action occurs
     * when a block of that material is right-clicked.
     *
     * @param mat the material to check
     * @return true if the material is interactive; false otherwise
     */
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

    /**
     * Check if the given material is colourable.  Ideally the relevant MaterialData
     * should implement the Colorable interface, but Bukkit doesn't have that yet...
     *
     * @param mat the material to check
     * @return true if the material is colourable; false otherwise
     */
    public static boolean isColorable(Material mat) {
        switch (mat) {
            case STAINED_GLASS:
            case STAINED_GLASS_PANE:
            case STAINED_CLAY:
            case CARPET:
            case WOOL:
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
            case CAKE_BLOCK:
            case CARROT:
            case COCOA:
            case DOUBLE_STEP:
            case WOOD_DOUBLE_STEP:
            case FIRE:
            case SOIL:
            case MELON_STEM:
            case MOB_SPAWNER:
            case NETHER_WARTS:
            case POTATO:
            case PUMPKIN_STEM:
            case SNOW:
            case SUGAR_CANE_BLOCK:
            case LONG_GRASS:
            case DOUBLE_PLANT:
            case CROPS:
                return false;
		default:
			break;
        }
        return true;
    }

    /**
     * Check if the given material can be used as a potion ingredient in vanilla
     * potion brewing.
     *
     * @param type the material (including data) to check
     * @return true if the material can be used as a potion ingredient; false otherwise
     */
    @SuppressWarnings("deprecation")
	public static boolean isPotionIngredient(MaterialData type) {
        switch (type.getItemType()) {
            case NETHER_STALK:
            case REDSTONE:
            case GHAST_TEAR:
            case GLOWSTONE_DUST:
            case SULPHUR:
            case SPECKLED_MELON:
            case MAGMA_CREAM:
            case BLAZE_POWDER:
            case SUGAR:
            case SPIDER_EYE:
            case FERMENTED_SPIDER_EYE:
            case GOLDEN_CARROT:
                return true;
            case RAW_FISH:
                return type.getData() == 3;
            default:
                return false;
        }
    }

    /**
     * Check if the given material is a burnable fuel.
     *
     * @param mat the material to check
     * @return true if the material can be used as furnace fuel; false otherwise
     */
    public static boolean isFuel(Material mat) {
        switch (mat) {
            case WOOD:
            case WOOD_AXE:
            case WOOD_HOE:
            case WOOD_PICKAXE:
            case WOOD_SPADE:
            case WOOD_SWORD:
            case LOG:
            case LOG_2:
            case STICK:
            case SAPLING:
            case BLAZE_ROD:
            case LAVA_BUCKET:
            case COAL:
                return true;
		default:
			break;
        }
        return false;
    }

    /**
     * Create a skull with the given player skin
     *
     * @param b      block in which to create the skull
     * @param name   name of the skin
     * @param player skull will face the opposite direction the player faces
     * @return the skull (the caller should call skull.update() when ready)
     */
    public static Skull setSkullHead(Block b, String name, Player player) {
        return setSkullHead(b, name, getFaceFromYaw(player.getLocation().getYaw()).getOppositeFace());
    }

    /**
     * Create a skull with the given player skin
     *
     * @param b      block in which to create the skull
     * @param name   name of the skin
     * @param direction direction the skull should face
     * @return the skull (the caller should call skull.update() when ready)
     */
    public static Skull setSkullHead(Block b, String name, BlockFace direction) {
        b.setType(Material.SKULL);
        Skull skull = (Skull) b.getState();
        skull.setSkullType(SkullType.PLAYER);
        skull.setOwner(name);
        org.bukkit.material.Skull sk = (org.bukkit.material.Skull) skull.getData();
        sk.setFacingDirection(BlockFace.SELF);
        skull.setData(sk);
        skull.setRotation(direction);
        return skull;
    }

    /**
     * Given a yaw value, get the closest block face for the given yaw.  The yaw value
     * is usually obtained via {@link org.bukkit.Location#getYaw()}
     *
     * @param yaw the yaw to convert
     * @return the nearest block face
     */
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

    /**
     * Get the drops which the given block could produce, given the tool used to break it,
     * taking into account any silk touch or looting enchantment on the tool as well as the
     * type of tool.  For blocks which can drop a random number of items, this method
     * may return a different result each time it's called.
     *
     * @param b    the block
     * @param tool the tool, may be null for an empty hand
     * @return a list of items which would drop from the block, if broken
     */
    @SuppressWarnings("deprecation")
	public static List<ItemStack> calculateDrops(Block b, ItemStack tool) {
        List<ItemStack> res = new ArrayList<ItemStack>();
        if (tool != null && tool.getEnchantmentLevel(Enchantment.SILK_TOUCH) == 1 && isObtainable(b.getType())) {
            res.add(b.getState().getData().toItemStack(1));
        } else {
            Random r = new Random();
            Collection<ItemStack> res2 = tool == null ? b.getDrops() : b.getDrops(tool);
            // b.getDrops() appears bugged for nether wart
            if (b.getType() != Material.NETHER_WARTS && res2.isEmpty()) {
                if (tool != null) {
                    res2 = b.getDrops();
                    if (res2.isEmpty()) {
                        return res;
                    }
                } else {
                    return res;
                }
            }
            int fortune = tool == null ? 0 : tool.getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS);
            switch (b.getType()) {
                case DIAMOND_ORE:
                    res.add(new ItemStack(Material.DIAMOND, getOreMultiplier(fortune, r))); break;
                case COAL_ORE:
                    res.add(new ItemStack(Material.COAL, getOreMultiplier(fortune, r))); break;
                case EMERALD_ORE:
                    res.add(new ItemStack(Material.EMERALD, getOreMultiplier(fortune, r))); break;
                case QUARTZ_ORE:
                    res.add(new ItemStack(Material.QUARTZ, getOreMultiplier(fortune, r))); break;
                case LAPIS_ORE:
                    res.add(makeColouredMaterial(Material.INK_SACK, DyeColor.BLUE).toItemStack((r.nextInt(4) + 5) * getOreMultiplier(fortune, r)));
                    break;
                case REDSTONE_ORE:
                    res.add(new ItemStack(Material.REDSTONE, (r.nextInt(2 + fortune) + 4)));
                    break;
                case NETHER_WARTS:
                    if (b.getData() >= 3) {
                        res.add(new ItemStack(Material.NETHER_STALK, r.nextInt(3 + fortune) + 2));
                    } else {
                        res.add(new ItemStack(Material.NETHER_STALK, 1));
                    }
                    break;
                case POTATO:
                    if (b.getData() >= 7) {
                        res.add(new ItemStack(Material.POTATO_ITEM, r.nextInt(4 + fortune) + 1));
                    } else {
                        res.add(new ItemStack(Material.POTATO_ITEM, 1));
                    }
                    break;
                case CARROT:
                    if (b.getData() >= 7) {
                        res.add(new ItemStack(Material.CARROT_ITEM, r.nextInt(4 + fortune) + 1));
                    } else {
                        res.add(new ItemStack(Material.CARROT_ITEM, 1));
                    }
                    break;
                case GLOWSTONE:
                    res.add(new ItemStack(Material.GLOWSTONE_DUST, Math.min(4, r.nextInt(3 + fortune) + 2)));
                    break;
                case MELON_BLOCK:
                    res.add(new ItemStack(Material.MELON, Math.min(9, r.nextInt(5 + fortune) + 3)));
                    break;
                case CROPS:
                    if (b.getData() >= 7) {
                        res.add(new ItemStack(Material.WHEAT));
                        res.add(new ItemStack(Material.SEEDS, r.nextInt(4 + fortune)));
                    } else {
                        res.add(new ItemStack(Material.SEEDS, 1));
                    }
                    break;
                case GRAVEL:
                    res.add(new ItemStack(Material.GRAVEL));
                    if (r.nextInt(100) < gravelChance[Math.min(fortune, gravelChance.length)]) {
                        res.add(new ItemStack(Material.FLINT));
                    }
                    break;
                default:
                    res.addAll(tool == null ? b.getDrops() : b.getDrops(tool));
            }
        }

        if (Debugger.getInstance().getLevel() >= 2) {
            Debugger.getInstance().debug(2, "Block " + b + " would drop:");
            for (ItemStack stack : res) {
                Debugger.getInstance().debug(2, " - " + stack);
            }
        }

        return res;
    }

    private static int getOreMultiplier(int fortune, Random r) {
        switch (fortune) {
            case 1: return r.nextInt(3) == 0 ? 2 : 1;
            case 2: switch (r.nextInt(4)) {
                case 0: return 2;
                case 1: return 3;
                default: return 1;
            }
            case 3: switch (r.nextInt(4)) {
                case 0: return 2;
                case 1: return 3;
                case 2: return 4;
                default: return 1;

            }
            default: return 1;
        }
    }

    /**
     * Check if the given block is exposed to the world on the given face.
     * {@link org.bukkit.Material#isOccluding()} is used to check if a face is exposed.
     *
     * @param block the block to check
     * @param face the face to check
     * @return true if the given block face is exposed
     */
    public static boolean isExposed(Block block, BlockFace face) {
        return !block.getRelative(face).getType().isOccluding();
    }

    /**
     * Check if the given block is exposed on <i>any</i> face.
     * {@link org.bukkit.Material#isOccluding()} is used to check if a face is exposed.
     *
     * @param block the block to check
     * @return true if any face of the block is exposed
     */
    public static boolean isExposed(Block block) {
        for (BlockFace face : directFaces) {
            if (isExposed(block, face)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get a display-formatted string for the given chargeable item or block's current
     * charge level.
     *
     * @param ch the chargeable item or block
     * @return a formatted string showing the chargeable's charge
     */
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

    /**
     * Round up the given value to given nearest multiple.
     *
     * @param n the value to round up
     * @param nearestMultiple the nearest multiple to round to
     * @return the rounded value
     */
    public static int roundUp(int n, int nearestMultiple) {
        return n + nearestMultiple - 1 - (n - 1) % nearestMultiple;
    }

    /**
     * Get a display-formatted string for the given ItemStack.  The item stack's metadata is
     * taken into account, as it the size of the stack.
     *
     * @param stack the item stack
     * @return a formatted description of the item stack
     */
    public static String describeItemStack(ItemStack stack) {
        if (stack == null) {
            return "nothing";
        }
        String res = stack.getAmount() + " x " + me.mrCookieSlime.CSCoreLibPlugin.general.String.StringUtils.formatItemName(stack, false);
        return res;
    }

    /**
     * Convenience method to get a coloured material: stained clay/glass/glass panes/wool/dye.
     * Also gets all the deprecated method calls into one place.
     *
     * @param mat    the material
     * @param colour the colour
     * @return the material data object
     */
    @SuppressWarnings("deprecation")
	public static MaterialData makeColouredMaterial(Material mat, DyeColor colour) {
        if (mat == Material.GLASS) mat = Material.STAINED_GLASS;
        else if (mat == Material.THIN_GLASS)
            mat = Material.STAINED_GLASS_PANE;

        return new MaterialData(mat, mat == Material.INK_SACK ? colour.getDyeData() : colour.getWoolData());
    }

    /**
     * Convenience method to get a log of the right species
     *
     * @param species the tree species
     * @return a MaterialData object representing the right log type
     */
    @SuppressWarnings("deprecation")
	public static MaterialData makeLog(TreeSpecies species) {
        switch (species) {
            case DARK_OAK:
            case ACACIA:
                return new MaterialData(Material.LOG_2, (byte) (species.getData() - 4));
            default:
                return new MaterialData(Material.LOG, species.getData());
        }
    }

    /**
     * Convenience method to get a plank of the right species
     *
     * @param species the tree species
     * @return a MaterialData object representing the right plank type
     */
    @SuppressWarnings("deprecation")
	public static MaterialData makePlank(TreeSpecies species) {
        return new MaterialData(Material.WOOD, species.getData());
    }

    /**
     * Convenience method to get leaves of the right species
     *
     * @param species the tree species
     * @return a MaterialData object representing the right log type
     */
    @SuppressWarnings("deprecation")
	public static MaterialData makeLeaves(TreeSpecies species) {
        switch (species) {
            case DARK_OAK:
            case ACACIA:
                return new MaterialData(Material.LEAVES_2, (byte) (species.getData() - 4));
            default:
                return new MaterialData(Material.LEAVES, species.getData());
        }
    }
    
    @SuppressWarnings("deprecation")
	public static MaterialData makeSapling(TreeSpecies species) {
        return new MaterialData(Material.SAPLING, species.getData());
    }

    /**
     * Workaround method: ensure that the given inventory is refreshed to all its viewers.
     * This is needed in some cases to avoid ghost items being left around (in particular
     * if an item is shift-clicked into a machine inventory and then shortly after moved
     * to a different slot).
     *
     * @param inv the inventory to refresh
     */
    @SuppressWarnings("deprecation")
	public static void forceInventoryRefresh(Inventory inv) {
        // workaround to avoid leaving ghost items in the input slot
        for (HumanEntity entity : inv.getViewers()) {
            if (entity instanceof Player) {
                ((Player) entity).updateInventory();
            }
        }
    }

    /**
     * Convenience method to create an item stack with defined title and lore data
     *
     * @param material the material to create the stack from
     * @param title the item's title, may be null
     * @param lore the item's lore, may be empty
     * @return a new ItemStack with the given title and lore
     */
    public static ItemStack makeStack(Material material, String title, String... lore) {
        return makeStack(new MaterialData(material), title, lore);
    }

    /**
     * Convenience method to create an item stack with defined title and lore data
     *
     * @param materialData the material data to create the stack from
     * @param title the item's title, may be null
     * @param lore the item's lore, may be empty
     * @return a new ItemStack with the given title and lore
     */
    public static ItemStack makeStack(MaterialData materialData, String title, String... lore) {
        ItemStack stack = materialData.toItemStack(1);
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

    /**
     * Check if the given block is a water source block.
     *
     * @param block the block to check
     * @return true if the block is a water source block
     */
    @SuppressWarnings("deprecation")
	public static boolean isLiquidSourceBlock(Block block) {
        return block.isLiquid() && block.getData() == 0;
    }

    /**
     * Check if the given block is an infinite water source.  It must be a non-flowing
     * source water block with at least 2 non-flowing source water block neighbours.
     *
     * @param block the block to check
     * @return true if the block is an infinite water source
     */
    public static boolean isInfiniteWaterSource(Block block) {
        if (isLiquidSourceBlock(block) && block.getType() != Material.LAVA && block.getType() != Material.STATIONARY_LAVA) {
            int n = 0;
            for (BlockFace face : mainHorizontalFaces) {
                Block neighbour = block.getRelative(face);
                if (isLiquidSourceBlock(neighbour)) {
                    if (++n >= 2) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Get a special material data for the item with the data set to -1.  This
     * is useful when using the item in recipes, as a "don't care" data value.
     *
     * @param item the item to convert
     * @return a MaterialData for the item with wildcarded data
     */
    @SuppressWarnings("deprecation")
	public static MaterialData makeWildCardMaterialData(BaseSTBItem item) {
        MaterialData md = item.getMaterialData().clone();
        md.setData((byte) -1);
        return md;
    }

    /**
     * Get a special material data for the material with the data set to -1.
     * This is useful when using the item in recipes, as a "don't care" data
     * value.
     *
     * @param mat the base material
     * @return a MaterialData for the material with wildcarded data
     */
    @SuppressWarnings("deprecation")
	public static MaterialData makeWildCardMaterialData(Material mat) {
        return new MaterialData(mat, (byte) -1);
    }

    /**
     * Get a special item stack for the material with the data set to 32767.
     * This is useful when using the item in recipes, as a "don't care" data
     * value.
     *
     * @param mat the base material
     * @return an item stack for the material with wildcarded data
     */
    public static ItemStack makeWildCardItemStack(Material mat) {
        return new ItemStack(mat, 1, (short) 32767);
    }

    /**
     * Check if the given block can be used to fabricate items via vanilla
     * recipes.
     *
     * @param block the block to check
     * @return true if the block can be used to fabricate with, false otherwise
     */
    public static boolean canFabricateWith(Block block) {
        return block != null && block.getType() == Material.WORKBENCH;
    }

    /**
     * Check if the given item can be used to fabricate items via vanilla
     * recipes.
     *
     * @param stack the item stack to check
     * @return true if the item can be used to fabricate with, false otherwise
     */
    public static boolean canFabricateWith(ItemStack stack) {
        return stack != null && stack.getType() == Material.WORKBENCH;
    }

    /**
     * Send an audible alert to the given player indicating a problem of some
     * kind.
     *
     * @param player the player
     */
    public static void complain(Player player) {
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BASS, 1.0f, 1.0f);
    }

    /**
     * Send an error message to the given player, along with an audible alert.
     *
     * @param player the player
     * @param message the message text
     */
    public static void complain(Player player, String message) {
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BASS, 1.0f, 1.0f);
        MiscUtil.errorMessage(player, message);
    }

    public static void complain(HumanEntity player, String message) {
        if (player instanceof Player) {
            complain((Player) player, message);
        }
    }

    /**
     * Check if the given material is leaves.
     *
     * @param type the material to check
     * @return true if the material is any kind of tree leaf; false otherwise
     */
    public static boolean isLeaves(Material type) {
        return type == Material.LEAVES || type == Material.LEAVES_2;
    }

    /**
     * Give the items to a player, dropping any excess items on the ground.
     *
     * @param player the player
     * @param stacks one or more item stacks
     */
    public static void giveItems(HumanEntity player, ItemStack stacks) {
        HashMap<Integer,ItemStack> excess = player.getInventory().addItem(stacks);
        for (ItemStack stack : excess.values()) {
            player.getWorld().dropItemNaturally(player.getLocation(), stack);
        }
    }

    public static List<String> dumpItemStack(ItemStack stack) {
        if (stack == null) {
            return Collections.emptyList();
        }
        List<String> l = Lists.newArrayList();
        l.add("Quantity: " + stack.getAmount());
        l.add("Material/Data: " + stack.getType() + ":" + stack.getDurability());
        if (stack.hasItemMeta()) {
            ItemMeta meta = stack.getItemMeta();
            l.add("Display name: " + meta.getDisplayName());
            if (meta.hasLore()) {
                l.add("Lore: [" + Joiner.on(",").join(meta.getLore()) + "]");
            }
            if (meta.hasEnchants()) {
                for (Map.Entry<Enchantment, Integer> e : meta.getEnchants().entrySet()) {
                    l.add("Enchant " + e.getKey() + " = " + e.getValue());
                }
            }
        } else {
            l.add("No metadata");
        }
        if (stack.getType() != Material.AIR) {
            Attributes a = new Attributes(stack);
            l.add("Attribute count: " + a.size());
            for (Attributes.Attribute attr : a.values()) {
                l.add(String.format("* ID=%s name=[%s] amount=%f type=%s op=%s",
                        attr.getUUID().toString(), attr.getName(), attr.getAmount(),
                        attr.getAttributeType().toString(), attr.getOperation().toString()));
            }
        }
        return l;
    }

    /**
     * Given a string specification, try to get an ItemStack.
     * <p>
     * The spec. is of the form "material-name[:data-byte][,amount][,glow]" where
     * material-name is a valid Bukkit material name as understoood by
     * {@link Material#matchMaterial(String)}, data-byte is a numeric byte value,
     * amount is an optional item quantity, and "glow" if present indicates that
     * the item should glow if possible.
     * <p>
     * No item metadata is considered by this method.
     *
     * @param spec the specification
     * @return the return ItemStack
     * @throws DHUtilsException if the specification is invalid
     */
    public static ItemStack parseMaterialSpec(String spec) {
        if (spec == null || spec.isEmpty()) {
            return null;
        }

        String[] fields = spec.split(",");
        MaterialData mat = parseMatAndData(fields[0]);

        int amount = 1;
        boolean glowing = false;
        for (int i = 1; i < fields.length; i++) {
            if (StringUtils.isNumeric(fields[i])) {
                amount = Integer.parseInt(fields[i]);
            } else if (fields[i].equalsIgnoreCase("glow")) {
                glowing = true;
            }
        }
        ItemStack stack = mat.toItemStack(amount);
        if (glowing && SensibleToolboxPlugin.getInstance().isProtocolLibEnabled()) {
        	ItemGlow.setGlowing(stack, true);
        }

        return stack;
    }

    @SuppressWarnings("deprecation")
	private static MaterialData parseMatAndData(String matData) {
        String[] fields = matData.split("[:()]");
        Material mat = Material.matchMaterial(fields[0]);
        if (mat == null) {
            throw new DHUtilsException("Unknown material " + fields[0]);
        }
        MaterialData res = new MaterialData(mat);
        if (fields.length > 1) {
            if (StringUtils.isNumeric(fields[1])) {
                res.setData(Byte.parseByte(fields[1]));
            } else {
                switch (mat) {
                    case INK_SACK:
                        Dye dye = new Dye();
                        dye.setColor(DyeColor.valueOf(fields[1].toUpperCase()));
                        res = dye;
                        break;
                    case WOOL:
                    case CARPET:
                    case STAINED_GLASS:
                    case STAINED_GLASS_PANE:
                    case STAINED_CLAY:
                        // maybe one day these will all implement Colorable...
                        DyeColor dc2 = DyeColor.valueOf(fields[1].toUpperCase());
                        res.setData(dc2.getWoolData());
                        break;
                    case SAPLING:
                    case WOOD:
                        TreeSpecies ts = TreeSpecies.valueOf(fields[1].toUpperCase());
                        res.setData(ts.getData());
                        break;
				default:
					break;
                }
            }
        }
        return res;
    }

    /**
     * Get the relative rotations of two block faces; the number of clockwise
     * 90-degree rotations from the first face to the second.
     *
     * @param face1 the first face
     * @param face2 the second face
     * @return the number of 90-degree rotations between the faces
     */
    public static int getFaceRotation(BlockFace face1, BlockFace face2) {
        Validate.isTrue(face1.getModY() == 0 && Math.abs(face1.getModX() + face1.getModZ()) == 1, "invalid face " + face1);
        Validate.isTrue(face2.getModY() == 0 && Math.abs(face2.getModX() + face2.getModZ()) == 1, "invalid face " + face2);
        if (face1 == face2) {
            return 0;
        } else if (face1 == face2.getOppositeFace()) {
            return 2;
        } else {
            return face2 == BlockUtil.getLeft(face1) ? 3 : 1;
        }
    }

    /**
     * Given a BlockFace and a rotation in units of 90 degrees, return the
     * face obtained by a clockwise rotation.
     *
     * @param face the original face
     * @param rotation number of clockwise 90-degree rotations
     * @return the rotated face
     */
    public static BlockFace getRotatedFace(BlockFace face, int rotation) {
        Validate.isTrue(face.getModY() == 0 && Math.abs(face.getModX() + face.getModZ()) == 1, "invalid face " + face);
        switch (rotation) {
            case 0: return face;
            case 1: return BlockUtil.getLeft(face).getOppositeFace();
            case 2: return face.getOppositeFace();
            case 3: return BlockUtil.getLeft(face);
            default: throw new IllegalArgumentException("invalid rotation" + rotation);
        }
    }

    /**
     * Get the hardness for a given material.
     * //TODO: incomplete
     * @see <a href="http://minecraft.gamepedia.com/Digging#Blocks_by_Hardness">Minecraft Wiki page</a>
     *
     * @param mat the material to check
     * @return the base hardness of the block
     */
    public static double getMaterialHardness(Material mat) {
        switch (mat) {
            case BEDROCK: case ENDER_PORTAL_FRAME: case ENDER_PORTAL: case PORTAL: case COMMAND:
                return Double.MAX_VALUE;
            case OBSIDIAN:
                return 50;
            case ENDER_CHEST:
                return 22.5;
            case ANVIL: case COAL_BLOCK: case DIAMOND_BLOCK: case EMERALD_BLOCK: case IRON_BLOCK:
                case REDSTONE_BLOCK: case ENCHANTMENT_TABLE: case IRON_FENCE: case IRON_DOOR: case MOB_SPAWNER:
                return 5;
            case WEB:
                return 4;
            case DISPENSER: case DROPPER: case FURNACE:
                return 3.5;
            case BEACON: case GOLD_BLOCK: case COAL_ORE: case DRAGON_EGG: case DIAMOND_ORE: case EMERALD_ORE:
                case ENDER_STONE: case GOLD_ORE: case HOPPER:case IRON_ORE: case LAPIS_BLOCK: case QUARTZ_ORE:
                case REDSTONE_ORE: case TRAP_DOOR: case WOOD_DOOR:
                return 3;
            case CHEST: case TRAPPED_CHEST: case WORKBENCH:
                return 2.5;
            case BRICK_STAIRS: case BRICK: case CAULDRON: case COBBLESTONE: case COBBLESTONE_STAIRS: case COBBLE_WALL:
                case FENCE: case FENCE_GATE: case JUKEBOX: case MOSSY_COBBLESTONE:case NETHER_BRICK:
                case NETHER_FENCE: case NETHER_BRICK_STAIRS: case STEP: case LOG_2:case LOG: case WOOD:
                case WOOD_STEP: case WOOD_STAIRS:
                return 2;
            case BOOKSHELF: case STONE: case SMOOTH_BRICK: case SMOOTH_STAIRS:
                return 1.5;
            case HARD_CLAY: case STAINED_CLAY:
                return 1.25;
            case JACK_O_LANTERN: case MELON: case PUMPKIN: case SKULL: case SIGN_POST: case WALL_SIGN:
                return 1;
            case QUARTZ_BLOCK: case NOTE_BLOCK: case QUARTZ_STAIRS: case SANDSTONE: case SANDSTONE_STAIRS: case WOOL:
                return 0.8;
            case MONSTER_EGG:
                return 0.75;
            case RAILS:
                return 0.7;
            case CLAY: case SOIL: case GRASS: case GRAVEL: case MYCEL: case WATER_LILY: case SPONGE:
                return 0.6;
            case WOOD_BUTTON: case STONE_BUTTON: case CAKE_BLOCK:case DIRT: case HAY_BLOCK: case ICE:
            case LEVER: case PACKED_ICE: case PISTON_BASE: case SAND: case SOUL_SAND: case STONE_PLATE:
                case IRON_PLATE: case GOLD_PLATE: case WOOD_PLATE:
                return 0.5;
            case CACTUS: case LADDER: case NETHERRACK:
                return 0.4;
            case GLASS: case THIN_GLASS: case GLOWSTONE: case REDSTONE_LAMP_OFF: case REDSTONE_LAMP_ON:
                return 0.3;
            case BED: case COCOA: case DAYLIGHT_DETECTOR: case HUGE_MUSHROOM_1: case HUGE_MUSHROOM_2:
                case LEAVES: case LEAVES_2: case SNOW_BLOCK:
                return 0.2;
            case CARPET: case SNOW:
                return 0.1;
            default:
                return 0.0;
        }
    }

    /**
     * Check if the given block can be used as cable for the purposes of
     * transferring energy (SCU).
     *
     * @param block the block to check
     * @return true if the block can be used as a cable; false otherwise
     */
    public static boolean isCable(Block block) {
        return block.getType() == Material.IRON_FENCE;
    }

    /**
     * Encode the given level as a proportion of the given maximum as an item
     * durability; useful for displaying in the item's durability bar.
     *
     * @param stack the item stack
     * @param val the value to encode, between 0 and <em>max</em>
     * @param max the maximum value
     */
    public static void levelToDurability(ItemStack stack, int val, int max) {
        short maxDur = stack.getType().getMaxDurability();
        Validate.isTrue(maxDur > 0, "Item stack " + stack + " does not have a durability bar!");
        Validate.isTrue(val >= 0 && val <= max, "Value " + val + " out of range 0.." + max);
        float d = val / (float) max;
        short dur = (short) (maxDur * d);
        stack.setDurability((short) (Math.max(1, maxDur - dur)));
    }

    /**
     * Check if the given world is a creative world.  This requires an
     * external plugin to check; currently, only Multiverse is supported.
     *
     * @param world the world to check
     * @return true if the world is a creative world
     */
    public static boolean isCreativeWorld(World world) {
        if (SensibleToolbox.getPluginInstance().getMultiverseCore() != null) {
            MultiverseCore mvc = SensibleToolbox.getPluginInstance().getMultiverseCore();
            MultiverseWorld mvw = mvc.getMVWorldManager().getMVWorld(world);
            return mvw.getGameMode() == GameMode.CREATIVE;
        } else {
            return false;
        }
    }

    /**
     * Get the data byte for various items which have a direction: ladders,
     * wall signs, chests, furnaces, droppers, hoppers, dispensers, banners.
     *
     * @param face the direction the block is facing
     * @return the data byte to use for that direction
     */
    public static byte getDirectionData(BlockFace face) {
        switch (face) {
            case SELF: case DOWN: return 0;
            case UP: return 1;
            case NORTH: return 2;
            case SOUTH: return 3;
            case WEST: return 4;
            case EAST: return 5;
            default: throw new IllegalArgumentException("invalid direction " + face);
        }
    }
}
