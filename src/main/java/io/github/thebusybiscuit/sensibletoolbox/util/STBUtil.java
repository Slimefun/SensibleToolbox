package io.github.thebusybiscuit.sensibletoolbox.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.metadata.Metadatable;

import com.google.common.base.Joiner;

import io.github.thebusybiscuit.cscorelib2.inventory.ItemUtils;
import io.github.thebusybiscuit.sensibletoolbox.SensibleToolboxPlugin;
import io.github.thebusybiscuit.sensibletoolbox.api.energy.Chargeable;
import io.github.thebusybiscuit.sensibletoolbox.api.items.BaseSTBItem;
import me.desht.dhutils.DHUtilsException;
import me.desht.dhutils.ItemGlow;
import me.desht.dhutils.MiscUtil;
import me.desht.dhutils.block.BlockUtil;

/**
 * A collection of miscellaneous utility methods.
 */
public final class STBUtil {

    private STBUtil() {}

    /**
     * The block faces directly adjacent to a block.
     */
    public static final BlockFace[] directFaces = { BlockFace.UP, BlockFace.NORTH, BlockFace.EAST, BlockFace.DOWN, BlockFace.SOUTH, BlockFace.WEST };
    /**
     * The block faces in the four cardinal horizontal directions.
     */
    public static final BlockFace[] mainHorizontalFaces = new BlockFace[] { BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST, BlockFace.NORTH };
    /**
     * The block faces in all eight compass directions, plus the block itself.
     */
    public static final BlockFace[] allHorizontalFaces = new BlockFace[] { BlockFace.SELF, BlockFace.NORTH_EAST, BlockFace.EAST, BlockFace.SOUTH_EAST, BlockFace.SOUTH, BlockFace.SOUTH_WEST, BlockFace.WEST, BlockFace.NORTH_WEST, BlockFace.NORTH };

    /**
     * Check if the given material is a crop which can grow and/or be harvested.
     *
     * @param m
     *            the material to check
     * @return true if the material is a crop
     */
    public static boolean isCrop(@Nonnull Material m) {
        return m == Material.WHEAT || m == Material.CARROTS || m == Material.POTATOES || m == Material.PUMPKIN_STEM || m == Material.MELON_STEM || m == Material.BEETROOTS || m == Material.SWEET_BERRY_BUSH;
    }

