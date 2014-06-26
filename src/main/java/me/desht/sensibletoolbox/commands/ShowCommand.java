package me.desht.sensibletoolbox.commands;

import com.google.common.base.Joiner;
import me.desht.dhutils.*;
import me.desht.dhutils.commands.AbstractCommand;
import me.desht.sensibletoolbox.api.STBItem;
import me.desht.sensibletoolbox.blocks.BaseSTBBlock;
import me.desht.sensibletoolbox.items.BaseSTBItem;
import me.desht.sensibletoolbox.storage.LocationManager;
import me.desht.sensibletoolbox.util.BukkitSerialization;
import me.desht.sensibletoolbox.util.STBUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class ShowCommand extends AbstractCommand {
    public ShowCommand() {
        super("stb show");
        setPermissionNode("stb.commands.show");
        setUsage("/<command> show [-w <world>] [-type <itemid>] [-perf]");
        setOptions("w:s", "type:s", "perf", "dump");
    }

    @Override
    public boolean execute(Plugin plugin, CommandSender sender, String[] args) {
        MessagePager pager = MessagePager.getPager(sender).clear();
        if (args.length >= 1) {
            showDetails(sender, pager, args[0]);
        } else if (getBooleanOption("perf")) {
            for (World w : Bukkit.getWorlds()) {
                pager.add(w.getName() + ": " + w.getLoadedChunks().length + " loaded chunks");
            }
            long avg = LocationManager.getManager().getAverageTimePerTick();
            double pct = avg / 200000.0;
            pager.add(avg + " ns/tick (" + pct + "%) spent in ticking STB blocks");
        } else if (getBooleanOption("dump")) {
            dumpItemData(plugin, sender);
        } else {
            String id = getStringOption("type");
            if (hasOption("w")) {
                World w = Bukkit.getWorld(getStringOption("w"));
                DHValidate.notNull(w, "Unknown world: " + getStringOption("w"));
                show(pager, w, id);
            } else {
                for (World w : Bukkit.getWorlds()) {
                    show(pager, w, id);
                }
            }
        }

        pager.showPage();
        return true;
    }

    private void dumpItemData(Plugin plugin, CommandSender sender) {
        File out = new File(plugin.getDataFolder(), "item-dump.txt");
        try {
            PrintWriter writer = new PrintWriter(out, "UTF-8");
            for (String itemId : BaseSTBItem.getItemIds()) {
                STBItem item = BaseSTBItem.getItemById(itemId);
                String lore = Joiner.on("\\\\").join(item.getLore()).replace("\u00a7r", "");
                String appearance = ItemNames.lookup(item.getMaterialData().toItemStack());
                if (item.hasGlow()) {
                    appearance += " (glowing)";
                }
                writer.println("|" + item.getItemName() + "|" + item.getItemTypeID() + "|" + appearance + "|" + lore);
            }
            writer.close();
            MiscUtil.statusMessage(sender, "STB item data dumped to " + out);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showDetails(CommandSender sender, MessagePager pager, String locStr) {
        BaseSTBItem item;
        if (locStr.equals(".")) {
            notFromConsole(sender);
            Player player = (Player) sender;
            // try to show either the held item or the targeted block
            item = BaseSTBItem.getItemFromItemStack(player.getItemInHand());
            if (item == null) {
                Block b = player.getTargetBlock(null, 10);
                item = LocationManager.getManager().get(b.getLocation(), true);
            }
        } else {
            try {
                Location loc = MiscUtil.parseLocation(locStr);
                item = LocationManager.getManager().get(loc);
                DHValidate.notNull(item, "No STB block at " + locStr);
            } catch (IllegalArgumentException e) {
                throw new DHUtilsException(e.getMessage());
            }
        }
        DHValidate.notNull(item, "No valid STB item/block found");

        YamlConfiguration conf = item.freeze();
        pager.add(ChatColor.AQUA.toString() + item + ":");
        for (String k : conf.getKeys(true)) {
            if (!conf.isConfigurationSection(k)) {
                Object o = conf.get(k);
                if (o.toString().startsWith("rO")) {
                    // is it safe to assume base64-encoded string always starts with "rO" ?
                    String s = getStringFromBase64(o);
                    pager.add(ChatColor.WHITE + k + " = " + ChatColor.YELLOW + s);
                } else {
                    pager.add(ChatColor.WHITE + k + " = " + ChatColor.YELLOW + o);
                }
            }
        }
    }

    private String getStringFromBase64(Object o) {
        String s;
        try {
            Inventory inv = BukkitSerialization.fromBase64(o.toString());
            List<String> l = new ArrayList<String>(inv.getSize());
            for (ItemStack stack : inv) {
                if (stack != null) {
                    l.add(STBUtil.describeItemStack(inv.getItem(0)));
                }
            }
            s = Joiner.on(", ").join(l);
        } catch (IOException e) {
            s = "???";
        }
        return s;
    }

    private void show(MessagePager pager, World w, String id) {
        LocationManager mgr = LocationManager.getManager();
        for (BaseSTBBlock stb : mgr.listBlocks(w, true)) {
            if (id != null && !id.equalsIgnoreCase(stb.getItemTypeID())) {
                continue;
            }
            String name = stb.getItemName();
            if (stb.getDisplaySuffix() != null) {
                name = name + ": " + stb.getDisplaySuffix();
            }
            pager.addListItem(MiscUtil.formatLocation(stb.getLocation()) + " - " + name);
        }
    }

    @Override
    public List<String> onTabComplete(Plugin plugin, CommandSender sender, String[] args) {
        if (args.length >= 2 && args[args.length - 2].equals("-w")) {
            List<String> worlds = new ArrayList<String>();
            for (World w : Bukkit.getWorlds()) {
                worlds.add(w.getName());
            }
            return filterPrefix(sender, worlds, args[args.length - 1]);
        } else if (args.length >= 2 && args[args.length - 2].equals("-id")) {
            return filterPrefix(sender, BaseSTBItem.getItemIds(), args[args.length - 1]);
        } else {
            showUsage(sender);
            return noCompletions(sender);
        }
    }
}
