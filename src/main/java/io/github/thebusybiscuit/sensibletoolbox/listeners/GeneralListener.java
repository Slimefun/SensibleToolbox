package io.github.thebusybiscuit.sensibletoolbox.listeners;

import java.util.Iterator;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import io.github.thebusybiscuit.sensibletoolbox.SensibleToolboxPlugin;
import io.github.thebusybiscuit.sensibletoolbox.api.SensibleToolbox;
import io.github.thebusybiscuit.sensibletoolbox.api.energy.Chargeable;
import io.github.thebusybiscuit.sensibletoolbox.api.gui.STBGUIHolder;
import io.github.thebusybiscuit.sensibletoolbox.api.items.BaseSTBBlock;
import io.github.thebusybiscuit.sensibletoolbox.api.items.BaseSTBItem;
import io.github.thebusybiscuit.sensibletoolbox.api.items.ItemAction;
import io.github.thebusybiscuit.sensibletoolbox.core.gui.STBInventoryGUI;
import io.github.thebusybiscuit.sensibletoolbox.core.storage.LocationManager;
import io.github.thebusybiscuit.sensibletoolbox.utils.STBUtil;
import me.desht.dhutils.Debugger;
import me.desht.dhutils.text.LogUtils;

public class GeneralListener extends STBBaseListener {

    private static final String LAST_PISTON_EXTEND = "STB_Last_Piston_Extend";

