package me.desht.sensibletoolbox.items.itemroutermodules;

import me.desht.dhutils.Debugger;
import me.desht.sensibletoolbox.items.BaseSTBItem;
import me.desht.sensibletoolbox.util.STBUtil;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.material.Dye;
import org.bukkit.material.MaterialData;

public class ReceiverModule extends DirectionalItemRouterModule {
	private static final Dye md = makeDye(DyeColor.ORANGE);

	public ReceiverModule() {}

	public ReceiverModule(ConfigurationSection conf) {
		super(conf);
	}

	@Override
	public String getItemName() {
		return "I.R. Mod: Receiver";
	}

	@Override
	public String[] getLore() {
		return makeDirectionalLore("Insert into an Item Router", "Receives items from a", "facing Sender module OR", "linked Adv. Sender module");
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
	public MaterialData getMaterialData() {
		return md;
	}

	@Override
	public boolean execute() {
		return false;
	}

	public int receiveItem(ItemStack item) {
		int received = getOwner().insertItems(item, BlockFace.SELF, false);
		if (received > 0) {
			Debugger.getInstance().debug(2, "receiver in " + getOwner() + " received " + received + " of " + item +
					", now has " + getOwner().getBufferItem());
		}
		return received;
	}
}
