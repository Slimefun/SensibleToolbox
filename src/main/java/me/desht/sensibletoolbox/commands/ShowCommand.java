package me.desht.sensibletoolbox.commands;

import me.desht.dhutils.MessagePager;
import me.desht.dhutils.MiscUtil;
import me.desht.dhutils.commands.AbstractCommand;
import me.desht.sensibletoolbox.LocationManager;
import me.desht.sensibletoolbox.SensibleToolboxPlugin;
import me.desht.sensibletoolbox.blocks.BaseSTBBlock;
import me.desht.sensibletoolbox.items.BaseSTBItem;
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
		LocationManager mgr = ((SensibleToolboxPlugin) plugin).getLocationManager();
		MessagePager pager = MessagePager.getPager(sender).clear();
		for (World w : Bukkit.getWorlds()) {
			for (BaseSTBItem item : mgr.listItems(w, true)) {
				if (item instanceof BaseSTBBlock) {
					BaseSTBBlock stb = (BaseSTBBlock) item;
					String name = item.getItemName();
					if (item.getDisplaySuffix() != null) {
						name = name + ": " + item.getDisplaySuffix();
					}
					pager.addListItem(MiscUtil.formatLocation(stb.getBaseLocation()) + " - " + name);
				}
			}
		}
		pager.add(Bukkit.getWorlds().get(0).getLoadedChunks().length + " loaded chunks");
		pager.showPage();
		return true;
	}
}
