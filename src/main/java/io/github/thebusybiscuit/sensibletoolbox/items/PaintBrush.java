package io.github.thebusybiscuit.sensibletoolbox.items;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang.Validate;
import org.bukkit.Art;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
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

import io.github.thebusybiscuit.sensibletoolbox.api.SensibleToolbox;
import io.github.thebusybiscuit.sensibletoolbox.api.items.BaseSTBBlock;
import io.github.thebusybiscuit.sensibletoolbox.api.items.BaseSTBItem;
import io.github.thebusybiscuit.sensibletoolbox.blocks.PaintCan;
import io.github.thebusybiscuit.sensibletoolbox.utils.HoloMessage;
import io.github.thebusybiscuit.sensibletoolbox.utils.STBUtil;
import io.github.thebusybiscuit.sensibletoolbox.utils.UnicodeSymbol;
import me.desht.dhutils.Debugger;
import me.desht.dhutils.IconMenu;
import me.desht.dhutils.MiscUtil;

public class PaintBrush extends BaseSTBItem implements IconMenu.OptionClickEventHandler {

    private int paintLevel;
    private DyeColor color;
    private Painting editingPainting = null;

    public PaintBrush() {
        super();
        color = DyeColor.WHITE;
        paintLevel = 0;
    }

    public PaintBrush(ConfigurationSection conf) {
        super(conf);
        setPaintLevel(conf.getInt("paintLevel"));
        setColor(DyeColor.valueOf(conf.getString("color")));
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

    @Nonnull
    public DyeColor getColor() {
        return color;
    }

    public void setColor(@Nonnull DyeColor color) {
        this.color = color;
    }

    @Override
    public boolean isEnchantable() {
        return false;
    }

    @Override
    public YamlConfiguration freeze() {
        YamlConfiguration res = super.freeze();
        res.set("paintLevel", paintLevel);
        res.set("color", color == null ? "" : color.toString());
        return res;
    }

    @Override
    public Material getMaterial() {
        return Material.GOLDEN_SHOVEL;
    }

    @Override
    public String getItemName() {
        return "Paintbrush";
    }

    @Override
    public String[] getLore() {
        return new String[] { "Paints colorable blocks:", " Wool, carpet, stained clay/glass", "R-click block: paint up to " + getMaxBlocksAffected() + " blocks", UnicodeSymbol.ARROW_UP.toUnicode() + " + R-click block: paint block", UnicodeSymbol.ARROW_UP.toUnicode() + " + R-click air: empty brush", };
    }

    @Override
    public ItemStack toItemStack(int amount) {
        ItemStack stack = super.toItemStack(amount);
        STBUtil.levelToDurability(stack, getPaintLevel(), getMaxPaintLevel());
        return stack;
    }

    @Override
    public String getDisplaySuffix() {
        return getPaintLevel() > 0 ? getPaintLevel() + " " + STBUtil.dyeColorToChatColor(getColor()) + getColor() : null;
    }

    @Override
    public Recipe getRecipe() {
        ShapedRecipe recipe = new ShapedRecipe(getKey(), toItemStack());
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
            }
            else if (okToColor(b, stb)) {
                int painted;
                // Bukkit Colorable interface doesn't cover all colorable blocks at this time, only Wool
                if (player.isSneaking()) {
                    // paint a single block
                    painted = paintBlocks(player, b);
                }
                else {
                    // paint multiple blocks around the clicked block
                    Block[] blocks = findBlocksAround(b);
                    painted = paintBlocks(player, blocks);
                }

                if (painted > 0) {
                    player.playSound(player.getLocation(), Sound.BLOCK_WATER_AMBIENT, 1.0F, 1.5F);
                }
            }
        }
        else if (event.getAction() == Action.RIGHT_CLICK_AIR && player.isSneaking()) {
            setPaintLevel(0);
        }

