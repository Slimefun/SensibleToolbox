package me.desht.sensibletoolbox.listeners;

import com.google.common.base.Joiner;
import me.desht.dhutils.Debugger;
import me.desht.sensibletoolbox.SensibleToolboxPlugin;
import me.desht.sensibletoolbox.api.STBBlock;
import me.desht.sensibletoolbox.api.STBItem;
import me.desht.sensibletoolbox.blocks.BaseSTBBlock;
import me.desht.sensibletoolbox.energynet.EnergyNetManager;
import me.desht.sensibletoolbox.gui.InventoryGUI;
import me.desht.sensibletoolbox.gui.STBGUIHolder;
import me.desht.sensibletoolbox.items.BaseSTBItem;
import me.desht.sensibletoolbox.recipes.CustomRecipeManager;
import me.desht.sensibletoolbox.storage.LocationManager;
import me.desht.sensibletoolbox.util.STBUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Furnace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.*;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Lever;
import org.bukkit.material.Sign;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.Iterator;

public class GeneralListener extends STBBaseListener {
	private static final String LAST_PISTON_EXTEND = "STB_Last_Piston_Extend";

	public GeneralListener(SensibleToolboxPlugin plugin) {
		super(plugin);
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		BaseSTBItem item = BaseSTBItem.getItemFromItemStack(event.getItem());
		if (item != null) {
			item.onInteractItem(event);
		}
		Block clicked = event.getClickedBlock();
		if (!event.isCancelled() && clicked != null) {
			if (clicked.getType() == Material.SIGN_POST || clicked.getType() == Material.WALL_SIGN) {
				Sign sign = (Sign) clicked.getState().getData();
				clicked = clicked.getRelative(sign.getAttachedFace());
			}
			BaseSTBBlock stb = LocationManager.getManager().get(clicked.getLocation());
			if (stb != null) {
				stb.onInteractBlock(event);
			}
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onPlayerInteract(PlayerInteractEntityEvent event) {
		ItemStack stack = event.getPlayer().getItemInHand();
		BaseSTBItem item = BaseSTBItem.getItemFromItemStack(stack);
		if (item != null) {
			item.onInteractEntity(event);
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
		ItemStack stack = event.getPlayer().getItemInHand();
		BaseSTBItem item = BaseSTBItem.getItemFromItemStack(stack);
		if (item != null) {
			item.onItemConsume(event);
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onItemChanged(PlayerItemHeldEvent event) {
		if (event.getPlayer().isSneaking()) {
			ItemStack stack = event.getPlayer().getItemInHand();
			BaseSTBItem item = BaseSTBItem.getItemFromItemStack(stack);
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
			stb.onBlockDamage(event);
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onBlockPlace(BlockPlaceEvent event) {
		STBItem item =  BaseSTBItem.getItemFromItemStack(event.getItemInHand());
		if (item instanceof STBBlock) {
			((BaseSTBBlock) item).onBlockPlace(event);
		} else if (item != null) {
			// prevent placing of non-block STB items, even if they use a block material (e.g. bag of holding)
			event.setCancelled(true);
		}
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onCablePlace(BlockPlaceEvent event) {
		if (EnergyNetManager.isCable(event.getBlock())) {
			EnergyNetManager.onCablePlaced(event.getBlock());
		}
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onBlockBreak(BlockBreakEvent event) {
		if (EnergyNetManager.isCable(event.getBlock())) {
			EnergyNetManager.onCableRemoved(event.getBlock());
		} else {
			boolean isCancelled = event.isCancelled();
			BaseSTBItem item = BaseSTBItem.getItemFromItemStack(event.getPlayer().getItemInHand());
			if (item != null) {
				item.onBreakBlockWithItem(event);
			}
			BaseSTBBlock stb = LocationManager.getManager().get(event.getBlock().getLocation());
			if (stb != null) {
				stb.onBlockBreak(event);
			}
			if (event.isCancelled() != isCancelled) {
				throw new IllegalStateException("You must not change the cancellation status of a STB block break event!");
			}
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onSignChange(SignChangeEvent event) {
		final Block b = event.getBlock();
		Sign sign = (Sign) b.getState().getData();
		Block attachedTo = b.getRelative(sign.getAttachedFace());
		BaseSTBBlock item = LocationManager.getManager().get(attachedTo.getLocation());
		if (item != null) {
			boolean ret = item.onSignChange(event);
			if (ret) {
				// pop the sign off next tick; it's done its job
				Bukkit.getScheduler().runTask(plugin, new Runnable() {
					@Override
					public void run() {
						b.setType(Material.AIR);
						b.getWorld().dropItemNaturally(b.getLocation(), new ItemStack(Material.SIGN));
					}
				});
			}
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onBlockPhysics(BlockPhysicsEvent event) {
		Block block = event.getBlock();
		BaseSTBBlock item = LocationManager.getManager().get(block.getLocation());
		if (item != null) {
			item.onBlockPhysics(event);
		} else {
			if (block.getType() == Material.LEVER) {
				Lever l = (Lever) block.getState().getData();
				item = LocationManager.getManager().get(block.getRelative(l.getAttachedFace()).getLocation());
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
					stb.breakBlock(b);
				}
				iter.remove();
			}
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onPrepareItemCraft(PrepareItemCraftEvent event) {
		Debugger.getInstance().debug("resulting item: " + event.getInventory().getResult());

		// prevent STB items being used where the vanilla material is expected
		// (e.g. 4 gold dust can't make a glowstone block even though gold dust uses glowstone dust for its material)
		for (ItemStack ingredient : event.getInventory().getMatrix()) {
			STBItem item = BaseSTBItem.getItemFromItemStack(ingredient);
			if (item != null && !item.isIngredientFor(event.getRecipe().getResult())) {
				Debugger.getInstance().debug(item + " is not an ingredient for " + event.getRecipe().getResult());
				event.getInventory().setResult(null);
				break;
			}
		}

		// and ensure vanilla items can't be used in place of custom STB ingredients
		// (e.g. paper can't be used to craft item router modules, even though a blank module uses paper for its material)
		STBItem result = BaseSTBItem.getItemFromItemStack(event.getRecipe().getResult());
		if (result != null) {
			for (ItemStack ingredient : event.getInventory().getMatrix()) {
				if (ingredient != null) {
					Class<? extends STBItem> c = result.getCraftingRestriction(ingredient.getType());
					if (c != null && !BaseSTBItem.isSTBItem(ingredient, c)) {
						Debugger.getInstance().debug("stopped crafting of " + result + " with vanilla item: " + ingredient.getType());
						event.getInventory().setResult(null);
						break;
					}
				}
			}
		}
		Debugger.getInstance().debug("resulting item now: " + event.getInventory().getResult());
	}

	@EventHandler
	public void onBurn(FurnaceBurnEvent event) {
		// for those STB items which can be smelted, ensure the vanilla item using
		// that material can't also be smelted (a furnace recipe will have been added previously)
		Block b = event.getBlock();
		Furnace f = (Furnace) b.getState();
		ItemStack stack = f.getInventory().getSmelting();
		if (!CustomRecipeManager.validateCustomSmelt(stack)) {
			b.getLocation().getWorld().dropItemNaturally(b.getLocation(), stack);
			f.getInventory().setSmelting(null);
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onArmourEquipCheck(InventoryClickEvent event) {
		if (event.getInventory().getType() == InventoryType.CRAFTING) {
			if (event.getSlotType() == InventoryType.SlotType.QUICKBAR || event.getSlotType() == InventoryType.SlotType.CONTAINER) {
				if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY && STBUtil.isWearable(event.getCurrentItem().getType())) {
					STBItem item = BaseSTBItem.getItemFromItemStack(event.getCurrentItem());
					if (item != null && !item.isWearable()) {
						event.setCancelled(true);
						// TODO: move it between hot bar and main inventory instead of just cancelling
					}
				}
			} else if (event.getSlotType() == InventoryType.SlotType.ARMOR) {
				STBItem item = BaseSTBItem.getItemFromItemStack(event.getCursor());
				if (item != null && !item.isWearable()) {
					event.setCancelled(true);
				}
			}
		}
	}

	@EventHandler
	public void onArmourEquipCheck(InventoryDragEvent event) {
		if (event.getInventory().getType() == InventoryType.CRAFTING && STBUtil.isWearable(event.getOldCursor().getType())) {
			for (int slot : event.getRawSlots()) {
				if (slot >= 5 && slot <= 8) {
					// armour slots
					STBItem item = BaseSTBItem.getItemFromItemStack(event.getOldCursor());
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
			STBItem item = BaseSTBItem.getItemFromItemStack(event.getItem());
			if (item != null && !item.isWearable()) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void onGUIInventoryClick(InventoryClickEvent event) {
		if (event.getInventory().getHolder() instanceof STBGUIHolder) {
			((STBGUIHolder) event.getInventory().getHolder()).getGUI().receiveEvent(event);
		} else if (event.getInventory().getHolder() instanceof Player) {
			InventoryGUI gui = InventoryGUI.getOpenGUI((Player) event.getInventory().getHolder());
			if (gui != null) {
				gui.receiveEvent(event);
			}
		}
	}

	@EventHandler
	public void onGUIInventoryDrag(InventoryDragEvent event) {
		if (event.getInventory().getHolder() instanceof STBGUIHolder) {
			((STBGUIHolder) event.getInventory().getHolder()).getGUI().receiveEvent(event);
		} else if (event.getInventory().getHolder() instanceof Player) {
			InventoryGUI gui = InventoryGUI.getOpenGUI((Player) event.getInventory().getHolder());
			if (gui != null) {
				gui.receiveEvent(event);
			}
		}
	}

	@EventHandler
	public void onGUIInventoryClose(InventoryCloseEvent event) {
		if (event.getInventory().getHolder() instanceof STBGUIHolder) {
			((STBGUIHolder) event.getInventory().getHolder()).getGUI().receiveEvent(event);
		} else if (event.getInventory().getHolder() instanceof Player) {
			InventoryGUI gui = InventoryGUI.getOpenGUI((Player) event.getInventory().getHolder());
			if (gui != null) {
				gui.receiveEvent(event);
			}
		}
	}

	@EventHandler
	public void onPrepareItemEnchant(PrepareItemEnchantEvent event) {
		STBItem item = BaseSTBItem.getItemFromItemStack(event.getItem());
		if (item != null && !item.isEnchantable()) {
			event.setCancelled(true);
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onPistonExtend(BlockPistonExtendEvent event) {

		// work around CB bug where event is called multiple times for a block
		Long when = (Long) STBUtil.getMetadataValue(event.getBlock(), LAST_PISTON_EXTEND);
		long now = System.currentTimeMillis();
		if (when != null && now - when < 50) {  // 50 ms = 1 tick
			return;
		}
		event.getBlock().setMetadata(LAST_PISTON_EXTEND, new FixedMetadataValue(plugin, now));

		LOOP: for (int i = event.getLength(); i > 0; i--) {
			final Block moving = event.getBlock().getRelative(event.getDirection(), i);
			final Block to = moving.getRelative(event.getDirection());
			final BaseSTBBlock stb = LocationManager.getManager().get(moving.getLocation());
			if (stb != null) {
				switch (stb.getPistonMoveReaction()) {
					case MOVE:
						// this has to be deferred, because it's possible that this piston extension was caused
						// by a STB block ticking, and modifying the tickers list directly would throw a CME
						Bukkit.getScheduler().runTask(plugin, new Runnable() {
							@Override
							public void run() {
								LocationManager.getManager().moveBlock(stb, moving.getLocation(), to.getLocation());
							}
						});
						break;
					case BLOCK:
						event.setCancelled(true);
						break LOOP; // if this one blocks, all subsequent blocks do too
					case BREAK:
						stb.breakBlock(moving);
						break;
				}
			}
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onPistonRetract(final BlockPistonRetractEvent event) {
		if (event.isSticky()) {
			final BaseSTBBlock stb = LocationManager.getManager().get(event.getRetractLocation());
			if (stb != null) {
				switch (stb.getPistonMoveReaction()) {
					case MOVE:
						BlockFace dir = event.getDirection().getOppositeFace();
						final Location to = event.getRetractLocation().add(dir.getModX(), dir.getModY(), dir.getModZ());
						Bukkit.getScheduler().runTask(plugin, new Runnable() {
							@Override
							public void run() {
								LocationManager.getManager().moveBlock(stb, event.getRetractLocation(), to);
							}
						});
						break;
					case BLOCK:
						event.setCancelled(true);
						break;
					case BREAK:
						stb.breakBlock(event.getRetractLocation().getBlock());
						break;
				}
			}
		}
	}
}
