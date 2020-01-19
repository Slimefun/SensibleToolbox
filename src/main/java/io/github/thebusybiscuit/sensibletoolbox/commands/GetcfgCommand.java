package io.github.thebusybiscuit.sensibletoolbox.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;

import io.github.thebusybiscuit.sensibletoolbox.SensibleToolboxPlugin;
import me.desht.dhutils.MessagePager;
import me.desht.dhutils.MiscUtil;
import me.desht.dhutils.commands.AbstractCommand;

public class GetcfgCommand extends AbstractCommand {

    public GetcfgCommand() {
        super("stb getcfg", 0, 1);
        setPermissionNode("stb.commands.getcfg");
        setUsage("/<command> getcfg  [<config-key>]");
    }

    @Override
    public boolean execute(Plugin plugin, CommandSender sender, String[] args) {
        List<String> lines = getPluginConfiguration(args.length >= 1 ? args[0] : null);
        if (lines.size() > 1) {
            MessagePager pager = MessagePager.getPager(sender).clear().setParseColours(true);
            for (String line : lines) {
                pager.add(line);
            }
            pager.showPage();
        } else if (lines.size() == 1) {
            MiscUtil.statusMessage(sender, lines.get(0));
        }
        return true;
    }

    public List<String> getPluginConfiguration() {
        return getPluginConfiguration(null);
    }

    public List<String> getPluginConfiguration(String section) {
        ArrayList<String> res = new ArrayList<String>();
        Configuration config = SensibleToolboxPlugin.getInstance().getConfig();
        ConfigurationSection cs = config.getRoot();

        Set<String> items;
        if (section == null) {
            items = config.getDefaults().getKeys(true);
        } else {
            if (config.getDefaults().isConfigurationSection(section)) {
                cs = config.getConfigurationSection(section);
                items = config.getDefaults().getConfigurationSection(section).getKeys(true);
            } else {
                items = new HashSet<String>();
                if (config.getDefaults().contains(section))
                    items.add(section);
            }
        }

        for (String k : items) {
            if (cs.isConfigurationSection(k))
                continue;
            res.add("&f" + k + "&- = '&e" + cs.get(k) + "&-'");
        }
        Collections.sort(res);
        return res;
    }

    @Override
    public List<String> onTabComplete(Plugin plugin, CommandSender sender, String[] args) {
        switch (args.length) {
            case 1:
                return getConfigCompletions(sender, plugin.getConfig(), args[0]);
            default:
                return noCompletions(sender);
        }
    }
}
