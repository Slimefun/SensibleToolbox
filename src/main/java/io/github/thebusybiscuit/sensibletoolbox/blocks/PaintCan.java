package io.github.thebusybiscuit.sensibletoolbox.blocks;

import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.base.Strings;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.Tag;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.RecipeChoice.MaterialChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import io.github.thebusybiscuit.sensibletoolbox.api.SensibleToolbox;
import io.github.thebusybiscuit.sensibletoolbox.api.gui.GUIUtil;
import io.github.thebusybiscuit.sensibletoolbox.api.gui.InventoryGUI;
import io.github.thebusybiscuit.sensibletoolbox.api.gui.SlotType;
import io.github.thebusybiscuit.sensibletoolbox.api.gui.gadgets.AccessControlGadget;
import io.github.thebusybiscuit.sensibletoolbox.api.gui.gadgets.ButtonGadget;
import io.github.thebusybiscuit.sensibletoolbox.api.gui.gadgets.LevelMonitor;
import io.github.thebusybiscuit.sensibletoolbox.api.gui.gadgets.LevelReporter;
import io.github.thebusybiscuit.sensibletoolbox.api.items.BaseSTBBlock;
import io.github.thebusybiscuit.sensibletoolbox.items.PaintBrush;
import io.github.thebusybiscuit.sensibletoolbox.utils.ColoredMaterial;
import io.github.thebusybiscuit.sensibletoolbox.utils.STBUtil;
import me.desht.dhutils.Debugger;

public class PaintCan extends BaseSTBBlock implements LevelReporter {

    private static final int MAX_PAINT_LEVEL = 200;
    private static final int PAINT_PER_DYE = 25;
    private static final int[] ITEM_SLOTS = { 9, 10 };
    private static final ItemStack MIX_TEXTURE = new ItemStack(Material.GOLDEN_SHOVEL);
    private static final ItemStack EMPTY_TEXTURE = new ItemStack(Material.WHITE_STAINED_GLASS);

    private int paintLevel;
    private DyeColor color;
    private int levelMonitorId;

    public PaintCan() {
        paintLevel = 0;
        color = DyeColor.WHITE;
    }

    public PaintCan(ConfigurationSection conf) {
        super(conf);
        setPaintLevel(conf.getInt("paintLevel"));
        setColor(DyeColor.valueOf(conf.getString("paintColor")));
    }

    public static int getMaxPaintLevel() {
        return MAX_PAINT_LEVEL;
    }

    @Override
    public YamlConfiguration freeze() {
        YamlConfiguration conf = super.freeze();
        conf.set("paintColor", getColor().toString());
        conf.set("paintLevel", getPaintLevel());
        return conf;
    }

    public int getPaintLevel() {
        return paintLevel;
    }

    public void setPaintLevel(int paintLevel) {
        int oldLevel = this.paintLevel;
        this.paintLevel = paintLevel;
        update(oldLevel == 0 && paintLevel != 0 || oldLevel != 0 && paintLevel == 0);
        updateAttachedLabelSigns();

        LevelMonitor monitor = getPaintLevelMonitor();

        if (monitor != null) {
            monitor.repaint();
        }
    }

    @Nonnull
    public DyeColor getColor() {
        return color;
    }

    public void setColor(@Nonnull DyeColor color) {
        DyeColor oldColor = this.color;
        this.color = color;

        if (this.color != oldColor) {
            update(true);
            updateAttachedLabelSigns();

            LevelMonitor monitor = getPaintLevelMonitor();
            if (monitor != null) {
                monitor.repaint();
            }
        }
    }

    @Override
    public Material getMaterial() {
        ColoredMaterial materials = ColoredMaterial.STAINED_GLASS;

        if (getPaintLevel() > 0) {
            materials = ColoredMaterial.WOOL;
        }

        return materials.get(color.ordinal());
    }

    @Override
    public String getItemName() {
        return "Paint Can";
    }

