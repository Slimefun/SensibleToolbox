package me.desht.sensibletoolbox.commands;

import me.desht.dhutils.DHValidate;
import me.desht.dhutils.commands.AbstractCommand;
import me.desht.sensibletoolbox.items.BaseSTBItem;
import me.desht.sensibletoolbox.items.RecipeBook;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class SearchCommand extends AbstractCommand {
	public SearchCommand() {
		super("stb search", 0, 1);
		setUsage("/<command> search <filter-string>");
		setPermissionNode("stb.commands.search");
	}

	@Override
	public boolean execute(Plugin plugin, CommandSender sender, String[] args) {
		notFromConsole(sender);
		Player player = (Player) sender;
		RecipeBook book = BaseSTBItem.getItemFromItemStack(player.getItemInHand(), RecipeBook.class);
		DHValidate.notNull(book, "You must be holding a Recipe Book to search for recipes!");
		String filter = args.length > 0 ? args[0] : "";
		book.setFilter(filter);
		book.openBook(player);
		return true;
	}
}
