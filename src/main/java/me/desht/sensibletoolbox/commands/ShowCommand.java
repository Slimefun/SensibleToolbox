package me.desht.sensibletoolbox.commands;

import me.desht.dhutils.MessagePager;
import me.desht.dhutils.MiscUtil;
import me.desht.dhutils.commands.AbstractCommand;
import me.desht.sensibletoolbox.blocks.BaseSTBBlock;
import me.desht.sensibletoolbox.storage.LocationManager;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

public class ShowCommand extends AbstractCommand {
	public ShowCommand() {
		super("stb show", 0, 1);
		setPermissionNode("stb.commands.show");
		setUsage("/<command> show");
	}

	@Override
	public boolean execute(Plugin plugin, CommandSender sender, String[] args) {
		LocationManager mgr = LocationManager.getManager();
		MessagePager pager = MessagePager.getPager(sender).clear();
		for (World w : Bukkit.getWorlds()) {
			for (BaseSTBBlock stb : mgr.listBlocks(w, true)) {
				String name = stb.getItemName();
				if (stb.getDisplaySuffix() != null) {
					name = name + ": " + stb.getDisplaySuffix();
				}
				pager.addListItem(MiscUtil.formatLocation(stb.getLocation()) + " - " + name);
			}
		}
		pager.add(Bukkit.getWorlds().get(0).getLoadedChunks().length + " loaded chunks");
		pager.showPage();
		return true;
	}
}
