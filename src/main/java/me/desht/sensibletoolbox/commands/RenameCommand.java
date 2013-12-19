package me.desht.sensibletoolbox.commands;

import me.desht.dhutils.DHUtilsException;
import me.desht.dhutils.DHValidate;
import me.desht.dhutils.ExperienceManager;
import me.desht.dhutils.MiscUtil;
import me.desht.dhutils.commands.AbstractCommand;
import me.desht.sensibletoolbox.items.BaseSTBItem;
import me.desht.sensibletoolbox.items.EnderLeash;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public class RenameCommand extends AbstractCommand {
	public RenameCommand() {
		super("stb rename", 1, 1);
		setPermissionNode("stb.commands.rename");
		setUsage("/<command> rename <new-name>");
	}

	@Override
	public boolean execute(Plugin plugin, CommandSender sender, String[] args) {
		notFromConsole(sender);
		Player player = (Player) sender;
		ItemStack stack = player.getItemInHand();
		BaseSTBItem stb = BaseSTBItem.getBaseItem(stack);
		System.out.println("got item " + stb);
		DHValidate.isTrue(stb != null && stb instanceof EnderLeash, "You are not holding an Ender Leash.");
		EnderLeash el = (EnderLeash) stb;
		el.processItemMeta(stack);
		DHValidate.isTrue(el.getCaptured() != null, "You don't have a captured animal to rename.");
		if (!player.hasPermission("stb.commands.rename.free")) {
			int needed = plugin.getConfig().getInt("rename_level_cost");
			DHValidate.isTrue(player.getLevel() >= needed, "You need " + needed + " levels to rename an animal.");
			player.setLevel(player.getLevel() - needed);
		}
		player.setItemInHand(EnderLeash.setAnimalName(stack, args[0]));
		MiscUtil.statusMessage(player, "Ender Leash animal renamed: " + ChatColor.GOLD + args[0]);

		return true;
	}
}
