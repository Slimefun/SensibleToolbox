package me.desht.sensibletoolbox.listeners;

import me.desht.sensibletoolbox.SensibleToolboxPlugin;
import me.desht.sensibletoolbox.api.items.BaseSTBItem;
import me.desht.sensibletoolbox.api.SensibleToolbox;
import me.desht.sensibletoolbox.api.util.STBUtil;
import me.desht.sensibletoolbox.api.recipes.RecipeUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Hopper;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.ItemStack;

/**
 * Contains event handlers to ensure vanilla and STB items behave correctly
 * in furnaces.  Many STB items are smeltable, but use vanilla materials which
 * are not; we must ensure that those vanilla items can't be smelted.
 */
public class FurnaceListener extends STBBaseListener {
    public FurnaceListener(SensibleToolboxPlugin plugin) {
        super(plugin);
    }


    @EventHandler
    public void onFurnaceInsert(final InventoryClickEvent event) {
        if (event.getInventory().getType() != InventoryType.FURNACE) {
            return;
        }
        if (event.getRawSlot() == 0 && event.getCursor().getType() != Material.AIR) {
            if (!validateSmeltingIngredient(event.getCursor())) {
                event.setCancelled(true);
            }
        } else if (event.getRawSlot() >= event.getView().getTopInventory().getSize()) {
            if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
                if (!validateSmeltingIngredient(event.getCurrentItem()) && !STBUtil.isFuel(event.getCurrentItem().getType())) {
                    event.setCancelled(true);
                    int newSlot = findNewSlot(event);
                    if (newSlot >= 0) {
                        event.getWhoClicked().getInventory().setItem(newSlot, event.getCurrentItem());
                        event.setCurrentItem(null);
                    }
                }
            }
        } else if (event.getRawSlot() == 2 && SensibleToolbox.getItemRegistry().isSTBItem(event.getCurrentItem())) {
            // work around CB bug where shift-clicking custom items out of furnace seems
            // to cause a de-sync, leaving phantom items in the furnace
            Bukkit.getScheduler().runTask(plugin, new Runnable() {
                @Override
                public void run() {
                    STBUtil.forceInventoryRefresh(event.getInventory());
                }
            });
        }
    }

    @EventHandler
    public void onFurnaceInsert(InventoryDragEvent event) {
        if (event.getInventory().getType() != InventoryType.FURNACE) {
            return;
        }
        if (event.getOldCursor() != null && event.getOldCursor().getType() != Material.AIR) {
            for (int slot : event.getRawSlots()) {
                if (slot == 0 && !validateSmeltingIngredient(event.getOldCursor())) {
                    event.setCancelled(true);
                    break;
                }
            }
        }
    }

    @EventHandler
    public void onFurnaceInsertHopper(InventoryMoveItemEvent event) {
        if (event.getDestination().getType() != InventoryType.FURNACE) {
            return;
        }

        if (event.getSource().getHolder() instanceof Hopper) {
            Block b1 = ((BlockState) event.getSource().getHolder()).getBlock();
            Block b2 = ((BlockState) event.getDestination().getHolder()).getBlock();
            if (b1.getY() == b2.getY() + 1) {
                // hopper above the furnace - trying to insert items to be smelted
                if (!validateSmeltingIngredient(event.getItem())) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onSmelt(FurnaceSmeltEvent event) {
        // this ensures that an STB item smelted in a furnace leaves the
        // correct result in the furnace's output slot
        BaseSTBItem item = SensibleToolbox.getItemRegistry().fromItemStack(event.getSource());
        if (item != null) {
            event.setResult(item.getSmeltingResult());

        }
    }

    private boolean validateSmeltingIngredient(ItemStack stack) {
        BaseSTBItem item = SensibleToolbox.getItemRegistry().fromItemStack(stack);
        if (item != null) {
            return item.getSmeltingResult() != null;
        } else {
            // vanilla item - need to ensure it's actually smeltable (i.e. wasn't added
            // as a furnace recipe because it's the material for some custom STB item)
            return RecipeUtil.isVanillaSmelt(stack.getType());
        }
    }

    private int findNewSlot(InventoryClickEvent event) {
        int from = -1, to = -2;
        switch (event.getSlotType()) {
            case QUICKBAR: from = 9; to = 35; break;
            case CONTAINER: from = 0; to = 8; break;
        }
        for (int i = from; i <= to; i++) {
            if (event.getWhoClicked().getInventory().getItem(i) == null) {
                return i;
            }
        }
        return -1;
    }
}
