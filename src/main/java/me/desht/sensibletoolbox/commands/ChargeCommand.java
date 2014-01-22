package me.desht.sensibletoolbox.commands;

import me.desht.dhutils.DHUtilsException;
import me.desht.dhutils.DHValidate;
import me.desht.dhutils.MiscUtil;
import me.desht.dhutils.commands.AbstractCommand;
import me.desht.sensibletoolbox.api.Chargeable;
import me.desht.sensibletoolbox.blocks.BaseSTBBlock;
import me.desht.sensibletoolbox.items.BaseSTBItem;
import me.desht.sensibletoolbox.storage.LocationManager;
import org.apache.commons.lang.Validate;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.material.Attachable;
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
		BaseSTBBlock stb = null;
		Chargeable c = null;
		if (item != null && item instanceof Chargeable) {
			c = (Chargeable) item;
		} else {
			// maybe there's a chargeable block targeted
			Block b = player.getTargetBlock(null, 10);
			if (b != null) {
				if (b.getType() == Material.WALL_SIGN || b.getType() == Material.SIGN_POST) {
					Sign s = (Sign) b.getState();
					b = b.getRelative(((Attachable) s.getData()).getAttachedFace());
				}
				stb = LocationManager.getManager().get(b.getLocation());
				if (stb != null && stb instanceof Chargeable) {
					System.out.println("found stb block");
					c = (Chargeable) stb;
				}
			}
		}
		DHValidate.notNull(c, "Nothing suitable to charge.");
		int max = c.getMaxCharge();
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

		c.setCharge(amount);
		if (item != null) {
			player.setItemInHand(item.toItemStack(1));
		} else if (stb != null) {
			stb.updateBlock();
			MiscUtil.statusMessage(player, "STB Block " + stb + " now has &e" + c.getCharge() + "&- charge.");
		}
		return true;
	}
}
