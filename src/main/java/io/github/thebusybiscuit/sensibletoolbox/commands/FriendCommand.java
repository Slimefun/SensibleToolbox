package io.github.thebusybiscuit.sensibletoolbox.commands;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.google.common.collect.Lists;

import io.github.thebusybiscuit.sensibletoolbox.SensibleToolboxPlugin;
import io.github.thebusybiscuit.sensibletoolbox.api.FriendManager;
import me.desht.dhutils.MiscUtil;
import me.desht.dhutils.text.MessagePager;

public class FriendCommand extends STBAbstractCommand {

    public FriendCommand() {
        super("stb friend");
        setPermissionNode("stb.commands.friend");
        setUsage(new String[] { "/<command> friend", "/<command> friend <player-name-or-uuid>", });
        setOptions("p:s");
    }

    @Override
    public boolean execute(Plugin plugin, CommandSender sender, String[] args) {
        FriendManager fm = ((SensibleToolboxPlugin) plugin).getFriendManager();

        Player target = getTargetPlayer(sender, getStringOption("p"));

        if (args.length >= 1) {
            UUID id = getID(args[0]);
            Validate.notNull(id, "Unknown player: " + args[0]);
            fm.addFriend(target.getUniqueId(), id);
            MiscUtil.statusMessage(sender, target.getName() + " is now friends with " + args[0]);
        }
        else if (args.length == 0) {
            // listing friends
            listFriends(sender, fm, target);
        }
        else {
            showUsage(sender);
        }
        return true;
    }

    private void listFriends(CommandSender sender, FriendManager fm, Player target) {
        Set<UUID> friends = fm.getFriends(target.getUniqueId());
        List<String> names = Lists.newArrayList();

        for (UUID id : friends) {
            names.add(Bukkit.getOfflinePlayer(id).getName());
        }

        String s = friends.size() == 1 ? "" : "s";
        MessagePager pager = MessagePager.getPager(sender).clear();
        pager.add(ChatColor.AQUA + "Player " + target.getName() + " has " + ChatColor.YELLOW + friends.size() + ChatColor.AQUA + " friend" + s + ":");

        for (String name : MiscUtil.asSortedList(names)) {
            pager.add(MessagePager.BULLET + ChatColor.YELLOW + name);
        }

        pager.showPage();
    }

    @Override
    public List<String> onTabComplete(Plugin plugin, CommandSender sender, String[] args) {
        if (args.length == 1) {
            // list online players
            return null;
        }
        else {
            return noCompletions(sender);
        }
    }
}
