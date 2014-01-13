package me.desht.sensibletoolbox.listeners;

import com.comphenix.protocol.Packets;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ConnectionSide;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import me.desht.sensibletoolbox.blocks.SoundMuffler;
import org.bukkit.Location;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import java.util.HashSet;
import java.util.Set;

public class SoundMufflerListener extends PacketAdapter implements Listener {
	private Set<SoundMuffler> mufflers = new HashSet<SoundMuffler>();

	public SoundMufflerListener(Plugin plugin) {
		super(plugin, ConnectionSide.SERVER_SIDE, ListenerPriority.NORMAL, Packets.Server.NAMED_SOUND_EFFECT);
	}

	@Override
	public void onPacketSending(PacketEvent event) {
		int distance = SoundMuffler.DISTANCE * SoundMuffler.DISTANCE;
		switch (event.getPacketID()) {
			case Packets.Server.NAMED_SOUND_EFFECT:
				int x = event.getPacket().getIntegers().read(0) >> 3;
				int y = event.getPacket().getIntegers().read(1) >> 3;
				int z = event.getPacket().getIntegers().read(2) >> 3;
				Location loc = new Location(event.getPlayer().getWorld(), x, y, z);
				for (SoundMuffler sm : mufflers) {
					if (loc.distanceSquared(sm.getBaseLocation()) < distance) {
//						String soundName = event.getPacket().getStrings().read(0);
//						System.out.println("muffle sound " + soundName + " @ " + loc);
						if (sm.getVolume() == 0) {
							event.setCancelled(true);
						} else {
							event.getPacket().getFloat().write(0, (float) sm.getVolume() / 100.0f);
						}
					}
				}
				break;
		}
	}

	public void registerMuffler(SoundMuffler m) {
		System.out.println("register muffler @ " + m.getBaseLocation());
		mufflers.add(m);
	}

	public void unregisterMuffler(SoundMuffler m) {
		System.out.println("unregister muffler @ " + m.getBaseLocation());
		mufflers.remove(m);
	}

	public void clear() {
		mufflers.clear();
	}

	public void start() {
		ProtocolLibrary.getProtocolManager().addPacketListener(this);
	}
}
