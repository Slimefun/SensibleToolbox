package me.desht.sensibletoolbox.items;

import me.desht.sensibletoolbox.SensibleToolboxPlugin;
import me.desht.sensibletoolbox.api.EnderTunable;
import me.desht.sensibletoolbox.api.STBBlock;
import me.desht.sensibletoolbox.enderstorage.EnderStorageManager;
import me.desht.sensibletoolbox.gui.AccessControlGadget;
import me.desht.sensibletoolbox.gui.InventoryGUI;
import me.desht.sensibletoolbox.gui.NumericGadget;
import me.desht.sensibletoolbox.gui.ToggleButton;
import me.desht.sensibletoolbox.storage.LocationManager;
import me.desht.sensibletoolbox.util.STBUtil;
import org.apache.commons.lang.math.IntRange;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.material.MaterialData;

public class EnderTuner extends BaseSTBItem {
    private static final MaterialData md = new MaterialData(Material.GOLD_NUGGET);
    private static final ItemStack GLOBAL_TEXTURE = InventoryGUI.makeTexture(
            STBUtil.makeColouredMaterial(Material.STAINED_GLASS, DyeColor.BLUE), "Global", "Common inventory for", "all players");
    private static final ItemStack PERSONAL_TEXTURE = InventoryGUI.makeTexture(
            STBUtil.makeColouredMaterial(Material.STAINED_GLASS, DyeColor.YELLOW), "Personal", "Separate inventory for", "each player");
    public static final int TUNING_GUI_SIZE = 27;
    public static final int TUNED_ITEM_SLOT = 11;
    public static final int FREQUENCY_BUTTON_SLOT = 13;
    public static final int GLOBAL_BUTTON_SLOT = 8;
    public static final int ACCESS_CONTROL_SLOT = 17;
    private InventoryGUI gui;
    private EnderTunable tuningBlock = null;

    public EnderTuner() {
    }

    public EnderTuner(ConfigurationSection conf) {
        super(conf);
    }

    @Override
    public MaterialData getMaterialData() {
        return md;
    }

    @Override
    public String getItemName() {
        return "Ender Tuner";
    }

    @Override
    public String[] getLore() {
        return new String[] {
                "Vibrates in six dimensions",
                "R-Click Ender Box: " + ChatColor.RESET + "tune box",
                "R-Click other: " + ChatColor.RESET + "open tuning GUI",
        };
    }

    @Override
    public Recipe getRecipe() {
        ShapedRecipe recipe = new ShapedRecipe(toItemStack(1));
        recipe.shape("SES", "III", " G ");
        recipe.setIngredient('S', Material.GLOWSTONE_DUST);
        recipe.setIngredient('I', Material.IRON_INGOT);
        recipe.setIngredient('E', Material.ENDER_PEARL);
        recipe.setIngredient('G', Material.GOLD_INGOT);
        return recipe;
    }

    @Override
    public boolean hasGlow() {
        return true;
    }

    @Override
    public void onInteractItem(PlayerInteractEvent event) {
        Block clicked = event.getClickedBlock();
        STBBlock stb = clicked == null ? null : LocationManager.getManager().get(clicked.getLocation(), true);
        if (stb instanceof EnderTunable && stb.hasAccessRights(event.getPlayer())) {
            tuningBlock = (EnderTunable) stb;
        }
        gui = makeTuningInventory(event.getPlayer());
        gui.show(event.getPlayer());
        event.setCancelled(true);
    }

    private InventoryGUI makeTuningInventory(Player player) {
        final InventoryGUI gui = new InventoryGUI(player, this, TUNING_GUI_SIZE, ChatColor.DARK_PURPLE + "Ender Tuner");
        for (int slot = 0; slot < gui.getInventory().getSize(); slot++) {
            gui.setSlotType(slot, InventoryGUI.SlotType.BACKGROUND);
        }
        gui.setSlotType(TUNED_ITEM_SLOT, InventoryGUI.SlotType.ITEM);
        int freq = 1;
        boolean global = false;
        if (tuningBlock != null) {
            gui.setItem(TUNED_ITEM_SLOT, ((STBBlock)tuningBlock).toItemStack());
            gui.addLabel("Ender Box", TUNED_ITEM_SLOT - 1, null);
            freq = tuningBlock.getEnderFrequency();
            global = tuningBlock.isGlobal();
            gui.addGadget(new AccessControlGadget(gui, ACCESS_CONTROL_SLOT, (STBBlock) tuningBlock));
        } else {
            gui.addLabel("Ender Bag", TUNED_ITEM_SLOT - 1, null, "Place an Ender Bag or", "Ender Box here to tune", "its Ender frequency");
        }
        gui.addGadget(new ToggleButton(gui, GLOBAL_BUTTON_SLOT, global, GLOBAL_TEXTURE, PERSONAL_TEXTURE, new ToggleButton.ToggleListener() {
            @Override
            public boolean run(boolean newValue) {
                ItemStack stack = gui.getItem(TUNED_ITEM_SLOT);
                BaseSTBItem item = BaseSTBItem.fromItemStack(stack);
                if (item instanceof EnderTunable) {
                    ((EnderTunable) item).setGlobal(newValue);
                    gui.setItem(TUNED_ITEM_SLOT, item.toItemStack(stack.getAmount()));
                    if (tuningBlock != null) {
                        tuningBlock.setGlobal(newValue);
                    }
                    return true;
                }
                return false;
            }
        }));
        gui.addGadget(new NumericGadget(gui, FREQUENCY_BUTTON_SLOT, "Ender Frequency", new IntRange(1, EnderStorageManager.MAX_ENDER_FREQUENCY), freq, 1, 10, new NumericGadget.NumericListener() {
            @Override
            public boolean run(int newValue) {
                ItemStack stack = gui.getItem(TUNED_ITEM_SLOT);
                BaseSTBItem item = BaseSTBItem.fromItemStack(stack);
                if (item instanceof EnderTunable) {
                    ((EnderTunable) item).setEnderFrequency(newValue);
                    gui.setItem(TUNED_ITEM_SLOT, item.toItemStack(stack.getAmount()));
                    if (tuningBlock != null) {
                        tuningBlock.setEnderFrequency(newValue);
                    }
                    return true;
                } else {
                    return false;
                }
            }
        }));
        return gui;
    }

