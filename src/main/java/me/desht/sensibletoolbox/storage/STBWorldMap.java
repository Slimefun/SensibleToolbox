package me.desht.sensibletoolbox.storage;

import org.bukkit.Bukkit;
import org.bukkit.World;

import java.util.HashMap;
import java.util.Map;

public class STBWorldMap {
	private final Map<String, STBChunkMap> map = new HashMap<String, STBChunkMap>();

	public STBChunkMap get(String worldName) {
		if (!map.containsKey(worldName)) {
			map.put(worldName, new STBChunkMap());
		}
		return map.get(worldName);
	}

	public void put(String worldName, STBChunkMap stcm) {
		map.put(worldName, stcm);
	}

	public boolean contains(String worldName) {
		return map.containsKey(worldName);
	}

//	public void tick() {
//		for (String worldName : map.keySet()) {
//			World w = Bukkit.getWorld(worldName);
//			if (w != null) {
//				map.get(worldName).tick(w);
//			}
//		}
//	}

	public void remove(String name) {
		map.remove(name);
	}
}