    /**
     * Check if the given material is any growing plant (not including leaves or trees)
     *
     * @param type
     *            the material to check
     * @return true if the material is a plant
     */
    public static boolean isPlant(@Nonnull Material type) {
        if (isCrop(type)) {
            return true;
        }

        if (Tag.SAPLINGS.isTagged(type)) {
            return true;
        }

        if (Tag.SMALL_FLOWERS.isTagged(type)) {
            return true;
        }

        if (Tag.TALL_FLOWERS.isTagged(type)) {
            return true;
        }

        switch (type) {
        case TALL_GRASS:
        case SUGAR_CANE:
        case BROWN_MUSHROOM:
        case RED_MUSHROOM:
        case DEAD_BUSH:
        case SWEET_BERRY_BUSH:
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
     * @param type
     *            the material to check
     * @return true if the material is wearable
     */
    public static boolean isWearable(@Nonnull Material type) {
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
     * @param b
     *            the centre block
     * @return array of all blocks around the given block, including the given block as the first element
     */
    public static Block[] getSurroundingBlocks(Block b) {
        Block[] result = new Block[allHorizontalFaces.length];
        for (int i = 0; i < allHorizontalFaces.length; i++) {
            result[i] = b.getRelative(allHorizontalFaces[i]);
        }
        return result;
    }

    @Nullable
    public static Material getCropType(@Nonnull Material seedType) {
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
        case BEETROOT_SEEDS:
            return Material.BEETROOTS;
        default:
            return null;
        }
    }

    /**
     * Convenience wrapper to get the metadata value set by STB.
     *
     * @param m
     *            the metadatable object
     * @param key
     *            the metadata key
     * @return the metadata value, or null if there is none
     */
    @Nullable
    public static Object getMetadataValue(@Nonnull Metadatable m, @Nonnull String key) {
        for (MetadataValue mv : m.getMetadata(key)) {
            if (mv.getOwningPlugin() == SensibleToolboxPlugin.getInstance()) {
                return mv.value();
            }
        }

        return null;
    }

    /**
     * Convert a dye color to the nearest matching chat color.
     *
     * @param dyeColor
     *            the dye color
     * @return the nearest matching chat color
     */
    @Nonnull
    public static ChatColor dyeColorToChatColor(@Nonnull DyeColor dyeColor) {
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
            return ChatColor.DARK_GRAY;
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
        case LIGHT_GRAY:
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
     * Check if the given material is colorable. Ideally the relevant MaterialData
     * should implement the Colorable interface, but Bukkit doesn't have that yet...
     *
     * @param mat
     *            the material to check
     * @return true if the material is colorable; false otherwise
     */
    public static boolean isColorable(Material mat) {
        if (Tag.WOOL.isTagged(mat)) {
            return true;
        }

        if (Tag.CARPETS.isTagged(mat)) {
            return true;
        }

        switch (mat) {
        // TODO: Fix glass coloring
        // case STAINED_GLASS:
        // case STAINED_GLASS_PANE:
        // case STAINED_CLAY:
        // return true;
        default:
            return false;
        }
    }

    /**
     * Given a yaw value, get the closest block face for the given yaw. The yaw value
     * is usually obtained via {@link org.bukkit.Location#getYaw()}
     *
     * @param yaw
     *            the yaw to convert
     * @return the nearest block face
     */
    @Nonnull
    public static BlockFace getFaceFromYaw(float yaw) {
        double rot = yaw % 360;

        if (rot < 0) {
            rot += 360;
        }

        if ((0 <= rot && rot < 45) || (315 <= rot && rot < 360.0)) {
            return BlockFace.SOUTH;
        }
        else if (45 <= rot && rot < 135) {
            return BlockFace.WEST;
        }
        else if (135 <= rot && rot < 225) {
            return BlockFace.NORTH;
        }
        else if (225 <= rot && rot < 315) {
            return BlockFace.EAST;
        }
        else {
            throw new IllegalArgumentException("impossible rotation: " + rot);
        }
    }

    /**
     * Check if the given block is exposed to the world on the given face.
     * {@link org.bukkit.Material#isOccluding()} is used to check if a face is exposed.
     *
     * @param block
     *            the block to check
     * @param face
     *            the face to check
     * @return true if the given block face is exposed
     */
    public static boolean isExposed(@Nonnull Block block, @Nonnull BlockFace face) {
        return !block.getRelative(face).getType().isOccluding();
    }

    /**
     * Check if the given block is exposed on <i>any</i> face.
     * {@link org.bukkit.Material#isOccluding()} is used to check if a face is exposed.
     *
     * @param block
     *            the block to check
     * @return true if any face of the block is exposed
     */
    public static boolean isExposed(@Nonnull Block block) {
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
     * @param ch
     *            the chargeable item or block
     * @return a formatted string showing the chargeable's charge
     */
    @Nonnull
    public static String getChargeString(@Nonnull Chargeable ch) {
        double d = ch.getCharge() / ch.getMaxCharge();
        ChatColor cc;

        if (d < 0.333) {
            cc = ChatColor.RED;
        }
        else if (d < 0.666) {
            cc = ChatColor.GOLD;
        }
        else {
            cc = ChatColor.GREEN;
        }

        return ChatColor.WHITE + "\u2301 " + cc + Math.round(ch.getCharge()) + "/" + ch.getMaxCharge() + " SCU";
    }

    /**
     * Round up the given value to given nearest multiple.
     *
     * @param n
     *            the value to round up
     * @param nearestMultiple
     *            the nearest multiple to round to
     * @return the rounded value
     */
    public static int roundUp(int n, int nearestMultiple) {
        return n + nearestMultiple - 1 - (n - 1) % nearestMultiple;
    }

    /**
     * Get a display-formatted string for the given ItemStack. The item stack's metadata is
     * taken into account, as it the size of the stack.
     *
     * @param stack
     *            the item stack
     * @return a formatted description of the item stack
     */
    @Nonnull
    public static String describeItemStack(@Nullable ItemStack stack) {
        if (stack == null) {
            return "nothing";
        }

        return stack.getAmount() + " x " + ItemUtils.getItemName(stack);
    }

    /**
     * Convenience method to create an item stack with defined title and lore data
     *
     * @param material
     *            the material to create the stack from
     * @param title
     *            the item's title, may be null
     * @param lore
     *            the item's lore, may be empty
     * @return a new ItemStack with the given title and lore
     */
    public static ItemStack makeStack(Material material, String title, String... lore) {
        ItemStack stack = new ItemStack(material);

        if (title != null) {
            ItemMeta meta = stack.getItemMeta();
            meta.setDisplayName(title);
            List<String> newLore = new ArrayList<>(lore.length);

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
     * @param block
     *            the block to check
     * @return true if the block is a water source block
     */
    public static boolean isLiquidSourceBlock(@Nonnull Block block) {
        return block.isLiquid() && block.getData() == 0;
    }

    /**
     * Check if the given block is an infinite water source. It must be a non-flowing
     * source water block with at least 2 non-flowing source water block neighbours.
     *
     * @param block
     *            the block to check
     * 
     * @return true if the block is an infinite water source
     */
    public static boolean isInfiniteWaterSource(@Nonnull Block block) {
        if (isLiquidSourceBlock(block) && block.getType() != Material.LAVA) {
            int n = 0;

            for (BlockFace face : mainHorizontalFaces) {
                Block neighbour = block.getRelative(face);

                if (isLiquidSourceBlock(neighbour)) {
                    n++;

                    if (n >= 2) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Check if the given block can be used to fabricate items via vanilla
     * recipes.
     *
     * @param block
     *            the block to check
     * @return true if the block can be used to fabricate with, false otherwise
     */
    public static boolean canFabricateWith(@Nullable Block block) {
        return block != null && block.getType() == Material.CRAFTING_TABLE;
    }

    /**
     * Check if the given item can be used to fabricate items via vanilla
     * recipes.
     *
     * @param stack
     *            the item stack to check
     * @return true if the item can be used to fabricate with, false otherwise
     */
    public static boolean canFabricateWith(@Nullable ItemStack stack) {
        return stack != null && stack.getType() == Material.CRAFTING_TABLE;
    }

    /**
     * Send an audible alert to the given player indicating a problem of some
     * kind.
     *
     * @param player
     *            the player
     */
    public static void complain(@Nonnull Player player) {
        Validate.notNull(player, "Cannot complain to nobody");
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0F, 1.0F);
    }

    /**
     * Send an error message to the given player, along with an audible alert.
     *
     * @param player
     *            the player
     * @param message
     *            the message text
     */
    public static void complain(Player player, String message) {
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0F, 1.0F);
        MiscUtil.errorMessage(player, message);
    }

    public static void complain(HumanEntity player, String message) {
        if (player instanceof Player) {
            complain((Player) player, message);
        }
    }

    /**
     * Give the items to a player, dropping any excess items on the ground.
     *
     * @param player
     *            the player
     * @param stacks
     *            one or more item stacks
     */
    public static void giveItems(HumanEntity player, ItemStack stacks) {
        Map<Integer, ItemStack> excess = player.getInventory().addItem(stacks);

        for (ItemStack stack : excess.values()) {
            player.getWorld().dropItemNaturally(player.getLocation(), stack);
        }
    }

    @Nonnull
    public static List<String> dumpItemStack(@Nullable ItemStack stack) {
        if (stack == null) {
            return Collections.emptyList();
        }

        List<String> l = new ArrayList<>();
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
        }
        else {
            l.add("No metadata");
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
     * @param spec
     *            the specification
     * @return the return ItemStack
     * @throws DHUtilsException
     *             if the specification is invalid
     */
    public static ItemStack parseMaterialSpec(String spec) {
        if (spec == null || spec.isEmpty()) {
            return null;
        }

        String[] fields = spec.split(",");
        Material material = Material.matchMaterial(fields[0]);

        int amount = 1;
        boolean glowing = false;

        for (int i = 1; i < fields.length; i++) {
            if (StringUtils.isNumeric(fields[i])) {
                amount = Integer.parseInt(fields[i]);
            }
            else if (fields[i].equalsIgnoreCase("glow")) {
                glowing = true;
            }
        }

        ItemStack stack = new ItemStack(material, amount);

        if (glowing && SensibleToolboxPlugin.getInstance().isProtocolLibEnabled()) {
            ItemGlow.setGlowing(stack, true);
        }

        return stack;
    }

    /**
     * Get the relative rotations of two block faces; the number of clockwise
     * 90-degree rotations from the first face to the second.
     *
     * @param face1
     *            the first face
     * @param face2
     *            the second face
     * @return the number of 90-degree rotations between the faces
     */
    public static int getFaceRotation(BlockFace face1, BlockFace face2) {
        Validate.isTrue(face1.getModY() == 0 && Math.abs(face1.getModX() + face1.getModZ()) == 1, "invalid face " + face1);
        Validate.isTrue(face2.getModY() == 0 && Math.abs(face2.getModX() + face2.getModZ()) == 1, "invalid face " + face2);

        if (face1 == face2) {
            return 0;
        }
        else if (face1 == face2.getOppositeFace()) {
            return 2;
        }
        else {
            return face2 == BlockUtil.getLeft(face1) ? 3 : 1;
        }
    }

    /**
     * Given a BlockFace and a rotation in units of 90 degrees, return the
     * face obtained by a clockwise rotation.
     *
     * @param face
     *            the original face
     * @param rotation
     *            number of clockwise 90-degree rotations
     * @return the rotated face
     */
    public static BlockFace getRotatedFace(BlockFace face, int rotation) {
        Validate.isTrue(face.getModY() == 0 && Math.abs(face.getModX() + face.getModZ()) == 1, "invalid face " + face);

        switch (rotation) {
        case 0:
            return face;
        case 1:
            return BlockUtil.getLeft(face).getOppositeFace();
        case 2:
            return face.getOppositeFace();
        case 3:
            return BlockUtil.getLeft(face);
        default:
            throw new IllegalArgumentException("invalid rotation" + rotation);
        }
    }

    /**
     * Check if the given block can be used as cable for the purposes of
     * transferring energy (SCU).
     *
     * @param block
     *            the block to check
     * @return true if the block can be used as a cable; false otherwise
     */
    public static boolean isCable(Block block) {
        return block.getType() == Material.IRON_BARS;
    }

    /**
     * Encode the given level as a proportion of the given maximum as an item
     * durability; useful for displaying in the item's durability bar.
     *
     * @param stack
     *            the item stack
     * @param val
     *            the value to encode, between 0 and <em>max</em>
     * @param max
     *            the maximum value
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
     * Get the data byte for various items which have a direction: ladders,
     * wall signs, chests, furnaces, droppers, hoppers, dispensers, banners.
     *
     * @param face
     *            the direction the block is facing
     * @return the data byte to use for that direction
     */
    public static byte getDirectionData(BlockFace face) {
        switch (face) {
        case SELF:
        case DOWN:
            return 0;
        case UP:
            return 1;
        case NORTH:
            return 2;
        case SOUTH:
            return 3;
        case WEST:
            return 4;
        case EAST:
            return 5;
        default:
            throw new IllegalArgumentException("invalid direction " + face);
        }
    }

    public static boolean isPotionIngredient(Material type) {
        // TODO Fix Potion Ingredient lookup
        return false;
    }
}
