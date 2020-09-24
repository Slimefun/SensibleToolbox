package io.github.thebusybiscuit.sensibletoolbox.commands;

import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import io.github.thebusybiscuit.sensibletoolbox.api.SensibleToolbox;
import io.github.thebusybiscuit.sensibletoolbox.items.EnderLeash;
import me.desht.dhutils.MiscUtil;
import me.desht.dhutils.commands.AbstractCommand;

public class RenameCommand extends AbstractCommand {

    public RenameCommand() {
        super("stb rename", 1, 1);
        setPermissionNode("stb.commands.rename");
        setUsage("/<command> rename <new-name>");
        setQuotedArgs(true);
    }

    @Override
    public boolean execute(Plugin plugin, CommandSender sender, String[] args) {
        notFromConsole(sender);

        Player player = (Player) sender;
        ItemStack stack = player.getItemInHand();
        EnderLeash el = SensibleToolbox.getItemRegistry().fromItemStack(stack, EnderLeash.class);

        Validate.isTrue(el != null, "You are not holding an Ender Leash.");
        Validate.isTrue(el.getCapturedEntityType() != null, "You don't have a captured animal to rename.");

        if (!player.hasPermission("stb.commands.rename.free")) {
            int needed = plugin.getConfig().getInt("rename_level_cost");
            Validate.isTrue(player.getLevel() >= needed, "You need " + needed + " levels to rename an animal.");
            player.setLevel(player.getLevel() - needed);
        }

        el.setAnimalName(args[0]);
        player.setItemInHand(el.toItemStack());

        MiscUtil.statusMessage(player, "Ender Leash animal renamed: " + ChatColor.GOLD + args[0]);

        return true;
    }
}
