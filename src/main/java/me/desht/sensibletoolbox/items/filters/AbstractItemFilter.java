package me.desht.sensibletoolbox.items.filters;

import me.desht.sensibletoolbox.items.BaseSTBItem;
import me.desht.sensibletoolbox.util.Filter;
import me.desht.sensibletoolbox.util.STBUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

import java.util.List;

public abstract class AbstractItemFilter extends BaseSTBItem {
	private static final MaterialData md = new MaterialData(Material.WEB);

	private final Filter filter;

	protected AbstractItemFilter() {
		filter = new Filter();
	}

	public AbstractItemFilter(ConfigurationSection conf, boolean isWhite) {
		List<String> l = conf.getStringList("filtered");
		this.filter = new Filter(isWhite, l);
	}

	public AbstractItemFilter(Filter filter) {
		this.filter = filter.clone();
	}

	public static String getInventoryTitle() {
		return ChatColor.DARK_PURPLE + "Item Filter";
	}

	@Override
	public YamlConfiguration freeze() {
		YamlConfiguration conf = super.freeze();
		conf.set("filtered", filter.listFiltered());
		return conf;
	}

	@Override
	public MaterialData getMaterialData() {
		return md;
	}

	@Override
	public String[] getLore() {
		return new String[] { "Insert into an item router module", "to filters what the module will process.", "R-click: open filters GUI" };
	}

	@Override
	public String[] getExtraLore() {
		if (filter.size() == 0) {
			return new String[0];
		} else {
			return STBUtil.formatFilterLore(filter);
		}
	}

	@Override
	public void onInteractItem(PlayerInteractEvent event) {
		if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			Inventory inv = Bukkit.createInventory(event.getPlayer(), getFilterSize(), getInventoryTitle());
			populateFilterInventory(inv);
			event.getPlayer().openInventory(inv);
		}
	}

	private void populateFilterInventory(Inventory inv) {
		int n = 0;
		for (String f : filter.listFiltered()) {
			String[] f1 = f.split(":");
			Material mat = Material.getMaterial(f1[0]);
			short dur = f1.length > 1 ? Short.parseShort(f1[1]) : 0;
			ItemStack stack = new ItemStack(mat, 1, dur);
			inv.setItem(n, stack);
			if (++n >= inv.getSize()) {
				break;
			}
		}
	}

	protected int getFilterSize() {
		return 9;
	}

	public void clear() {
		filter.clear();
	}

	public void addFilteredItem(ItemStack stack) {
		filter.addItem(stack);
	}

	public List<String> listFiltered() {
		return filter.listFiltered();
	}
}
