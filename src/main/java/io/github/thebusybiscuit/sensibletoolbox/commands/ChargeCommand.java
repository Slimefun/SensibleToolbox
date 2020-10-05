package io.github.thebusybiscuit.sensibletoolbox.commands;

import java.util.Set;

import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Attachable;
import org.bukkit.plugin.Plugin;

import io.github.thebusybiscuit.sensibletoolbox.api.SensibleToolbox;
import io.github.thebusybiscuit.sensibletoolbox.api.energy.Chargeable;
import io.github.thebusybiscuit.sensibletoolbox.api.items.BaseSTBBlock;
import io.github.thebusybiscuit.sensibletoolbox.api.items.BaseSTBItem;
import io.github.thebusybiscuit.sensibletoolbox.core.storage.LocationManager;
import io.github.thebusybiscuit.sensibletoolbox.utils.STBUtil;
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
        if (!(sender instanceof Player)) {
            MiscUtil.errorMessage(sender, "This command can't be run from the console.");
            return true;
        }

        Player player = (Player) sender;
        ItemStack itemInHand = player.getInventory().getItemInMainHand();
        BaseSTBItem item = SensibleToolbox.getItemRegistry().fromItemStack(itemInHand);
        BaseSTBBlock block = null;
        Chargeable chargeable = null;

        if (item instanceof Chargeable) {
            chargeable = (Chargeable) item;
        }
        else {
            Block b = player.getTargetBlock((Set<Material>) null, 10);

            if (Tag.WALL_SIGNS.isTagged(b.getType()) || Tag.STANDING_SIGNS.isTagged(b.getType())) {
                Sign s = (Sign) b.getState();
                b = b.getRelative(((Attachable) s.getData()).getAttachedFace());
            }

            block = LocationManager.getManager().get(b.getLocation());

            if (block instanceof Chargeable) {
                chargeable = (Chargeable) block;
            }
        }

        if (chargeable == null) {
            MiscUtil.errorMessage(sender, "Nothing suitable to charge.");
            return true;
        }

        int max = chargeable.getMaxCharge();
        int amount;

        if (args.length > 0) {
            amount = Integer.parseInt(args[0]);

            if (amount >= 0 && amount <= max) {
                MiscUtil.errorMessage(sender, "Must be in range 0-" + max);
                return true;
            }
        }
        else {
            amount = max;
        }

        chargeable.setCharge(amount);

        if (item != null) {
            player.getInventory().setItemInMainHand(item.toItemStack());
        }
        else if (block != null) {
            block.update(true);
            MiscUtil.statusMessage(player, "&6" + block.getItemName() + "&- charged to " + STBUtil.getChargeString(chargeable));
        }

        return true;
    }
}
