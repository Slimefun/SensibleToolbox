package io.github.thebusybiscuit.sensibletoolbox.blocks;

import java.util.HashMap;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Skull;
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
import org.bukkit.material.Dye;
import org.bukkit.material.Wool;

import io.github.thebusybiscuit.sensibletoolbox.api.SensibleToolbox;
import io.github.thebusybiscuit.sensibletoolbox.api.gui.AccessControlGadget;
import io.github.thebusybiscuit.sensibletoolbox.api.gui.ButtonGadget;
import io.github.thebusybiscuit.sensibletoolbox.api.gui.GUIUtil;
import io.github.thebusybiscuit.sensibletoolbox.api.gui.InventoryGUI;
import io.github.thebusybiscuit.sensibletoolbox.api.gui.LevelMonitor;
import io.github.thebusybiscuit.sensibletoolbox.api.items.BaseSTBBlock;
import io.github.thebusybiscuit.sensibletoolbox.api.util.STBUtil;
import io.github.thebusybiscuit.sensibletoolbox.items.PaintBrush;
import me.desht.dhutils.Debugger;

public class PaintCan extends BaseSTBBlock implements LevelMonitor.LevelReporter {
	
    private static final int MAX_PAINT_LEVEL = 200;
    private static final int PAINT_PER_DYE = 25;
    private static final int[] ITEM_SLOTS = {9, 10};
    private static final ItemStack MIX_TEXTURE = new ItemStack(Material.GOLDEN_SHOVEL);
    private static final ItemStack EMPTY_TEXTURE = new ItemStack(Material.WHITE_STAINED_GLASS);
    
    private int paintLevel;
    private DyeColor colour;
    private int levelMonitorId;

    public PaintCan() {
        paintLevel = 0;
        colour = DyeColor.WHITE;
    }

    public PaintCan(ConfigurationSection conf) {
        super(conf);
        setPaintLevel(conf.getInt("paintLevel"));
        setColour(DyeColor.valueOf(conf.getString("paintColour")));
    }

    public static int getMaxPaintLevel() {
        return MAX_PAINT_LEVEL;
    }

    @Override
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
        int oldLevel = this.paintLevel;
        this.paintLevel = paintLevel;
        update(oldLevel == 0 && paintLevel != 0 || oldLevel != 0 && paintLevel == 0);
        updateAttachedLabelSigns();
        
