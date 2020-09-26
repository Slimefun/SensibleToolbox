package io.github.thebusybiscuit.sensibletoolbox.commands;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import com.google.common.base.Joiner;

import io.github.thebusybiscuit.cscorelib2.inventory.ItemUtils;
import io.github.thebusybiscuit.sensibletoolbox.api.SensibleToolbox;
import io.github.thebusybiscuit.sensibletoolbox.api.items.BaseSTBBlock;
import io.github.thebusybiscuit.sensibletoolbox.api.items.BaseSTBItem;
import io.github.thebusybiscuit.sensibletoolbox.core.storage.LocationManager;
import io.github.thebusybiscuit.sensibletoolbox.util.BukkitSerialization;
import io.github.thebusybiscuit.sensibletoolbox.util.STBUtil;
import me.desht.dhutils.DHUtilsException;
import me.desht.dhutils.MessagePager;
import me.desht.dhutils.MiscUtil;
import me.desht.dhutils.commands.AbstractCommand;

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
        }
        else if (getBooleanOption("perf")) {
            for (World w : Bukkit.getWorlds()) {
                pager.add(w.getName() + ": " + w.getLoadedChunks().length + " loaded chunks");
            }

            long avg = LocationManager.getManager().getAverageTimePerTick();
            double pct = avg / 200000.0;
            pager.add(avg + " ns/tick (" + pct + "%) spent in ticking STB blocks");
        }
        else if (getBooleanOption("dump")) {
            dumpItemData(plugin, sender);
        }
        else {
            String id = getStringOption("type");

            if (hasOption("w")) {
                World w = Bukkit.getWorld(getStringOption("w"));
                Validate.notNull(w, "Unknown world: " + getStringOption("w"));
                show(pager, w, id);
            }
            else {
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

        try (PrintWriter writer = new PrintWriter(out, "UTF-8")) {
            for (String itemId : SensibleToolbox.getItemRegistry().getItemIds()) {
                BaseSTBItem item = SensibleToolbox.getItemRegistry().getItemById(itemId);
                String lore = Joiner.on("\\\\").join(item.getLore()).replace("\u00a7r", "");
                String appearance = ItemUtils.getItemName(new ItemStack(item.getMaterial()));
                if (item.hasGlow()) {
                    appearance += " (glowing)";
                }
                writer.println("|" + item.getItemName() + "|" + item.getItemTypeID() + "|" + appearance + "|" + lore);
            }

            MiscUtil.statusMessage(sender, "STB item data dumped to " + out);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showDetails(CommandSender sender, MessagePager pager, String locStr) {
        BaseSTBItem item;

        if (locStr.equals(".")) {
            notFromConsole(sender);
            Player player = (Player) sender;
            // try to show either the held item or the targeted block
            item = SensibleToolbox.getItemRegistry().fromItemStack(player.getItemInHand());

            if (item == null) {
                Block b = player.getTargetBlock((Set<Material>) null, 10);
                item = LocationManager.getManager().get(b.getLocation(), true);
            }
        }
        else {
            try {
                Location loc = MiscUtil.parseLocation(locStr);
                item = LocationManager.getManager().get(loc);
                Validate.notNull(item, "No STB block at " + locStr);
            }
            catch (IllegalArgumentException e) {
                throw new DHUtilsException(e.getMessage());
            }
        }

        Validate.notNull(item, "No valid STB item/block found");

        YamlConfiguration conf = item.freeze();
        pager.add(ChatColor.AQUA.toString() + item + ":");

        for (String k : conf.getKeys(true)) {
            if (!conf.isConfigurationSection(k)) {
                Object o = conf.get(k);
                if (o.toString().startsWith("rO")) {
                    // is it safe to assume base64-encoded string always starts with "rO" ?
                    String s = getStringFromBase64(o);
                    pager.add(ChatColor.WHITE + k + " = " + ChatColor.YELLOW + s);
                }
                else {
                    pager.add(ChatColor.WHITE + k + " = " + ChatColor.YELLOW + o);
                }
            }
        }
    }

    private String getStringFromBase64(Object o) {
        String s;

        try {
            Inventory inv = BukkitSerialization.fromBase64(o.toString());
            List<String> l = new ArrayList<>(inv.getSize());
            for (ItemStack stack : inv) {
                if (stack != null) {
                    l.add(STBUtil.describeItemStack(stack));
                }
            }
            s = Joiner.on(", ").join(l);
        }
        catch (IOException e) {
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
            List<String> worlds = new ArrayList<>();

            for (World w : Bukkit.getWorlds()) {
                worlds.add(w.getName());
            }

            return filterPrefix(sender, worlds, args[args.length - 1]);
        }
        else if (args.length >= 2 && args[args.length - 2].equals("-id")) {
            return filterPrefix(sender, SensibleToolbox.getItemRegistry().getItemIds(), args[args.length - 1]);
        }
        else {
            showUsage(sender);
            return noCompletions(sender);
        }
    }
}
