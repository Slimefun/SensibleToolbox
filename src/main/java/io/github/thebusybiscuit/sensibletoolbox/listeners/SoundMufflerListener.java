package io.github.thebusybiscuit.sensibletoolbox.listeners;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;

import io.github.thebusybiscuit.sensibletoolbox.blocks.SoundMuffler;
import me.desht.dhutils.Debugger;

public class SoundMufflerListener extends PacketAdapter implements Listener {
	
    private final Set<SoundMuffler> mufflers = new HashSet<>();

    public SoundMufflerListener(Plugin plugin) {
        super(plugin, ListenerPriority.NORMAL, PacketType.Play.Server.NAMED_SOUND_EFFECT);
    }

    @Override
    public void onPacketSending(PacketEvent event) {
        int distance = SoundMuffler.DISTANCE * SoundMuffler.DISTANCE;
        if (event.getPacketType() == PacketType.Play.Server.NAMED_SOUND_EFFECT) {
            int x = event.getPacket().getIntegers().read(0) >> 3;
            int y = event.getPacket().getIntegers().read(1) >> 3;
            int z = event.getPacket().getIntegers().read(2) >> 3;
            Location loc = new Location(event.getPlayer().getWorld(), x, y, z);
            for (SoundMuffler sm : mufflers) {
                if (loc.getWorld().equals(sm.getLocation().getWorld()) && loc.distanceSquared(sm.getLocation()) < distance) {
                    if (sm.getVolume() == 0) {
                        event.setCancelled(true);
                    } else {
                        event.getPacket().getFloat().write(0, (float) sm.getVolume() / 100.0f);
                    }
                }
            }
        }
    }

    public void registerMuffler(SoundMuffler m) {
        Debugger.getInstance().debug("Registered sound muffler @ " + m.getLocation());
        mufflers.add(m);
    }

    public void unregisterMuffler(SoundMuffler m) {
        Debugger.getInstance().debug("Unregistered sound muffler @ " + m.getLocation());
        mufflers.remove(m);
    }

    public void clear() {
        mufflers.clear();
    }

    public void start() {
        ProtocolLibrary.getProtocolManager().addPacketListener(this);
    }
}
