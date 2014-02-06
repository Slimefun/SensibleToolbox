package me.desht.sensibletoolbox.items.itemroutermodules;

import me.desht.sensibletoolbox.items.filters.AbstractItemFilter;
import me.desht.sensibletoolbox.items.filters.ItemFilter;
import me.desht.sensibletoolbox.items.filters.ReverseItemFilter;
import me.desht.sensibletoolbox.util.Filter;
import me.desht.sensibletoolbox.util.STBUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;

public abstract class DirectionalItemRouterModule extends ItemRouterModule {
	private Filter filter;
	private BlockFace direction;

	public DirectionalItemRouterModule() {
		filter = null;
		setDirection(BlockFace.SELF);
	}

	public DirectionalItemRouterModule(ConfigurationSection conf) {
		super(conf);
		setDirection(BlockFace.valueOf(conf.getString("direction")));
		if (conf.contains("filtered")) {
			filter = new Filter(conf.getBoolean("filterWhitelist", true), conf.getStringList("filtered"));
		} else {
			filter = null;
		}
	}

	public YamlConfiguration freeze() {
		YamlConfiguration conf = super.freeze();
		conf.set("direction", getDirection().toString());
		if (filter != null) {
			conf.set("filterWhitelist", filter.isWhiteList());
			conf.set("filtered", filter.listFiltered());
		}
		return conf;
	}

	@Override
	public String[] getExtraLore() {
		if (filter == null) {
			return new String[0];
		} else {
			return STBUtil.formatFilterLore(filter);
		}
	}

	@Override
	public String getDisplaySuffix() {
		return direction != null && direction != BlockFace.SELF ? direction.toString() : null;
	}

	public BlockFace getDirection() {
		return direction;
	}

	public void setDirection(BlockFace direction) {
		this.direction = direction;
	}

	protected Filter getFilter() {
		return filter;
	}

	@Override
	public void onInteractItem(PlayerInteractEvent event) {
		if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
			// set module direction based on clicked block face
			setDirection(event.getBlockFace().getOppositeFace());
			System.out.println("player is holding " + event.getPlayer().getItemInHand());
			event.getPlayer().setItemInHand(toItemStack(event.getPlayer().getItemInHand().getAmount()));
			event.setCancelled(true);
		} else if (event.getPlayer().getItemInHand().getAmount() == 1 &&
				(event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
			// open module inventory to insert/remove item filters
			if (event.getClickedBlock() == null || !STBUtil.isInteractive(event.getClickedBlock().getType())) {
				Inventory inv = Bukkit.createInventory(event.getPlayer(), 9, getInventoryTitle());
				populateGUI(inv);
				event.getPlayer().openInventory(inv);
				event.setCancelled(true);
			}
		}
	}

	protected void populateGUI(Inventory inv) {
		if (filter != null) {
			AbstractItemFilter f = filter.isWhiteList() ? new ItemFilter(filter) : new ReverseItemFilter(filter);
			inv.setItem(0, f.toItemStack(1));
		}
	}

	public void installFilter(AbstractItemFilter itemFilter) {
		if (itemFilter != null) {
			this.filter = new Filter(itemFilter);
		} else {
			this.filter = null;
		}
	}
}