    @Override
    public boolean onSlotClick(HumanEntity player, int slot, ClickType click, ItemStack inSlot, ItemStack onCursor) {
        if (tuningBlock != null) {
            if (player instanceof Player) {
                STBUtil.complain((Player) player);
            }
            return false;
        }
        if (slot == TUNED_ITEM_SLOT) {
            if (onCursor.getType() == Material.AIR) {
                ((NumericGadget) gui.getGadget(FREQUENCY_BUTTON_SLOT)).setValue(1);
                return true;
            } else {
                BaseSTBItem item = BaseSTBItem.fromItemStack(onCursor);
                if (item instanceof EnderTunable) {
                    ((NumericGadget) gui.getGadget(FREQUENCY_BUTTON_SLOT)).setValue(((EnderTunable) item).getEnderFrequency());
                    ((ToggleButton) gui.getGadget(GLOBAL_BUTTON_SLOT)).setValue(((EnderTunable) item).isGlobal());
                    _hackyDelayedInvUpdate((Player) player);
                    return true;
                }
            }
        }
        return false;
    }

    private void _hackyDelayedInvUpdate(final Player player) {
        // this is unfortunately needed to ensure player sees the updated frequency
        // button text after a tunable item is inserted
        Bukkit.getScheduler().runTask(SensibleToolboxPlugin.getInstance(), new Runnable() {
            @Override
            public void run() {
                //noinspection deprecation
                player.updateInventory();
            }
        });
    }

    @Override
    public boolean onPlayerInventoryClick(HumanEntity player, int slot, ClickType click, ItemStack inSlot, ItemStack onCursor) {
        return true;
    }

    @Override
    public int onShiftClickInsert(HumanEntity player, int slot, ItemStack toInsert) {
        if (tuningBlock != null) {
            if (player instanceof Player) {
                STBUtil.complain((Player) player);
            }
            return 0;
        }
        BaseSTBItem item = BaseSTBItem.fromItemStack(toInsert);
        if (item instanceof EnderTunable && gui.getItem(TUNED_ITEM_SLOT) == null) {
            gui.setItem(TUNED_ITEM_SLOT, toInsert);
            ((NumericGadget) gui.getGadget(FREQUENCY_BUTTON_SLOT)).setValue(((EnderTunable) item).getEnderFrequency());
            ((ToggleButton) gui.getGadget(GLOBAL_BUTTON_SLOT)).setValue(((EnderTunable) item).isGlobal());
            return toInsert.getAmount();
        } else {
            return 0;
        }
    }

    @Override
    public boolean onShiftClickExtract(HumanEntity player, int slot, ItemStack toExtract) {
        if (tuningBlock != null) {
            if (player instanceof Player) {
                STBUtil.complain((Player) player);
            }
            return false;
        }
        if (slot == TUNED_ITEM_SLOT && gui.getItem(slot) != null) {
            ((NumericGadget) gui.getGadget(FREQUENCY_BUTTON_SLOT)).setValue(1);
            return true;
        }
        return slot == TUNED_ITEM_SLOT && gui.getItem(slot) != null;
    }

    @Override
    public boolean onClickOutside(HumanEntity player) {
        return false;
    }

    @Override
    public void onGUIClosed(HumanEntity player) {
        if (tuningBlock == null) {
            ItemStack stack = gui.getItem(TUNED_ITEM_SLOT);
            if (stack != null) {
                STBUtil.giveItems(player, stack);
            }
        }
    }
}
