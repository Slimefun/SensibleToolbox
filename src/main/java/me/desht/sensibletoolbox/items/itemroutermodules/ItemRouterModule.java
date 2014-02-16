package me.desht.sensibletoolbox.items.itemroutermodules;

import me.desht.sensibletoolbox.blocks.ItemRouter;
import me.desht.sensibletoolbox.items.BaseSTBItem;
import org.bukkit.DyeColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.material.Dye;

public abstract class ItemRouterModule extends BaseSTBItem {
	private ItemRouter owner;
	private int amount;

	protected static Dye makeDye(DyeColor color) {
		Dye dye = new Dye();
		dye.setColor(color);
		return dye;
	}

	protected ItemRouterModule() {
		amount = 1;
	}

	public ItemRouterModule(ConfigurationSection conf) {
		amount = conf.getInt("amount", 1);
	}

	public int getAmount() {
		return amount;
	}

	public void setAmount(int amount) {
		this.amount = amount;
	}

	@Override
	public YamlConfiguration freeze() {
		YamlConfiguration conf = super.freeze();
		conf.set("amount", amount);
		return conf;
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


}
