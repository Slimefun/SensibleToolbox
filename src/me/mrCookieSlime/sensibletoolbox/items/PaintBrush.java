package me.mrCookieSlime.sensibletoolbox.items;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import me.desht.sensibletoolbox.dhutils.Debugger;
import me.desht.sensibletoolbox.dhutils.IconMenu;
import me.desht.sensibletoolbox.dhutils.MiscUtil;
import me.mrCookieSlime.sensibletoolbox.api.SensibleToolbox;
import me.mrCookieSlime.sensibletoolbox.api.items.BaseSTBBlock;
import me.mrCookieSlime.sensibletoolbox.api.items.BaseSTBItem;
import me.mrCookieSlime.sensibletoolbox.api.util.BlockProtection;
import me.mrCookieSlime.sensibletoolbox.api.util.PopupMessage;
import me.mrCookieSlime.sensibletoolbox.api.util.STBUtil;
import me.mrCookieSlime.sensibletoolbox.blocks.PaintCan;

import org.apache.commons.lang.Validate;
import org.bukkit.Art;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.TreeSpecies;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Painting;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.material.Colorable;
import org.bukkit.material.MaterialData;

public class PaintBrush extends BaseSTBItem implements IconMenu.OptionClickEventHandler {
    private static final MaterialData md = new MaterialData(Material.GOLD_SPADE);
    private int paintLevel;
    private DyeColor colour;
    private Painting editingPainting = null;

    public PaintBrush() {
        super();
        colour = DyeColor.WHITE;
        paintLevel = 0;
    }

