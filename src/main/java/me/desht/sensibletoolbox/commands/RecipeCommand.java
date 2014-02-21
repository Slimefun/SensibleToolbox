package me.desht.sensibletoolbox.commands;

import me.desht.dhutils.DHValidate;
import me.desht.dhutils.commands.AbstractCommand;
import me.desht.sensibletoolbox.items.BaseSTBItem;
import me.desht.sensibletoolbox.items.RecipeBook;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class RecipeCommand extends AbstractCommand {
	public RecipeCommand() {
		super("stb recipe", 0, 1);
		setUsage("/<command> recipe <filter-string>");
		setPermissionNode("stb.commands.recipe");
	}

	@Override
	public boolean execute(Plugin plugin, CommandSender sender, String[] args) {
		notFromConsole(sender);
		Player player = (Player) sender;
		RecipeBook book = BaseSTBItem.getItemFromItemStack(player.getItemInHand(), RecipeBook.class);
		DHValidate.notNull(book, "You must be holding a Recipe Book to search for recipes!");
		String filter = args.length > 0 ? args[0] : "";
		Block b = player.getTargetBlock(null, 4);
		book.setFabricationAvailable(b != null && b.getType() == Material.WORKBENCH);
		book.setFilter(filter);
		book.goToItemList();
		book.openBook(player);
		return true;
	}
}
