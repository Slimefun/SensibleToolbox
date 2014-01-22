package me.desht.sensibletoolbox.listeners;

import me.desht.sensibletoolbox.blocks.BaseSTBBlock;
import me.desht.sensibletoolbox.items.BaseSTBItem;
import me.desht.sensibletoolbox.SensibleToolboxPlugin;
import me.desht.sensibletoolbox.items.IronDust;
import me.desht.sensibletoolbox.storage.LocationManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Furnace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.*;
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
			item.handleItemInteraction(event);
		}
		if (event.getClickedBlock() != null && !event.getPlayer().isSneaking()) {
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
			item.handleConsume(event);
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onItemChanged(PlayerItemHeldEvent event) {
		if (event.getPlayer().isSneaking()) {
			ItemStack stack = event.getPlayer().getItemInHand();
			BaseSTBItem item = BaseSTBItem.getItemFromItemStack(stack);
			if (item != null) {
				item.handleMouseWheel(event);
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onBlockDamage(BlockDamageEvent event) {
		BaseSTBBlock item = LocationManager.getManager().get(event.getBlock().getLocation());
		if (item != null) {
			item.onBlockDamage(event);
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onBlockPlace(BlockPlaceEvent event) {
		BaseSTBItem item = BaseSTBItem.getItemFromItemStack(event.getItemInHand());
		if (item != null && item instanceof BaseSTBBlock) {
			((BaseSTBBlock)item).onBlockPlace(event);
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onBlockBreak(BlockBreakEvent event) {
		BaseSTBBlock item = LocationManager.getManager().get(event.getBlock().getLocation());
		if (item != null) {
			item.onBlockBreak(event);
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
	public void onItemCraft(CraftItemEvent event) {
		// prevent STB items being used in vanilla crafting recipes
		for (ItemStack stack : event.getInventory().getMatrix()) {
			if (BaseSTBItem.isSTBItem(stack)) {
				event.setCancelled(true);
				break;
			}
		}
	}

	@EventHandler
	public void onBurn(FurnaceBurnEvent event) {
		System.out.println("furnace burn!");
		Block b = event.getBlock();
		Furnace f = (Furnace) b.getState();
		ItemStack stack = f.getInventory().getSmelting();
		BaseSTBItem item = BaseSTBItem.getItemFromItemStack(stack);
		Class<? extends BaseSTBItem> klass = BaseSTBItem.getCustomSmelt(stack.getType());
		if (klass != null && !klass.isInstance(item)) {
			System.out.println("no smelting vanilla item: " + stack);
			b.getLocation().getWorld().dropItemNaturally(b.getLocation(), stack);
			f.getInventory().setSmelting(null);
			event.setCancelled(true);
		}
	}
}