    public PaintBrush(ConfigurationSection conf) {
        super(conf);
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

    public boolean allowWoodStaining() {
        return getItemConfig() != null && getItemConfig().getBoolean("wood_staining");
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
        return allowWoodStaining() ?
                new String[] {
                        "Paints colourable blocks:",
                        " - wool, carpet, stained clay/glass",
                        "And planks/wood slabs:",
                        " - grey/white/brown/pink/orange/black",
                        "R-click block: paint up to " + getMaxBlocksAffected() + " blocks",
                        "⇧ + R-click block: paint block",
                        "⇧ + R-click air: empty brush",
                } :
                new String[] {
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
        STBUtil.levelToDurability(stack, getPaintLevel(), getMaxPaintLevel());
        return stack;
    }

    @Override
    public String getDisplaySuffix() {
        return getPaintLevel() > 0 ? getPaintLevel() + " " + STBUtil.dyeColorToChatColor(getColour()) + getColour() : null;
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
            BaseSTBBlock stb = SensibleToolbox.getBlockAt(b.getLocation(), true);
            if (stb instanceof PaintCan) {
                refillFromCan((PaintCan) stb);
            } else if (okToColor(b, stb)) {
                int painted;
                // Bukkit Colorable interface doesn't cover all colorable blocks at this time, only Wool
                if (player.isSneaking()) {
                    // paint a single block
                    painted = paintBlocks(player, b);
                } else {
                    // paint multiple blocks around the clicked block
                    Block[] blocks = findBlocksAround(b);
                    painted = paintBlocks(player, blocks);
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

    private boolean okToColor(Block b, BaseSTBBlock stb) {
        if (stb != null && !(stb instanceof Colorable)) {
            // we don't want blocks which happen to use a Colorable material to be paintable
            return false;
        }
        if (getBlockColour(b) == getColour() || getPaintLevel() <= 0) {
            return false;
        }
        return STBUtil.isColorable(b.getType())
                || b.getType() == Material.GLASS
                || b.getType() == Material.THIN_GLASS
                || allowWoodStaining() && isStainableWood(b.getType()) && okWoodStain(getColour());
    }

    private boolean isStainableWood(Material mat) {
        switch (mat) {
            case WOOD: case WOOD_STEP: case WOOD_DOUBLE_STEP:
                return true;
            default:
                return false;
        }
    }

    private boolean okWoodStain(DyeColor colour) {
        switch (colour) {
            case BLACK: case WHITE: case GRAY:
            case PINK: case ORANGE: case BROWN:
                return true;
            default:
                return false;
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
        Debugger.getInstance().debug(can + " has " + can.getPaintLevel() + " of " + can.getColour() +
                "; try to fill brush with " + needed + ", actual = " + actual);
        if (actual > 0) {
            this.setColour(can.getColour());
            this.setPaintLevel(this.getPaintLevel() + actual);
            can.setPaintLevel(can.getPaintLevel() - actual);
            Debugger.getInstance().debug("brush now = " + this.getPaintLevel() + " " + this.getColour() +
                    ", can now = " + can.getPaintLevel() + " " + can.getColour());
        }
    }

    @Override
    public void onInteractEntity(PlayerInteractEntityEvent event) {
        event.setCancelled(true);
        if (getPaintLevel() <= 0) {
            return;
        }
        Entity e = event.getRightClicked();
        int paintUsed = 0;
        if (e instanceof Colorable) {
            ((Colorable) e).setColor(getColour());
            paintUsed = 1;
        } else if (e instanceof Painting) {
            Art a = ((Painting) e).getArt();
            if (getPaintLevel() >= a.getBlockHeight() * a.getBlockWidth()) {
                IconMenu menu = buildMenu((Painting) e);
                menu.open(event.getPlayer());
            } else {
                Location loc = e.getLocation().add(0, -a.getBlockHeight() / 2.0, 0);
                PopupMessage.quickMessage(event.getPlayer(), loc, ChatColor.RED + "Not enough paint!");
            }
        } else if (e instanceof Wolf) {
            Wolf wolf = (Wolf) e;
            wolf.setCollarColor(getColour());
            paintUsed = 1;
        }

        if (paintUsed > 0) {
            setPaintLevel(getPaintLevel() - paintUsed);
            event.getPlayer().setItemInHand(toItemStack());
            event.getPlayer().playSound(e.getLocation(), Sound.WATER, 1.0f, 1.5f);
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
        find(b.getRelative(BlockFace.EAST), mat, blocks, max);
        find(b.getRelative(BlockFace.NORTH), mat, blocks, max);
        find(b.getRelative(BlockFace.SOUTH), mat, blocks, max);
        find(b.getRelative(BlockFace.WEST), mat, blocks, max);
        find(b.getRelative(BlockFace.DOWN), mat, blocks, max);
    }

    @SuppressWarnings("deprecation")
	private DyeColor getBlockColour(Block b) {
        if (STBUtil.isColorable(b.getType())) {
            return DyeColor.getByWoolData(b.getData());
        } else if (isStainableWood(b.getType())) {
            switch (b.getData() & 0x07) {
                case 0: return DyeColor.GRAY; // Oak
                case 1: return DyeColor.BROWN; // Spruce
                case 2: return DyeColor.WHITE; // Birch
                case 3: return DyeColor.PINK; // Jungle
                case 4: return DyeColor.ORANGE; // Acacia
                case 5: return DyeColor.BLACK; // Dark Oak
            }
        } else {
            return null;
        }
        return STBUtil.isColorable(b.getType()) ? DyeColor.getByWoolData(b.getData()) : null;
    }

    @SuppressWarnings("deprecation")
	private int paintBlocks(Player player, Block... blocks) {
        int painted = 0;
        for (Block b : blocks) {
            if (!SensibleToolbox.getBlockProtection().playerCanBuild(player, b, BlockProtection.Operation.PLACE)) {
                continue;
            }
            Debugger.getInstance().debug(2, "painting! " + b + "  " + getPaintLevel() + " " + getColour());
            BaseSTBBlock stb = SensibleToolbox.getBlockAt(b.getLocation());
            if (stb != null && stb instanceof Colorable) {
                ((Colorable) stb).setColor(getColour());
            } else if (isStainableWood(b.getType())) {
                int data = b.getData() & 0xf8;
                data |= getWoodType(getColour()).getData();
                b.setData((byte) data);
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

    private TreeSpecies getWoodType(DyeColor colour) {
        switch (colour) {
            case GRAY: return TreeSpecies.GENERIC; // oak
            case BROWN: return TreeSpecies.REDWOOD; // spruce
            case WHITE: return TreeSpecies.BIRCH;
            case PINK: return TreeSpecies.JUNGLE;
            case ORANGE: return TreeSpecies.ACACIA;
            case BLACK: return TreeSpecies.DARK_OAK;
            default: return TreeSpecies.GENERIC;
        }
    }

    @Override
    public void onOptionClick(IconMenu.OptionClickEvent event) {
        Validate.notNull(editingPainting, "Editing painting should be non-null here!");
        String artName = event.getName();
        try {
            Art art = Art.valueOf(artName);
            editingPainting.setArt(art);
            setPaintLevel(getPaintLevel() - art.getBlockWidth() * art.getBlockHeight());
            event.getPlayer().setItemInHand(toItemStack());
            event.getPlayer().playSound(editingPainting.getLocation(), Sound.WATER, 1.0f, 1.5f);
        } catch (IllegalArgumentException e) {
            MiscUtil.errorMessage(event.getPlayer(), "Invalid artwork: " + artName);
        }
        event.setWillClose(true);
        event.setWillDestroy(true);
        editingPainting = null;
    }

    private IconMenu buildMenu(Painting painting) {
        editingPainting = painting;
        Art[] other = getOtherArt(painting.getArt());
        IconMenu menu = new IconMenu("Select Artwork", STBUtil.roundUp(other.length, 9), this, getProviderPlugin());
        int pos = 0;
        for (Art a : other) {
            menu.setOption(pos++, new ItemStack(Material.PAINTING), a.name(), "");
        }
        return menu;
    }

    private static Art[] getOtherArt(Art art) {
        List<Art> l = new ArrayList<Art>();
        for (Art a : Art.values()) {
            if (a.getBlockWidth() == art.getBlockWidth() && a.getBlockHeight() == art.getBlockHeight() && a != art) {
                l.add(a);
            }
        }
        return l.toArray(new Art[l.size()]);
    }
}