    public GeneralListener(SensibleToolboxPlugin plugin) {
        super(plugin);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        BaseSTBItem item = SensibleToolbox.getItemRegistry().fromItemStack(event.getItem());

        if (item != null && item.checkPlayerPermission(event.getPlayer(), ItemAction.INTERACT)) {
            item.onInteractItem(event);
        }

        Block clicked = event.getClickedBlock();
        if (event.useInteractedBlock() != Result.DENY && clicked != null) {
            BaseSTBBlock stb = LocationManager.getManager().get(clicked.getLocation(), true);

            if (stb != null && stb.checkPlayerPermission(event.getPlayer(), ItemAction.INTERACT_BLOCK)) {
                stb.onInteractBlock(event);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEntityEvent event) {
        ItemStack stack = event.getPlayer().getInventory().getItemInMainHand();
        BaseSTBItem item = SensibleToolbox.getItemRegistry().fromItemStack(stack);
        if (item != null) {
            item.onInteractEntity(event);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
        ItemStack stack = event.getItem();
        BaseSTBItem item = SensibleToolbox.getItemRegistry().fromItemStack(stack);

        if (item != null) {
            item.onItemConsume(event);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onItemChanged(PlayerItemHeldEvent event) {
        if (event.getPlayer().isSneaking()) {
            ItemStack stack = event.getPlayer().getInventory().getItemInMainHand();
            BaseSTBItem item = SensibleToolbox.getItemRegistry().fromItemStack(stack);

            if (item != null) {
                item.onItemHeld(event);
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockDamage(BlockDamageEvent event) {
        BaseSTBBlock stb = LocationManager.getManager().get(event.getBlock().getLocation());
        if (stb != null) {
            if (stb.checkPlayerPermission(event.getPlayer(), ItemAction.BREAK)) {
                stb.onBlockDamage(event);
            } else {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onBlockPrePlaceCheck(BlockPlaceEvent event) {
        BaseSTBItem stb = SensibleToolbox.getItemRegistry().fromItemStack(event.getItemInHand());
        if (stb != null) {
            if (!(stb instanceof BaseSTBBlock)) {
                event.setCancelled(true);
            } else if (!stb.checkPlayerPermission(event.getPlayer(), ItemAction.PLACE)) {
                event.setCancelled(true);
            } else if (!((BaseSTBBlock) stb).validatePlaceable(event.getBlock().getLocation())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onBlockWillPlace(BlockPlaceEvent event) {
        BaseSTBItem stb = SensibleToolbox.getItemRegistry().fromItemStack(event.getItemInHand());
        if (stb != null) {
            // sanity check: we should only get here if the item is an STB block, since
            // onBlockPrePlaceCheck() will have cancelled the event if it wasn't
            Validate.isTrue(stb instanceof BaseSTBBlock, "trying to place a non-block STB item? " + stb.getItemTypeID());
            ((BaseSTBBlock) stb).placeBlock(event.getBlock(), event.getPlayer(), STBUtil.getFaceFromYaw(event.getPlayer().getLocation().getYaw()).getOppositeFace());

            if (event.isCancelled()) {
                throw new IllegalStateException("You must not change the cancellation status of a STB block place event!");
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onBlockMayBurnAway(BlockBurnEvent event) {
        BaseSTBBlock stb = LocationManager.getManager().get(event.getBlock().getLocation());
        if (stb != null && !stb.isFlammable()) {
            event.setCancelled(true);
            for (BlockFace face : STBUtil.DIRECT_BLOCK_FACES) {
                Block b = event.getBlock().getRelative(face);
                if (b.getType() == Material.FIRE && ThreadLocalRandom.current().nextInt(3) != 0) {
                    b.setType(Material.AIR);
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onBlockBurntAway(BlockBurnEvent event) {
        BaseSTBBlock stb = LocationManager.getManager().get(event.getBlock().getLocation());
        if (stb != null) {
            if (!stb.isFlammable()) {
                LogUtils.warning("Non-flammable STB block " + stb + " was not protected from flame?");
            }
            stb.breakBlock(false);
            stb.onBlockBurnt(event);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onCablePlace(BlockPlaceEvent event) {
        if (STBUtil.isCable(event.getBlock())) {
            plugin.getEnergyNetManager().onCablePlaced(event.getBlock());
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onBlockBreak(BlockBreakEvent event) {
        if (STBUtil.isCable(event.getBlock())) {
            plugin.getEnergyNetManager().onCableRemoved(event.getBlock());
        } else {
            ItemStack stack = event.getPlayer().getInventory().getItemInMainHand();

            BaseSTBItem item = SensibleToolbox.getItemRegistry().fromItemStack(stack);

            if (item != null) {
                item.onBreakBlockWithItem(event);
            }

            BaseSTBBlock stb = LocationManager.getManager().get(event.getBlock().getLocation());

            if (stb != null) {
                stb.breakBlock(true);
            }

            if (event.isCancelled()) {
                throw new IllegalStateException("You must not change the cancellation status of a STB block break event!");
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onSignChange(SignChangeEvent event) {
        if (Tag.WALL_SIGNS.isTagged(event.getBlock().getType())) {
            Block b = event.getBlock();
            WallSign sign = (WallSign) b.getBlockData();

            Block attachedTo = b.getRelative(sign.getFacing());
            BaseSTBBlock item = LocationManager.getManager().get(attachedTo.getLocation());

            if (item != null) {
                boolean ret = item.onSignChange(event);

                if (ret) {
                    // pop the sign off next tick; it's done its job
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        b.setType(Material.AIR);
                        b.getWorld().dropItemNaturally(b.getLocation(), new ItemStack(b.getType()));
                    });
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onLabelSignBroken(BlockBreakEvent event) {
        if (Tag.WALL_SIGNS.isTagged(event.getBlock().getType())) {
            WallSign sign = (WallSign) event.getBlock().getBlockData();
            Block b2 = event.getBlock().getRelative(sign.getFacing().getOppositeFace());
            BaseSTBBlock stb = LocationManager.getManager().get(b2.getLocation());

            if (stb != null) {
                stb.detachLabelSign(sign.getFacing());
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPhysics(BlockPhysicsEvent event) {
        Block block = event.getBlock();
        BaseSTBBlock item = LocationManager.getManager().get(block.getLocation());
        if (item != null) {
            item.handlePhysicsEvent(event);
        } else {
            if (block.getType() == Material.LEVER) {
                Directional l = (Directional) block.getState().getData();
                item = LocationManager.getManager().get(block.getRelative(l.getFacing()).getLocation());

                if (item != null) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onFlow(BlockFromToEvent event) {
        BaseSTBBlock item = LocationManager.getManager().get(event.getToBlock().getLocation());

        if (item != null) {
            // this prevents things like the carpet layer on a solar cell being washed off
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        Iterator<Block> iter = event.blockList().iterator();
        while (iter.hasNext()) {
            Block b = iter.next();
            BaseSTBBlock stb = LocationManager.getManager().get(b.getLocation());

            if (stb != null) {
                if (stb.onEntityExplode(event)) {
                    stb.breakBlock(ThreadLocalRandom.current().nextInt(100) < plugin.getConfig().getInt("explode_item_drop_chance"));
                }
                iter.remove();
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPrepareItemCraft(PrepareItemCraftEvent event) {
        Debugger.getInstance().debug("resulting item: " + event.getInventory().getResult());

        if (event.getRecipe() == null) {
            return;
        }

        BaseSTBItem result = SensibleToolbox.getItemRegistry().fromItemStack(event.getRecipe().getResult());
        if (result != null) {
            // ensure that everyone viewing the crafting inventory has permission to craft the item
            for (HumanEntity he : event.getViewers()) {
                if (he instanceof Player && !result.checkPlayerPermission((Player) he, ItemAction.CRAFT)) {
                    event.getInventory().setResult(null);
                    return;
                }
            }
            if (!result.validateCrafting(event.getInventory())) {
                event.getInventory().setResult(null);
                return;
            }
        }

        double finalSCU = 0.0;

        // prevent STB items being used where the vanilla material is expected
        // (e.g. 4 gold dust can't make a glowstone block even though gold dust uses glowstone dust for its material)
        for (ItemStack ingredient : event.getInventory().getMatrix()) {
            BaseSTBItem item = SensibleToolbox.getItemRegistry().fromItemStack(ingredient);
            if (item != null) {
                if (!item.isIngredientFor(event.getRecipe().getResult())) {
                    Debugger.getInstance().debug(item + " is not an ingredient for " + event.getRecipe().getResult());
                    event.getInventory().setResult(null);
                    break;
                } else if (item instanceof Chargeable && result instanceof Chargeable) {
                    // add the ingredient's charge to the final item's charge
                    finalSCU += ((Chargeable) item).getCharge();
                }
            }
        }

        if (finalSCU > 0) {
            Chargeable c = (Chargeable) result;
            c.setCharge(Math.min(c.getMaxCharge(), finalSCU));
            event.getInventory().setResult(result.toItemStack());
        }

        // and ensure vanilla items can't be used in place of custom STB ingredients
        // (e.g. paper can't be used to craft item router modules, even though a blank module uses paper for its
        // material)
        if (result != null) {
            for (ItemStack ingredient : event.getInventory().getMatrix()) {
                if (ingredient != null) {
                    Class<? extends BaseSTBItem> c = result.getCraftingRestriction(ingredient.getType());
                    if (c != null && !SensibleToolbox.getItemRegistry().isSTBItem(ingredient, c)) {
                        Debugger.getInstance().debug("stopped crafting of " + result + " with vanilla item: " + ingredient.getType());
                        event.getInventory().setResult(null);
                        break;
                    }
                }
            }
            if (result.onCrafted()) {
                event.getInventory().setResult(result.toItemStack(event.getInventory().getResult().getAmount()));
            }
        }
        Debugger.getInstance().debug("resulting item now: " + event.getInventory().getResult());
    }

    @EventHandler
    public void onArmourEquipCheck(InventoryClickEvent event) {
        if (event.getInventory().getType() == InventoryType.CRAFTING) {
            if (event.getSlotType() == InventoryType.SlotType.QUICKBAR || event.getSlotType() == InventoryType.SlotType.CONTAINER) {
                if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY && STBUtil.isWearable(event.getCurrentItem().getType())) {
                    BaseSTBItem item = SensibleToolbox.getItemRegistry().fromItemStack(event.getCurrentItem());
                    if (item != null && !item.isWearable()) {
                        event.setCancelled(true);
                        int newSlot = findNewSlot(event);
                        if (newSlot >= 0) {
                            event.getWhoClicked().getInventory().setItem(newSlot, event.getCurrentItem());
                            event.setCurrentItem(null);
                        }
                    }
                }
            } else if (event.getSlotType() == InventoryType.SlotType.ARMOR) {
                BaseSTBItem item = SensibleToolbox.getItemRegistry().fromItemStack(event.getCursor());
                if (item != null && !item.isWearable()) {
                    event.setCancelled(true);
                }
            }
        }
    }

    private int findNewSlot(InventoryClickEvent event) {
        int from = -1;
        int to = -2;

        switch (event.getSlotType()) {
        case QUICKBAR:
            from = 9;
            to = 35;
            break;
        case CONTAINER:
            from = 0;
            to = 8;
            break;
        default:
            break;
        }

        for (int i = from; i <= to; i++) {
            if (event.getWhoClicked().getInventory().getItem(i) == null) {
                return i;
            }
        }

        return -1;
    }

    @EventHandler
    public void onArmourEquipCheck(InventoryDragEvent event) {
        if (event.getInventory().getType() == InventoryType.CRAFTING && STBUtil.isWearable(event.getOldCursor().getType())) {
            for (int slot : event.getRawSlots()) {
                if (slot >= 5 && slot <= 8) {
                    // armour slots
                    BaseSTBItem item = SensibleToolbox.getItemRegistry().fromItemStack(event.getOldCursor());

                    if (item != null && !item.isWearable()) {
                        event.setCancelled(true);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onArmourEquipCheck(BlockDispenseEvent event) {
        if (STBUtil.isWearable(event.getItem().getType())) {
            BaseSTBItem item = SensibleToolbox.getItemRegistry().fromItemStack(event.getItem());

            if (item != null && !item.isWearable()) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onGUIInventoryClick(InventoryClickEvent event) {
        STBInventoryGUI gui = getGUIForInventoryEvent(event);

        if (gui != null) {
            gui.receiveEvent(event);
        }
    }

    @EventHandler
    public void onGUIInventoryDrag(InventoryDragEvent event) {
        STBInventoryGUI gui = getGUIForInventoryEvent(event);

        if (gui != null) {
            gui.receiveEvent(event);
        }
    }

    @EventHandler
    public void onGUIInventoryClose(InventoryCloseEvent event) {
        STBInventoryGUI gui = getGUIForInventoryEvent(event);

        if (gui != null) {
            gui.receiveEvent(event);
        }
    }

    private STBInventoryGUI getGUIForInventoryEvent(InventoryEvent event) {
        if (event.getInventory().getHolder() instanceof STBGUIHolder) {
            return (STBInventoryGUI) ((STBGUIHolder) event.getInventory().getHolder()).getGUI();
        } else if (event.getInventory().getHolder() instanceof Player) {
            return (STBInventoryGUI) STBInventoryGUI.getOpenGUI((Player) event.getInventory().getHolder());
        } else {
            return null;
        }
    }

    @EventHandler
    public void onPrepareItemEnchant(PrepareItemEnchantEvent event) {
        BaseSTBItem item = SensibleToolbox.getItemRegistry().fromItemStack(event.getItem());

        if (item != null && !item.isEnchantable()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPistonExtend(BlockPistonExtendEvent event) {
        // work around CB bug where event is called multiple times for a block
        Long when = (Long) STBUtil.getMetadataValue(event.getBlock(), LAST_PISTON_EXTEND);
        long now = System.currentTimeMillis();

        if (when != null && now - when < 50) { // 50 ms = 1 tick
            return;
        }

        event.getBlock().setMetadata(LAST_PISTON_EXTEND, new FixedMetadataValue(plugin, now));

        for (int i = event.getBlocks().size(); i > 0; i--) {
            Block moving = event.getBlock().getRelative(event.getDirection(), i);
            Block to = moving.getRelative(event.getDirection());
            BaseSTBBlock stb = LocationManager.getManager().get(moving.getLocation());
            if (stb != null) {
                switch (stb.getPistonMoveReaction()) {
                case MOVE:
                    // this has to be deferred, because it's possible that this piston extension was caused
                    // by a STB block ticking, and modifying the tickers list directly would throw a CME
                    Bukkit.getScheduler().runTask(plugin, () -> LocationManager.getManager().moveBlock(stb, moving.getLocation(), to.getLocation()));
                    break;
                case BLOCK:
                    event.setCancelled(true);
                    return; // if this one blocks, all subsequent blocks do too
                case BREAK:
                    stb.breakBlock(true);
                    break;
                default:
                    break;
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPistonRetract(BlockPistonRetractEvent event) {
        if (event.isSticky()) {
            BaseSTBBlock stb = LocationManager.getManager().get(event.getRetractLocation());

            if (stb != null) {
                switch (stb.getPistonMoveReaction()) {
                case MOVE:
                    BlockFace dir = event.getDirection().getOppositeFace();
                    Location to = event.getRetractLocation().add(dir.getModX(), dir.getModY(), dir.getModZ());
                    Bukkit.getScheduler().runTask(plugin, () -> LocationManager.getManager().moveBlock(stb, event.getRetractLocation(), to));
                    break;
                case BLOCK:
                    event.setCancelled(true);
                    break;
                case BREAK:
                    stb.breakBlock(true);
                    break;
                default:
                    break;
                }
            }
        }
    }
}
