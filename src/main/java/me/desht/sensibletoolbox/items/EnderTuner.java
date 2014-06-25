package me.desht.sensibletoolbox.items;

import me.desht.sensibletoolbox.SensibleToolboxPlugin;
import me.desht.sensibletoolbox.api.EnderTunable;
import me.desht.sensibletoolbox.gui.InventoryGUI;
import me.desht.sensibletoolbox.gui.NumericGadget;
import me.desht.sensibletoolbox.util.STBUtil;
import org.apache.commons.lang.math.IntRange;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
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
    private static final MaterialData md = new MaterialData(Material.ARROW);
    public static final int TUNED_ITEM = 11;
    public static final int FREQUENCY_BUTTON_SLOT = 13;
    private InventoryGUI gui;

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
                "R-Click Air: " + ChatColor.RESET + "open GUI",
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
        gui = makeTuningInventory(event.getPlayer());
        gui.show(event.getPlayer());
    }

    private InventoryGUI makeTuningInventory(Player player) {
        final InventoryGUI gui = new InventoryGUI(player, this, 27, ChatColor.DARK_PURPLE + "Ender Tuner");
        for (int slot = 0; slot < 27; slot++) {
            gui.setSlotType(slot, InventoryGUI.SlotType.BACKGROUND);
        }
        gui.setSlotType(TUNED_ITEM, InventoryGUI.SlotType.ITEM);
        gui.addLabel("Ender Bag", 10, null, "Place an Ender Bag", "here to tune its", "ender frequency");

        gui.addGadget(new NumericGadget(gui, FREQUENCY_BUTTON_SLOT, "Frequency", new IntRange(1, 1000), 1, 1, 10, new NumericGadget.UpdateListener() {
            @Override
            public boolean run(int value) {
                ItemStack stack = gui.getItem(TUNED_ITEM);
                BaseSTBItem item = BaseSTBItem.getItemFromItemStack(stack);
                if (item instanceof EnderTunable) {
                    ((EnderTunable) item).setEnderFrequency(value);
                    gui.setItem(TUNED_ITEM, item.toItemStack(stack.getAmount()));
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
        if (slot == TUNED_ITEM) {
            if (onCursor.getType() == Material.AIR) {
                ((NumericGadget) gui.getGadget(FREQUENCY_BUTTON_SLOT)).setValue(1);
                return true;
            } else {
                BaseSTBItem item = BaseSTBItem.getItemFromItemStack(onCursor);
                if (item instanceof EnderTunable) {
                    ((NumericGadget) gui.getGadget(FREQUENCY_BUTTON_SLOT)).setValue(((EnderTunable) item).getEnderFrequency());
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
        BaseSTBItem item = BaseSTBItem.getItemFromItemStack(toInsert);
        if (item instanceof EnderTunable && gui.getItem(TUNED_ITEM) == null) {
            gui.setItem(TUNED_ITEM, toInsert);
            ((NumericGadget) gui.getGadget(FREQUENCY_BUTTON_SLOT)).setValue(((EnderTunable) item).getEnderFrequency());
            return toInsert.getAmount();
        } else {
            return 0;
        }
    }

    @Override
    public boolean onShiftClickExtract(HumanEntity player, int slot, ItemStack toExtract) {
        if (slot == TUNED_ITEM && gui.getItem(slot) != null) {
            ((NumericGadget) gui.getGadget(FREQUENCY_BUTTON_SLOT)).setValue(1);
            return true;
        }
        return slot == TUNED_ITEM && gui.getItem(slot) != null;
    }

    @Override
    public boolean onClickOutside(HumanEntity player) {
        return false;
    }

    @Override
    public void onGUIClosed(HumanEntity player) {
        ItemStack stack = gui.getItem(TUNED_ITEM);
        if (stack != null) {
            STBUtil.giveItems(player, stack);
        }
    }
}
