package io.github.thebusybiscuit.sensibletoolbox.listeners;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nonnull;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;

import io.github.thebusybiscuit.sensibletoolbox.blocks.SoundMuffler;
import me.desht.dhutils.Debugger;

/**
 * This {@link Listener} handles sound packets for the {@link SoundMuffler}.
 * 
 * @author desht
 * @author TheBusyBiscuit
 * 
 * @see SoundMuffler
 *
 */
public class SoundMufflerListener extends PacketAdapter implements Listener {

    private static final PacketType SOUND_PACKET = PacketType.Play.Server.NAMED_SOUND_EFFECT;

    private final Set<SoundMuffler> mufflers = new HashSet<>();

    public SoundMufflerListener(@Nonnull Plugin plugin) {
        super(plugin, ListenerPriority.NORMAL, SOUND_PACKET);
    }

    @Override
    public void onPacketSending(PacketEvent event) {
        // Fixes #72 - Check if Player is temporary
        if (!event.isPlayerTemporary() && event.getPacketType() == SOUND_PACKET) {
            Player player = event.getPlayer();

            int x = event.getPacket().getIntegers().read(0) >> 3;
            int y = event.getPacket().getIntegers().read(1) >> 3;
            int z = event.getPacket().getIntegers().read(2) >> 3;
            Location loc = new Location(player.getWorld(), x, y, z);

            for (SoundMuffler sm : mufflers) {
                if (isInRange(sm, loc)) {
                    if (sm.getVolume() == 0) {
                        // Completely mute the sound
                        event.setCancelled(true);
                    } else {
                        // Reduce the sound volume
                        event.getPacket().getFloat().write(0, (float) sm.getVolume() / 100.0F);
                    }
                }
            }
        }
    }

    private boolean isInRange(@Nonnull SoundMuffler sm, @Nonnull Location loc) {
        int distance = SoundMuffler.DISTANCE * SoundMuffler.DISTANCE;
        return loc.getWorld().equals(sm.getLocation().getWorld()) && loc.distanceSquared(sm.getLocation()) < distance;
    }

    public void registerMuffler(@Nonnull SoundMuffler m) {
        Debugger.getInstance().debug("Registered sound muffler @ " + m.getLocation());
        mufflers.add(m);
    }

    public void unregisterMuffler(@Nonnull SoundMuffler m) {
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