        updateHeldItemStack(event.getPlayer(), event.getHand());
        event.setCancelled(true);
    }

    private boolean okToColor(Block b, BaseSTBBlock stb) {
        if (stb != null && !(stb instanceof Colorable)) {
            // we don't want blocks which happen to use a Colorable material to be paintable
            return false;
        }

        if (getBlockColor(b) == getColor() || getPaintLevel() <= 0) {
            return false;
        }

        return STBUtil.isColorable(b.getType()) || b.getType() == Material.GLASS || b.getType() == Material.GLASS_PANE;
    }

    private void refillFromCan(@Nonnull PaintCan can) {
        int needed;
        if (this.getColor() == can.getColor()) {
            needed = this.getMaxPaintLevel() - this.getPaintLevel();
        }
        else {
            this.setPaintLevel(0);
            needed = this.getMaxPaintLevel();
        }
        int actual = Math.min(needed, can.getPaintLevel());
        Debugger.getInstance().debug(can + " has " + can.getPaintLevel() + " of " + can.getColor() + "; " + "try to fill brush with " + needed + ", actual = " + actual);
        if (actual > 0) {
            this.setColor(can.getColor());
            this.setPaintLevel(this.getPaintLevel() + actual);
            can.setPaintLevel(can.getPaintLevel() - actual);
            Debugger.getInstance().debug("brush now = " + this.getPaintLevel() + " " + this.getColor() + ", can now = " + can.getPaintLevel() + " " + can.getColor());
        }
    }

    @Override
    public void onInteractEntity(PlayerInteractEntityEvent event) {
        event.setCancelled(true);
        if (getPaintLevel() <= 0) return;
        Entity e = event.getRightClicked();
        int paintUsed = 0;
        if (e instanceof Colorable) {
            ((Colorable) e).setColor(getColor());
            paintUsed = 1;
        }
        else if (e instanceof Painting) {
            Art a = ((Painting) e).getArt();
            if (getPaintLevel() >= a.getBlockHeight() * a.getBlockWidth()) {
                IconMenu menu = buildMenu((Painting) e);
                menu.open(event.getPlayer());
            }
            else {
                Location loc = e.getLocation().add(0, -a.getBlockHeight() / 2.0, 0);
                HoloMessage.popup(event.getPlayer(), loc, ChatColor.RED + "Not enough paint!");
            }
        }
        else if (e instanceof Wolf) {
            Wolf wolf = (Wolf) e;
            wolf.setCollarColor(getColor());
            paintUsed = 1;
        }

        if (paintUsed > 0) {
            setPaintLevel(getPaintLevel() - paintUsed);
            updateHeldItemStack(event.getPlayer(), event.getHand());
            event.getPlayer().playSound(e.getLocation(), Sound.BLOCK_WATER_AMBIENT, 1.0F, 1.5F);
        }
    }

    private Block[] findBlocksAround(Block b) {
        Set<Block> blocks = new HashSet<>();
        find(b, b.getType(), blocks, getMaxBlocksAffected());
        return blocks.toArray(new Block[0]);
    }

    private void find(Block b, Material mat, Set<Block> blocks, int max) {
        if (b.getType() != mat || getBlockColor(b) == getColor() || blocks.contains(b) || blocks.size() > max) {
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

    @Nullable
    private DyeColor getBlockColor(@Nonnull Block b) {
        if (STBUtil.isColorable(b.getType())) {
            return DyeColor.getByColor(getColor().getColor());
        }
        else {
            return null;
        }
    }

    private int paintBlocks(@Nonnull Player player, Block... blocks) {
        int painted = 0;

        // TODO: Get Paint Brush working again
        // for (Block b : blocks) {
        // if (!SensibleToolbox.getProtectionManager().hasPermission(player, b, ProtectableAction.PLACE_BLOCK)) {
        // continue;
        // }
        //
        // Debugger.getInstance().debug(2, "painting! " + b + " " + getPaintLevel() + " " + getColor());
        // BaseSTBBlock stb = SensibleToolbox.getBlockAt(b.getLocation());
        //
        // if (stb instanceof Colorable) {
        // ((Colorable) stb).setColor(getColor());
        // }
        // else {
        // if (b.getType() == Material.GLASS) {
        // b.setType(Material.STAINED_GLASS);
        // }
        // else if (b.getType() == Material.GLASS_PANE) {
        // b.setType(Material.STAINED_GLASS_PANE);
        // }
        // }
        //
        // painted++;
        // setPaintLevel(getPaintLevel() - 1);
        //
        // if (getPaintLevel() <= 0) {
        // break;
        // }
        // }
        return painted;
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
            event.getPlayer().playSound(editingPainting.getLocation(), Sound.BLOCK_WATER_AMBIENT, 1.0F, 1.5F);
        }
        catch (IllegalArgumentException e) {
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
            menu.setOption(pos, new ItemStack(Material.PAINTING), a.name(), "");
            pos++;
        }

        return menu;
    }

    private static Art[] getOtherArt(Art art) {
        List<Art> l = new ArrayList<>();

        for (Art a : Art.values()) {
            if (a.getBlockWidth() == art.getBlockWidth() && a.getBlockHeight() == art.getBlockHeight() && a != art) {
                l.add(a);
            }
        }

        return l.toArray(new Art[0]);
    }
}
