package me.desht.sensibletoolbox.items.itemroutermodules;

import me.desht.sensibletoolbox.blocks.ItemRouter;
import me.desht.sensibletoolbox.items.BaseSTBItem;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.material.Dye;
import org.bukkit.material.MaterialData;

import java.lang.reflect.Constructor;

public abstract class ItemRouterModule extends BaseSTBItem {
	public static String getInventoryTitle() {
		return ChatColor.GOLD + "Item Router Module Setup";
	}

	protected static Dye makeDye(DyeColor color) {
		Dye dye = new Dye();
		dye.setColor(color);
		return dye;
	}

	private ItemRouter owner;

	protected ItemRouterModule() {
	}

	public ItemRouterModule(ConfigurationSection conf) {
	}

	@Override
	public boolean hasGlow() {
		return true;
	}

	public ItemRouter getOwner() {
		return owner;
	}

	public void setOwner(ItemRouter owner) {
		this.owner = owner;
	}

	public abstract boolean execute();

}
