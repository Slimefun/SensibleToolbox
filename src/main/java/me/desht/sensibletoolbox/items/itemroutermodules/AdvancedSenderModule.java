package me.desht.sensibletoolbox.items.itemroutermodules;

import me.desht.dhutils.MiscUtil;
import me.desht.sensibletoolbox.blocks.ItemRouter;
import me.desht.sensibletoolbox.items.BaseSTBItem;
import me.desht.sensibletoolbox.storage.LocationManager;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.material.Dye;
import org.bukkit.material.MaterialData;

public class AdvancedSenderModule extends DirectionalItemRouterModule {
	private static final int RANGE = 24;
	private static final Dye md = makeDye(DyeColor.LIGHT_BLUE);
	private Location linkedLoc;

	public AdvancedSenderModule() {
		linkedLoc = null;
	}

	public AdvancedSenderModule(ConfigurationSection conf) {
		super(conf);
		if (conf.contains("linkedLoc")) {
			try {
				linkedLoc = MiscUtil.parseLocation(conf.getString("linkedLoc"));
			} catch (IllegalArgumentException e) {
				linkedLoc = null;
			}
		}
	}

	@Override
	public YamlConfiguration freeze() {
		YamlConfiguration conf = super.freeze();
		if (linkedLoc != null) {
			conf.set("linkedLoc", MiscUtil.formatLocation(linkedLoc));
		}
		return conf;
	}

	@Override
	public MaterialData getMaterialData() {
		return md;
	}

	@Override
	public String getItemName() {
		return "I.R. Mod: Adv. Sender";
	}

	@Override
	public String[] getLore() {
		return new String[] {
				"Insert into an Item Router",
				"Sends items to a linked Receiver Module",
				" anywhere within a " + RANGE + "-block radius",
				" (line of sight is not needed)",
				"L-Click item router with installed",
				" Receiver Module: " + ChatColor.RESET + " Link Adv. Sender",
				"⇧ + L-Click anywhere: " + ChatColor.RESET + " Unlink Adv. Sender"
		};
	}

	@Override
	public String getDisplaySuffix() {
		if (linkedLoc == null) {
			return "[Not Linked]";
		} else {
			String prefix = "";
			if (getOwner() != null) {
				Location loc = getOwner().getLocation();
				if (loc.getWorld() != linkedLoc.getWorld() || loc.distanceSquared(linkedLoc) > RANGE * RANGE) {
					prefix = ChatColor.RED.toString() + ChatColor.ITALIC;
				}
			} else if (LocationManager.getManager().get(linkedLoc, ItemRouter.class) == null) {
				prefix = ChatColor.RED.toString() + ChatColor.STRIKETHROUGH;
			}
			return prefix + "[" + MiscUtil.formatLocation(linkedLoc) + "]";
		}
	}

	@Override
	public Recipe getRecipe() {
		SenderModule sm = new SenderModule();
		registerCustomIngredients(sm);
		ShapelessRecipe recipe = new ShapelessRecipe(toItemStack());
		recipe.addIngredient(sm.getMaterialData());
		recipe.addIngredient(Material.EYE_OF_ENDER);
		recipe.addIngredient(Material.DIAMOND);
		return recipe;
	}

	@Override
	public void onInteractItem(PlayerInteractEvent event) {
		if (event.getAction() == Action.LEFT_CLICK_BLOCK && !event.getPlayer().isSneaking()) {
			// try to link up with a receiver module
			boolean linked = false;
			ItemRouter rtr = LocationManager.getManager().get(event.getClickedBlock().getLocation(), ItemRouter.class);
			if (rtr != null) {
				for (ItemRouterModule mod : rtr.getInstalledModules()) {
					if (mod instanceof ReceiverModule) {
						linkToRouter(rtr);
						event.getPlayer().setItemInHand(toItemStack(event.getPlayer().getItemInHand().getAmount()));
						linked = true;
						break;
					}
				}
			}
			event.getPlayer().playSound(event.getPlayer().getLocation(), linked ? Sound.ORB_PICKUP : Sound.NOTE_BASS, 1.0f, 1.0f);
			event.setCancelled(true);
		} else if (event.getPlayer().isSneaking() && (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK)) {
			linkToRouter(null);
			event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.NOTE_BASS, 1.0f, 1.0f);
			event.getPlayer().setItemInHand(toItemStack(event.getPlayer().getItemInHand().getAmount()));
			event.setCancelled(true);
		} else if (event.getPlayer().getItemInHand().getAmount() == 1 &&
				(event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
			super.onInteractItem(event);
		}
	}

	public void linkToRouter(ItemRouter rtr) {
		linkedLoc = rtr == null ? null : rtr.getLocation();
	}

	@Override
	public boolean execute() {
		if (getOwner() != null && getOwner().getBufferItem() != null && linkedLoc != null) {
			if (getFilter() != null && !getFilter().shouldPass(getOwner().getBufferItem())) {
				return false;
			}
			ItemRouter rtr = LocationManager.getManager().get(linkedLoc, ItemRouter.class);
			if (rtr != null) {
				Location loc1 = getOwner().getLocation();
				Location loc2 = rtr.getLocation();
				if (loc1.getWorld() != loc2.getWorld() || loc1.distanceSquared(loc2) > RANGE * RANGE) {
					return false;
				}
				for (ItemRouterModule mod : rtr.getInstalledModules()) {
					if (mod instanceof ReceiverModule) {
						int sent = sendItems((ReceiverModule) mod);
						return sent > 0;
					}
				}
			}
		}
		return false;
	}

	private int sendItems(ReceiverModule receiver) {
		int nToSend = getOwner().getStackSize();
		ItemStack toSend = getOwner().getBufferItem().clone();
		toSend.setAmount(Math.min(nToSend, toSend.getAmount()));
		int received = receiver.receiveItem(toSend);
		getOwner().reduceBuffer(received);
		return received;
	}
}
