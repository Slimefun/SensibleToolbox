package me.mrCookieSlime.sensibletoolbox.commands;

import me.desht.sensibletoolbox.dhutils.DHUtilsException;
import me.desht.sensibletoolbox.dhutils.DHValidate;
import me.desht.sensibletoolbox.dhutils.MiscUtil;
import me.desht.sensibletoolbox.dhutils.commands.AbstractCommand;
import me.mrCookieSlime.sensibletoolbox.api.SensibleToolbox;
import me.mrCookieSlime.sensibletoolbox.api.energy.Chargeable;
import me.mrCookieSlime.sensibletoolbox.api.items.BaseSTBBlock;
import me.mrCookieSlime.sensibletoolbox.api.items.BaseSTBItem;
import me.mrCookieSlime.sensibletoolbox.api.util.STBUtil;
import me.mrCookieSlime.sensibletoolbox.core.storage.LocationManager;

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

    @SuppressWarnings("deprecation")
	@Override
    public boolean execute(Plugin plugin, CommandSender sender, String[] args) {
        notFromConsole(sender);

        Player player = (Player) sender;
        BaseSTBItem item = SensibleToolbox.getItemRegistry().fromItemStack(player.getItemInHand());
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
            player.setItemInHand(item.toItemStack());
        } else if (stb != null) {
            stb.update(true);
            MiscUtil.statusMessage(player, "&6" + stb.getItemName() + "&- charged to " + STBUtil.getChargeString(c));
        }
        return true;
    }
}
