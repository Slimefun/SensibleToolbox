package me.desht.sensibletoolbox.listeners;

import me.desht.dhutils.Debugger;
import me.desht.sensibletoolbox.blocks.BaseSTBBlock;
import me.desht.sensibletoolbox.energynet.EnergyNetManager;
import me.desht.sensibletoolbox.gui.InventoryGUI;
import me.desht.sensibletoolbox.gui.STBGUIHolder;
import me.desht.sensibletoolbox.items.BaseSTBItem;
import me.desht.sensibletoolbox.SensibleToolboxPlugin;
import me.desht.sensibletoolbox.items.energycells.EnergyCell;
import me.desht.sensibletoolbox.storage.LocationManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
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
import org.bukkit.inventory.*;
import org.bukkit.material.Lever;
import org.bukkit.material.Sign;

import java.util.Iterator;

public class GeneralListener extends STBBaseListener {
	public GeneralListener(SensibleToolboxPlugin plugin) {
		super(plugin);
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		ItemStack stack = event.getPlayer().getItemInHand();
		BaseSTBItem item = BaseSTBItem.getItemFromItemStack(stack);
		if (item != null) {
			item.onInteractItem(event);
		}
		if (!event.isCancelled() && event.getClickedBlock() != null) {
			BaseSTBBlock stb = LocationManager.getManager().get(event.getClickedBlock().getLocation());
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
			item.handleEntityInteraction(event);
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
		BaseSTBItem item =  BaseSTBItem.getItemFromItemStack(event.getItemInHand());
		if (item instanceof BaseSTBBlock) {
			((BaseSTBBlock) item).onBlockPlace(event);
		} else if (item != null) {
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
			BaseSTBBlock stb = LocationManager.getManager().get(event.getBlock().getLocation());
			if (stb != null) {
				boolean isCancelled = event.isCancelled();
				stb.onBlockBreak(event);
				if (event.isCancelled() != isCancelled) {
					throw new IllegalStateException("You must not change the cancellation status of a STB block break event!");
				}
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

		// prevent STB items being used in vanilla crafting recipes
		// (e.g. 4 gold dust can't make a glowstone block even though gold dust uses glowstone dust for its material)
		for (ItemStack ingredient : event.getInventory().getMatrix()) {
			BaseSTBItem item = BaseSTBItem.getItemFromItemStack(ingredient);
			if (item != null && !item.isIngredientFor(event.getRecipe().getResult())) {
				Debugger.getInstance().debug(item + " is not an ingredient for " + event.getRecipe().getResult());
				event.getInventory().setResult(null);
				break;
			}
		}

		// and ensure vanilla items can't be used in place of custom STB ingredients
		// (e.g. paper can't be used to craft item router modules, even though a blank module uses paper)
		BaseSTBItem result = BaseSTBItem.getItemFromItemStack(event.getRecipe().getResult());
		if (result != null) {
			for (ItemStack ingredient : event.getInventory().getMatrix()) {
				if (ingredient != null) {
					Class<? extends BaseSTBItem> c = result.getCraftingRestriction(ingredient.getType());
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
		Class<? extends BaseSTBItem> klass = BaseSTBItem.getCustomSmelt(stack.getType());
		if (klass != null) {
			BaseSTBItem item = BaseSTBItem.getItemFromItemStack(stack);
			if (!klass.isInstance(item)) {
				Debugger.getInstance().debug("stopped smelting of vanilla item: " + stack);
				b.getLocation().getWorld().dropItemNaturally(b.getLocation(), stack);
				f.getInventory().setSmelting(null);
				event.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void onEquipEnergyCell(InventoryClickEvent event) {
		if (event.getInventory().getType() == InventoryType.CRAFTING) {
			if (event.getSlotType() == InventoryType.SlotType.QUICKBAR || event.getSlotType() == InventoryType.SlotType.CONTAINER) {
				if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
					if (BaseSTBItem.isSTBItem(event.getCurrentItem(), EnergyCell.class)) {
						event.setCancelled(true); // no shift-clicking a energy cell into the helmet slot
					}
				}
			} else if (event.getSlotType() == InventoryType.SlotType.ARMOR) {
				if (BaseSTBItem.isSTBItem(event.getCursor(), EnergyCell.class)) {
					event.setCancelled(true); // no placing an energy cell into the helmet slot
				}
			}
		}
	}

	@EventHandler
	public void onEquipEnergyCell(BlockDispenseEvent event) {
		if (BaseSTBItem.isSTBItem(event.getItem(), EnergyCell.class)) {
			event.setCancelled(true); // no dispensing energy cells (as armour item)
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
		BaseSTBItem item = BaseSTBItem.getItemFromItemStack(event.getItem());
		if (item != null && !item.isEnchantable()) {
			event.setCancelled(true);
		}
	}
}
