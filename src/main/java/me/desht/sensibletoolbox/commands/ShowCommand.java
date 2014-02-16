package me.desht.sensibletoolbox.commands;

import me.desht.dhutils.DHUtilsException;
import me.desht.dhutils.DHValidate;
import me.desht.dhutils.MessagePager;
import me.desht.dhutils.MiscUtil;
import me.desht.dhutils.commands.AbstractCommand;
import me.desht.sensibletoolbox.blocks.BaseSTBBlock;
import me.desht.sensibletoolbox.items.BaseSTBItem;
import me.desht.sensibletoolbox.storage.LocationManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

public class ShowCommand extends AbstractCommand {
	public ShowCommand() {
		super("stb show");
		setPermissionNode("stb.commands.show");
		setUsage("/<command> show [-w <world>] [-id <itemid>] [-perf]");
		setOptions("w:s", "id:s", "perf");
	}

	@Override
	public boolean execute(Plugin plugin, CommandSender sender, String[] args) {
		MessagePager pager = MessagePager.getPager(sender).clear();
		if (args.length >= 1) {
			showDetails(pager, args[0]);
		} else if (getBooleanOption("perf")) {
			pager.add(Bukkit.getWorlds().get(0).getLoadedChunks().length + " loaded chunks");
			long avg = LocationManager.getManager().getAverageTimePerTick();
			double pct = avg / 200000.0;
			pager.add(LocationManager.getManager().getAverageTimePerTick() + " ns/tick (" + pct + "%) spent in ticking STB blocks");
		} else {
			String id = getStringOption("id");
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

	private void showDetails(MessagePager pager, String locStr) {
		try {
			Location loc = MiscUtil.parseLocation(locStr);
			BaseSTBBlock stb = LocationManager.getManager().get(loc);
			DHValidate.notNull(stb, "No STB block at " + locStr);
			YamlConfiguration conf = stb.freeze();
			pager.add(ChatColor.YELLOW.toString() + stb + ":");
			for (String l : conf.saveToString().split("\\n")) {
				if (!l.isEmpty()) {
					pager.add(ChatColor.AQUA + l);
				}
			}
		} catch (IllegalArgumentException e) {
			throw new DHUtilsException(e.getMessage());
		}
	}

	private void show(MessagePager pager, World w, String id) {
		LocationManager mgr = LocationManager.getManager();
		for (BaseSTBBlock stb : mgr.listBlocks(w, true)) {
			if (id != null && !id.equalsIgnoreCase(stb.getItemID())) {
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
