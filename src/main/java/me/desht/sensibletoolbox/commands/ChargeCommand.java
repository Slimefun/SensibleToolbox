package me.desht.sensibletoolbox.commands;

import me.desht.dhutils.DHUtilsException;
import me.desht.dhutils.commands.AbstractCommand;
import me.desht.sensibletoolbox.api.Chargeable;
import me.desht.sensibletoolbox.items.BaseSTBItem;
import org.apache.commons.lang.Validate;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class ChargeCommand extends AbstractCommand {
	public ChargeCommand() {
		super("stb charge", 0, 1);
		setPermissionNode("stb.commands.charge");
		setUsage("/<command> charge <amount>");
	}

	@Override
	public boolean execute(Plugin plugin, CommandSender sender, String[] args) {
		notFromConsole(sender);

		Player player = (Player) sender;
		BaseSTBItem item = BaseSTBItem.getItemFromItemStack(player.getItemInHand());
		if (item == null || !(item instanceof Chargeable)) {
			throw new DHUtilsException("You are not holding a chargeable item.");
		}

		int max = ((Chargeable) item).getMaxCharge();
		int amount;
		if (args.length > 0) {
			try {
				amount = Integer.parseInt(args[0]);
				Validate.isTrue(amount >= 0 && amount <= max, "Must be in range 0-" + max);
			} catch (IllegalArgumentException e) {
				throw new DHUtilsException("Invalid value: " + args[0] + " - " + e.getMessage());
			}
		} else {
			amount = max;
		}

		((Chargeable) item).setCharge(amount);
		player.setItemInHand(item.toItemStack(1));

		return true;
	}
}
