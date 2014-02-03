package me.desht.sensibletoolbox.items.itemroutermodules;

import me.desht.sensibletoolbox.blocks.ItemRouter;
import me.desht.sensibletoolbox.items.BaseSTBItem;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.lang.reflect.Constructor;

public abstract class ItemRouterModule extends BaseSTBItem {
	private ItemRouter owner;

	protected ItemRouterModule() {
	}

	public ItemRouterModule(ConfigurationSection conf) {
	}

	@Override
	public Material getBaseMaterial() {
		return Material.PAPER;
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
