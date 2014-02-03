package me.desht.sensibletoolbox.items.itemroutermodules;

import me.desht.sensibletoolbox.items.BaseSTBItem;
import me.desht.sensibletoolbox.util.STBUtil;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapelessRecipe;

public class ReceiverModule extends DirectionalItemRouterModule {
	public ReceiverModule() {}

	public ReceiverModule(ConfigurationSection conf) {
		super(conf);
	}

	@Override
	public String getItemName() {
		return "Item Router Receiver Module";
	}

	@Override
	public String[] getLore() {
		return new String[] { "Insert into an Item Router", "Receives items from a", "facing Sender module"};
	}

	@Override
	public Recipe getRecipe() {
		ShapelessRecipe recipe = new ShapelessRecipe(toItemStack(1));
		recipe.addIngredient(Material.PAPER); // in fact a Blank Module
		recipe.addIngredient(Material.TRAP_DOOR);
		return recipe;
	}

	@Override
	public Class<? extends BaseSTBItem> getCraftingRestriction(Material mat) {
		return mat == Material.PAPER ? BlankModule.class : null;
	}

	@Override
	public boolean execute() {
		return false;
	}

	public int receiveItem(ItemStack item) {
		int received = getOwner().insertItems(item, BlockFace.SELF);
		if (received > 0) {
			System.out.println("receiver in " + getOwner() + " received " + received + " of " + STBUtil.describeItemStack(item)
					+ ", now has " + STBUtil.describeItemStack(getOwner().getBufferItem()));
		}
		return received;
	}
}
