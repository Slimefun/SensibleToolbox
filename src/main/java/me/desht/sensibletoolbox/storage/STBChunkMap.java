package me.desht.sensibletoolbox.storage;

import me.desht.sensibletoolbox.blocks.BaseSTBBlock;
import org.bukkit.Chunk;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class STBChunkMap {
	private final Map<Long, STBBlockMap> map = new HashMap<Long, STBBlockMap>();

	public STBBlockMap get(Chunk c) {
		return get(new ChunkCoords(c));
	}

	public STBBlockMap get(ChunkCoords cc) {
		Long key = makeKey(cc);
		if (!map.containsKey(key)) {
			map.put(key, new STBBlockMap());
		}
		return map.get(key);
	}

	public void put(Chunk c, STBBlockMap stbm) {
		map.put(makeKey(c), stbm);
	}

	public boolean contains(Chunk c) {
		return map.containsKey(makeKey(c));
	}

	private Long makeKey(ChunkCoords c) {
		return c.getX() + ((long) c.getZ() << 32);
	}

	private Long makeKey(Chunk c) {
		return c.getX() + ((long) c.getZ() << 32);
	}

//	public void tick(World w) {
//		for (Map.Entry<Long, STBBlockMap> entry : map.entrySet()) {
//			int cx = entry.getKey().intValue();
//			int cz = (int)((entry.getKey() >> 32));
//			if (w.isChunkLoaded(cx, cz)) {
//				entry.getValue().tick();
//			}
//		}
//	}

	public List<? extends BaseSTBBlock> list() {
		List<BaseSTBBlock> l = new ArrayList<BaseSTBBlock>();
		for (STBBlockMap stbm : map.values()) {
			l.addAll(stbm.list());
		}
		return l;
	}
}
