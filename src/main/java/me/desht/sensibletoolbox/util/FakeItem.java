package me.desht.sensibletoolbox.util;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.Packets;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.InvocationTargetException;

// Definitely doesn't work yet!

public class FakeItem {
    private WrappedDataWatcher itemWatcher;

	public FakeItem(Plugin plugin, Location loc) {
		itemWatcher = getDefaultWatcher(loc.getWorld(), EntityType.DROPPED_ITEM);

		for (WrappedWatchableObject object : itemWatcher) System.out.println(object);
	}

	public void sendPacket(Player p) {
        PacketContainer newPacket = new PacketContainer(PacketType.Play.Server.SPAWN_ENTITY);

        newPacket.getIntegers().
            write(0, 500).
            write(1, (int) EntityType.DROPPED_ITEM.getTypeId()).
            write(2, (int) (p.getLocation().getX() * 32)).
            write(3, (int) (p.getLocation().getY() * 32)).
            write(4, (int) (p.getLocation().getZ() * 32));

        newPacket.getDataWatcherModifier().
            write(0, itemWatcher);

        try {
            ProtocolLibrary.getProtocolManager().sendServerPacket(p, newPacket);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

	private WrappedDataWatcher getDefaultWatcher(World world, EntityType type) {
		Entity entity = world.spawnEntity(new Location(world, 0, 256, 0), type);
        WrappedDataWatcher watcher = WrappedDataWatcher.getEntityWatcher(entity).deepClone();
        entity.remove();
        return watcher;
	}
}