        if (getPaintLevelMonitor() != null) {
            getPaintLevelMonitor().repaint();
        }
    }

    public DyeColor getColour() {
        return colour;
    }

    public void setColour(DyeColor colour) {
        DyeColor oldColour = this.colour;
        this.colour = colour;
        
        if (this.colour != oldColour) {
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
        return getPaintLevel() > 0 ? new Wool(colour) : STBUtil.makeColouredMaterial(Material.STAINED_GLASS, colour);
    }

    @Override
    public String getItemName() {
        return "Paint Can";
    }

    @Override
    public String[] getLore() {
        return new String[]{
                "R-click block with Paint Brush",
                " to refill the brush",
                "R-click block with anything else",
                " to open mixer; place milk bucket and",
                " a dye inside to mix some paint"
        };
    }

    @Override
    public Recipe getRecipe() {
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
                getPaintLevelMonitor().repaint();
            }
            event.setCancelled(true);
        } 
        else {
            super.onInteractBlock(event);
        }
    }

    @Override
    public InventoryGUI createGUI() {
        InventoryGUI gui = GUIUtil.createGUI(this, 27, ChatColor.DARK_RED + getItemName());
        gui.addLabel("Ingredients", 0, null, "To mix paint:", "▶ Place a milk bucket & dye", "To dye items:", "▶ Place any dyeable item");
        
        for (int slot : ITEM_SLOTS) {
            gui.setSlotType(slot, InventoryGUI.SlotType.ITEM);
        }
        
        String[] lore = new String[] { "Combine milk & dye to make paint", "or dye any colourable item", "with existing paint"};
        gui.addGadget(new ButtonGadget(gui, 12, "Mix or Dye", lore, MIX_TEXTURE, () -> {
        	if (tryMix()) {
                Location loc = getLocation();
                loc.getWorld().playSound(loc, Sound.ENTITY_GENERIC_SPLASH, 1.0f, 1.0f);
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
        return MAX_PAINT_LEVEL;
    }

    @Override
    public ItemStack getLevelIcon() {
        ItemStack stack = new ItemStack(Material.LEATHER_LEGGINGS);
        LeatherArmorMeta meta = (LeatherArmorMeta) stack.getItemMeta();
        meta.setColor(getColour().getColor());
        stack.setItemMeta(meta);
        return stack;
    }

    @Override
    public int getLevelMonitorSlot() {
        return 15;
    }

    @Override
    public String getLevelMessage() {
        ChatColor cc = STBUtil.dyeColorToChatColor(getColour());
        return ChatColor.WHITE + "Paint Level: " + getPaintLevel() + "/" + MAX_PAINT_LEVEL + " " + cc + getColour();
    }

    private void emptyPaintCan() {
        setPaintLevel(0);
        Location loc = getLocation();
        loc.getWorld().playSound(loc, Sound.ENTITY_PLAYER_SPLASH, 1.0f, 1.0f);
    }

    private LevelMonitor getPaintLevelMonitor() {
        return getGUI() == null ? null : (LevelMonitor) getGUI().getMonitor(levelMonitorId);
    }

    private boolean validItem(ItemStack item) {
        return !item.hasItemMeta() &&
                (STBUtil.isColorable(item.getType()) ||
                item.getType() == Material.MILK_BUCKET || item.getType() == Material.INK_SACK ||
                item.getType() == Material.GLASS || item.getType() == Material.THIN_GLASS);
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
        } 
        else {
            HashMap<Integer, ItemStack> excess = getGUI().getInventory().addItem(toInsert);
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
        return getPaintLevel() + " " + STBUtil.dyeColorToChatColor(getColour()) + getColour();
    }

    @Override
    public RelativePosition[] getBlockStructure() {
        return new RelativePosition[]{new RelativePosition(0, 1, 0)};
    }

    @Override
    public void onBlockRegistered(Location location, boolean isPlacing) {
        if (isPlacing) {
            Block above = location.getBlock().getRelative(BlockFace.UP);
            Skull skull = STBUtil.setSkullHead(above, "MHF_OakLog", getFacing());
            skull.update();
        }

        super.onBlockRegistered(location, isPlacing);
    }

    @Override
    protected String[] getSignLabel(BlockFace face) {
        String res[] = super.getSignLabel(face);
        ChatColor cc = STBUtil.dyeColorToChatColor(getColour());
        res[1] = cc.toString() + getColour();
        res[2] = getPaintLevel() + "/" + getMaxPaintLevel();
        res[3] = cc + StringUtils.repeat("◼", (getPaintLevel() * 13) / getMaxPaintLevel());
        return res;
    }

    /**
     * Attempt to refill the can from the contents of the can's inventory.  The mixer needs to find
     * a milk bucket and at least one dye.  If mixing is successful, the bucket is replaced with an
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
                } 
                else if (stack.getType() == Material.INK_SACK && !stack.hasItemMeta() && dyeSlot == -1) {
                    dyeSlot = slot;
                } 
                else if (validItem(stack) && dyeableSlot == -1) {
                    dyeableSlot = slot;
                } 
                else {
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
            Dye dye = (Dye) dyeStack.getData();
            DyeColor newColour = dye.getColor();
            int dyeAmount = dyeStack.getAmount();
            int paintPerDye = getItemConfig().getInt("paint_per_dye", PAINT_PER_DYE);
            int toUse = Math.min((getMaxPaintLevel() - getPaintLevel()) / paintPerDye, dyeAmount);
            
            if (toUse == 0) {
                // not enough room for any mixing
                return false;
            }
            
            if (getColour() != newColour && getPaintLevel() > 0) {
                // two different colours - do they mix?
                DyeColor mixedColour = mixDyes(getColour(), newColour);
                
                if (mixedColour == null) {
                    // no - just replace the can's contents with the new colour
                    toUse = Math.min(getMaxPaintLevel() / paintPerDye, dyeAmount);
                    setColour(newColour);
                    setPaintLevel(paintPerDye * toUse);
                } 
                else {
                    // yes, they mix
                    setColour(mixedColour);
                    setPaintLevel(Math.min(getMaxPaintLevel(), getPaintLevel() + paintPerDye * toUse));
                }
            } 
            else {
                // either adding to an empty can, or adding more of the same colour
                setColour(newColour);
                setPaintLevel(Math.min(getMaxPaintLevel(), getPaintLevel() + paintPerDye * toUse));
            }
            
            Debugger.getInstance().debug(this + ": paint mixed! now " + getPaintLevel() + " " + getColour());

            Location loc = getLocation();
            loc.getWorld().playSound(loc, Sound.ENTITY_GENERIC_SPLASH, 1.0f, 1.0f);

            inventory.setItem(bucketSlot, new ItemStack(Material.BUCKET));
            dyeStack.setAmount(dyeStack.getAmount() - toUse);
            inventory.setItem(dyeSlot, dyeStack.getAmount() > 0 ? dyeStack : null);

            return true;
        } 
        else if (dyeableSlot >= 0 && getPaintLevel() > 0) {
            // soak up some paint with the dyeable item(s)
            int toDye = inventory.getItem(dyeableSlot).getAmount();
            Debugger.getInstance().debug(this + ": dyeing " + inventory.getItem(dyeableSlot));
            int canDye = Math.min(toDye, getPaintLevel());
            ItemStack undyed = inventory.getItem(dyeableSlot).getData().toItemStack(inventory.getItem(dyeableSlot).getAmount());
            ItemStack dyed = STBUtil.makeColouredMaterial(undyed.getType(), getColour()).toItemStack(Math.min(canDye, undyed.getAmount()));
            undyed.setAmount(undyed.getAmount() - dyed.getAmount());
            inventory.setItem(ITEM_SLOTS[0], dyed.getAmount() > 0 ? dyed : null);
            inventory.setItem(ITEM_SLOTS[1], undyed.getAmount() > 0 ? undyed : null);
            setPaintLevel(getPaintLevel() - canDye);
            Location loc = getLocation();
            loc.getWorld().playSound(loc, Sound.ENTITY_GENERIC_SPLASH, 1.0f, 1.0f);
            return true;
        } 
        else {
            return false;
        }
    }

    private DyeColor mixDyes(DyeColor dye1, DyeColor dye2) {
        if (dye1.compareTo(dye2) > 0) {
            DyeColor tmp = dye2;
            dye2 = dye1;
            dye1 = tmp;
        } 
        else if (dye1 == dye2) {
            return dye1;
        }
        
        Debugger.getInstance().debug(this + ": try mixing: " + dye1 + " " + dye2);
        
        if (dye1 == DyeColor.YELLOW && dye2 == DyeColor.RED) {
            return DyeColor.ORANGE;
        } 
        else if (dye1 == DyeColor.WHITE && dye2 == DyeColor.RED) {
            return DyeColor.PINK;
        } 
        else if (dye1 == DyeColor.BLUE && dye2 == DyeColor.GREEN) {
            return DyeColor.CYAN;
        } 
        else if (dye1 == DyeColor.BLUE && dye2 == DyeColor.RED) {
            return DyeColor.PURPLE;
        } 
        else if (dye1 == DyeColor.WHITE && dye2 == DyeColor.BLACK) {
            return DyeColor.GRAY;
        } 
        else if (dye1 == DyeColor.WHITE && dye2 == DyeColor.BLUE) {
            return DyeColor.LIGHT_BLUE;
        } 
        else if (dye1 == DyeColor.WHITE && dye2 == DyeColor.GREEN) {
            return DyeColor.LIME;
        } 
        else if (dye1 == DyeColor.PINK && dye2 == DyeColor.PURPLE) {
            return DyeColor.MAGENTA;
        } 
        else if (dye1 == DyeColor.WHITE && dye2 == DyeColor.GRAY) {
            return DyeColor.LIGHT_GRAY;
        } 
        else {
            // colours don't mix
            return null;
        }
    }
}
