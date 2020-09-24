package me.desht.sensibletoolbox.util;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.InvocationTargetException;

// Definitely doesn't work yet!

public class FakeItem {
    private WrappedDataWatcher itemWatcher;

    public FakeItem(Plugin plugin, Location loc, Material mat) {
        itemWatcher = getDefaultWatcher(loc.getWorld(), mat);

//        for (WrappedWatchableObject object : itemWatcher)
//            System.out.println(object);
    }

    public void sendPacket(Player p) {
        PacketContainer newPacket = new PacketContainer(PacketType.Play.Server.SPAWN_ENTITY);

        newPacket.getIntegers().
                write(0, itemWatcher.getEntity().getEntityId()).
                write(1, (int) EntityType.DROPPED_ITEM.getTypeId()).
                write(2, (int) (p.getLocation().getX() * 32)).
                write(3, (int) (p.getLocation().getY() * 32)).
                write(4, (int) (p.getLocation().getZ() * 32)).
                write(5, 0).
                write(6, 0).
                write(7, 0);

        newPacket.getDataWatcherModifier().
                write(0, itemWatcher);

        try {
            ProtocolLibrary.getProtocolManager().sendServerPacket(p, newPacket);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    private WrappedDataWatcher getDefaultWatcher(World world, Material mat) {
        Entity entity = world.dropItem(new Location(world, 0, 256, 0), new ItemStack(mat));
        WrappedDataWatcher watcher = WrappedDataWatcher.getEntityWatcher(entity).deepClone();
        entity.remove();
        return watcher;
    }
}