    @Override
    public String[] getLore() {
        return new String[] { "R-click block with Paint Brush", " to refill the brush", "R-click block with anything else", " to open mixer; place milk bucket and", " a dye inside to mix some paint" };
    }

    @Override
    public Recipe getMainRecipe() {
        ShapedRecipe recipe = new ShapedRecipe(getKey(), toItemStack());
        recipe.shape("GSG", "G G", "III");
        recipe.setIngredient('S', new MaterialChoice(Tag.WOODEN_SLABS));
        recipe.setIngredient('G', Material.GLASS);
        recipe.setIngredient('I', Material.IRON_INGOT);
        return recipe;
    }

    @Override
    public void onInteractBlock(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack stack = event.getItem();

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            PaintBrush brush = SensibleToolbox.getItemRegistry().fromItemStack(stack, PaintBrush.class);

            if (brush == null) {
                // refilling a paintbrush/roller from the can is handled in the PaintBrush object
                getGUI().show(player);
                LevelMonitor monitor = getPaintLevelMonitor();

                if (monitor != null) {
                    monitor.repaint();
                }
            }

            event.setCancelled(true);
        } else {
            super.onInteractBlock(event);
        }
    }

    @Override
    public InventoryGUI createGUI() {
        InventoryGUI gui = GUIUtil.createGUI(this, 27, ChatColor.DARK_RED + getItemName());
        gui.addLabel("Ingredients", 0, null, "To mix paint:", "▶ Place a milk bucket & dye", "To dye items:", "▶ Place any dyeable item");

        for (int slot : ITEM_SLOTS) {
            gui.setSlotType(slot, SlotType.ITEM);
        }

        String[] lore = new String[] { "Combine milk & dye to make paint", "or dye any colorable item", "with existing paint" };
        gui.addGadget(new ButtonGadget(gui, 12, "Mix or Dye", lore, MIX_TEXTURE, () -> {
            if (tryMix()) {
                Location loc = getLocation();
                loc.getWorld().playSound(loc, Sound.ENTITY_GENERIC_SPLASH, 1.0F, 1.0F);
            }
        }));

        lore = new String[] { "Caution: This will empty the", "paint tank and can't be undone!" };
        gui.addGadget(new ButtonGadget(gui, 13, ChatColor.RED.toString() + ChatColor.UNDERLINE + "☠ Empty Paint ☠", lore, EMPTY_TEXTURE, this::emptyPaintCan));

        levelMonitorId = gui.addMonitor(new LevelMonitor(gui, this));
        gui.addGadget(new AccessControlGadget(gui, 8));
        return gui;
    }

    @Override
    public int getLevel() {
        return getPaintLevel();
    }

    @Override
    public int getMaxLevel() {
        return getMaxPaintLevel();
    }

    @Override
    public ItemStack getLevelIcon() {
        ItemStack stack = new ItemStack(Material.LEATHER_LEGGINGS);
        LeatherArmorMeta meta = (LeatherArmorMeta) stack.getItemMeta();
        meta.setColor(getColor().getColor());
        stack.setItemMeta(meta);
        return stack;
    }

    @Override
    public int getLevelMonitorSlot() {
        return 15;
    }

    @Override
    public String getLevelMessage() {
        ChatColor cc = STBUtil.dyeColorToChatColor(getColor());
        return ChatColor.WHITE + "Paint Level: " + getPaintLevel() + "/" + MAX_PAINT_LEVEL + " " + cc + getColor();
    }

    private void emptyPaintCan() {
        setPaintLevel(0);
        Location loc = getLocation();
        loc.getWorld().playSound(loc, Sound.ENTITY_PLAYER_SPLASH, 1.0F, 1.0F);
    }

    @Nullable
    private LevelMonitor getPaintLevelMonitor() {
        return getGUI() == null ? null : (LevelMonitor) getGUI().getMonitor(levelMonitorId);
    }

    private boolean validItem(@Nonnull ItemStack item) {
        return !item.hasItemMeta() && (STBUtil.isColorable(item.getType()) || item.getType() == Material.MILK_BUCKET || STBUtil.isDye(item.getType()));
    }

    @Override
    public boolean onSlotClick(HumanEntity player, int slot, ClickType click, ItemStack inSlot, ItemStack onCursor) {
        return onCursor.getType() == Material.AIR || validItem(onCursor);
    }

    @Override
    public boolean onPlayerInventoryClick(HumanEntity player, int slot, ClickType click, ItemStack inSlot, ItemStack onCursor) {
        return true;
    }

    @Override
    public int onShiftClickInsert(HumanEntity player, int slot, ItemStack toInsert) {
        if (!validItem(toInsert)) {
            return 0;
        } else {
            Map<Integer, ItemStack> excess = getGUI().getInventory().addItem(toInsert);
            int inserted = toInsert.getAmount();

            for (ItemStack stack : excess.values()) {
                inserted -= stack.getAmount();
            }

            return inserted;
        }
    }

    @Override
    public boolean onShiftClickExtract(HumanEntity player, int slot, ItemStack toExtract) {
        return true;
    }

    @Override
    public boolean onClickOutside(HumanEntity player) {
        return false;
    }

    @Override
    public void onGUIClosed(HumanEntity player) {
        if (getGUI().getViewers().size() == 1) {
            // last player closing inventory - eject any remaining items
            Location loc = getLocation();

            for (int slot : ITEM_SLOTS) {
                ItemStack item = getGUI().getInventory().getItem(slot);

                if (item != null) {
                    loc.getWorld().dropItemNaturally(getLocation(), item);
                    getGUI().getInventory().setItem(slot, null);
                }
            }
        }
    }

    @Override
    public String getDisplaySuffix() {
        return getPaintLevel() + " " + STBUtil.dyeColorToChatColor(getColor()) + getColor();
    }

    @Override
    protected String[] getSignLabel(BlockFace face) {
        String[] res = super.getSignLabel(face);
        ChatColor cc = STBUtil.dyeColorToChatColor(getColor());
        res[1] = cc.toString() + getColor();
        res[2] = getPaintLevel() + "/" + getMaxPaintLevel();
        res[3] = cc + Strings.repeat("◼", (getPaintLevel() * 13) / getMaxPaintLevel());
        return res;
    }

    /**
     * Attempt to refill the can from the contents of the can's inventory. The mixer needs to find
     * a milk bucket and at least one dye. If mixing is successful, the bucket is replaced with an
     * empty bucket and dye is removed, and the method returns true.
     *
     * @return true if mixing was successful, false otherwise
     */
    public boolean tryMix() {
        int bucketSlot = -1;
        int dyeSlot = -1;
        int dyeableSlot = -1;

        Inventory inventory = getGUI().getInventory();

        // first try to find a milk bucket, dye and/or wool
        for (int slot : ITEM_SLOTS) {
            ItemStack stack = inventory.getItem(slot);
            if (stack != null) {
                if (stack.getType() == Material.MILK_BUCKET && !stack.hasItemMeta() && bucketSlot == -1) {
                    bucketSlot = slot;
                } else if (STBUtil.isDye(stack.getType()) && !stack.hasItemMeta() && dyeSlot == -1) {
                    dyeSlot = slot;
                } else if (validItem(stack) && dyeableSlot == -1) {
                    dyeableSlot = slot;
                } else {
                    // not an item we want - eject it
                    getLocation().getWorld().dropItemNaturally(getLocation(), stack);
                    inventory.setItem(slot, null);
                }
            }
        }

        Debugger.getInstance().debug(this + ": dyeable=" + dyeableSlot + " dye=" + dyeSlot + " milk=" + bucketSlot);

        if (bucketSlot >= 0 && dyeSlot >= 0) {
            // we have milk & some dye - mix it up!
            ItemStack dyeStack = inventory.getItem(dyeSlot);
            DyeColor newColor = STBUtil.getColorFromDye(dyeStack.getType());
            int dyeAmount = dyeStack.getAmount();
            int paintPerDye = getItemConfig().getInt("paint_per_dye", PAINT_PER_DYE);
            int toUse = Math.min((getMaxPaintLevel() - getPaintLevel()) / paintPerDye, dyeAmount);

            if (toUse == 0) {
                // not enough room for any mixing
                return false;
            }

            if (getColor() != newColor && getPaintLevel() > 0) {
                // two different colors - do they mix?
                DyeColor mixedColor = mixDyes(getColor(), newColor);

                if (mixedColor == null) {
                    // no - just replace the can's contents with the new color
                    toUse = Math.min(getMaxPaintLevel() / paintPerDye, dyeAmount);
                    setColor(newColor);
                    setPaintLevel(paintPerDye * toUse);
                } else {
                    // yes, they mix
                    setColor(mixedColor);
                    setPaintLevel(Math.min(getMaxPaintLevel(), getPaintLevel() + paintPerDye * toUse));
                }
            } else {
                // either adding to an empty can, or adding more of the same color
                setColor(newColor);
                setPaintLevel(Math.min(getMaxPaintLevel(), getPaintLevel() + paintPerDye * toUse));
            }

            Debugger.getInstance().debug(this + ": paint mixed! now " + getPaintLevel() + " " + getColor());

            Location loc = getLocation();
            loc.getWorld().playSound(loc, Sound.ENTITY_GENERIC_SPLASH, 1.0F, 1.0F);

            inventory.setItem(bucketSlot, new ItemStack(Material.BUCKET));
            dyeStack.setAmount(dyeStack.getAmount() - toUse);
            inventory.setItem(dyeSlot, dyeStack.getAmount() > 0 ? dyeStack : null);

            return true;
        } else if (dyeableSlot >= 0 && getPaintLevel() > 0) {
            // soak up some paint with the dyeable item(s)
            // TODO: Fix Paint Can
            // int toDye = inventory.getItem(dyeableSlot).getAmount();
            // Debugger.getInstance().debug(this + ": dyeing " + inventory.getItem(dyeableSlot));
            // int canDye = Math.min(toDye, getPaintLevel());
            // ItemStack undyed =
            // inventory.getItem(dyeableSlot).getData().toItemStack(inventory.getItem(dyeableSlot).getAmount());
            // ItemStack dyed = STBUtil.makeColoredMaterial(undyed.getType(), getColor()).toItemStack(Math.min(canDye,
            // undyed.getAmount()));
            // undyed.setAmount(undyed.getAmount() - dyed.getAmount());
            // inventory.setItem(ITEM_SLOTS[0], dyed.getAmount() > 0 ? dyed : null);
            // inventory.setItem(ITEM_SLOTS[1], undyed.getAmount() > 0 ? undyed : null);
            // setPaintLevel(getPaintLevel() - canDye);
            // Location loc = getLocation();
            // loc.getWorld().playSound(loc, Sound.ENTITY_GENERIC_SPLASH, 1.0f, 1.0f);
            return true;
        } else {
            return false;
        }
    }

    private DyeColor mixDyes(DyeColor dye1, DyeColor dye2) {
        if (dye1 == dye2) {
            return dye1;
        } else if (dye1.compareTo(dye2) > 0) {
            DyeColor tmp = dye2;
            dye2 = dye1;
            dye1 = tmp;
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
        } else if (dye1 == DyeColor.WHITE && dye2 == DyeColor.BLACK) {
            return DyeColor.GRAY;
        } else if (dye1 == DyeColor.WHITE && dye2 == DyeColor.BLUE) {
            return DyeColor.LIGHT_BLUE;
        } else if (dye1 == DyeColor.WHITE && dye2 == DyeColor.GREEN) {
            return DyeColor.LIME;
        } else if (dye1 == DyeColor.PINK && dye2 == DyeColor.PURPLE) {
            return DyeColor.MAGENTA;
        } else if (dye1 == DyeColor.WHITE && dye2 == DyeColor.GRAY) {
            return DyeColor.LIGHT_GRAY;
        } else {
            // colors don't mix
            return null;
        }
    }
}
