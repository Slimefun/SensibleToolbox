package io.github.thebusybiscuit.sensibletoolbox.commands;

import java.util.Set;

import org.apache.commons.lang.Validate;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.material.Attachable;
import org.bukkit.plugin.Plugin;

import io.github.thebusybiscuit.sensibletoolbox.api.SensibleToolbox;
import io.github.thebusybiscuit.sensibletoolbox.api.energy.Chargeable;
import io.github.thebusybiscuit.sensibletoolbox.api.items.BaseSTBBlock;
import io.github.thebusybiscuit.sensibletoolbox.api.items.BaseSTBItem;
import io.github.thebusybiscuit.sensibletoolbox.core.storage.LocationManager;
import io.github.thebusybiscuit.sensibletoolbox.util.STBUtil;
import me.desht.dhutils.DHUtilsException;
import me.desht.dhutils.MiscUtil;
import me.desht.dhutils.commands.AbstractCommand;

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
        BaseSTBItem item = SensibleToolbox.getItemRegistry().fromItemStack(player.getInventory().getItemInMainHand());
        BaseSTBBlock stb = null;
        Chargeable c = null;

        if (item instanceof Chargeable) {
            c = (Chargeable) item;
        }
        else {
            Block b = player.getTargetBlock((Set<Material>) null, 10);

            if (Tag.WALL_SIGNS.isTagged(b.getType()) || Tag.STANDING_SIGNS.isTagged(b.getType())) {
                Sign s = (Sign) b.getState();
                b = b.getRelative(((Attachable) s.getData()).getAttachedFace());
            }

            stb = LocationManager.getManager().get(b.getLocation());

            if (stb instanceof Chargeable) {
                c = (Chargeable) stb;
            }
        }

        Validate.notNull(c, "Nothing suitable to charge.");
        int max = c.getMaxCharge();
        int amount;

        if (args.length > 0) {
            try {
                amount = Integer.parseInt(args[0]);
                Validate.isTrue(amount >= 0 && amount <= max, "Must be in range 0-" + max);
            }
            catch (IllegalArgumentException e) {
                throw new DHUtilsException("Invalid value: " + args[0] + " - " + e.getMessage());
            }
        }
        else {
            amount = max;
        }

        c.setCharge(amount);

        if (item != null) {
            player.getInventory().setItemInMainHand(item.toItemStack());
        }
        else if (stb != null) {
            stb.update(true);
            MiscUtil.statusMessage(player, "&6" + stb.getItemName() + "&- charged to " + STBUtil.getChargeString(c));
        }

        return true;
    }
}
