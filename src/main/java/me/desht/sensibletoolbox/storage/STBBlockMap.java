package me.desht.sensibletoolbox.storage;

import me.desht.sensibletoolbox.blocks.BaseSTBBlock;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.*;

public class STBBlockMap {
	private final Map<BlockPosition, BaseSTBBlock> map = new HashMap<BlockPosition, BaseSTBBlock>();

	public BaseSTBBlock get(BlockPosition pos) {
		return map.get(pos);
	}

	public void put(BlockPosition pos, BaseSTBBlock stb) {
		map.put(pos, stb);
	}

	public boolean contains(BlockPosition pos) {
		return map.containsKey(pos);
	}

	public void remove(BlockPosition pos) {
		map.remove(pos);
	}

	public YamlConfiguration freeze() {
		YamlConfiguration conf = new YamlConfiguration();
		for (Map.Entry<BlockPosition, BaseSTBBlock> entry : map.entrySet()) {
			ConfigurationSection cs = conf.createSection(entry.getKey().toString());
			cs.set("TYPE", entry.getValue().getItemID());
			YamlConfiguration objConf = entry.getValue().freeze();
			for (String k : objConf.getKeys(false)) {
				cs.set(k, objConf.get(k));
			}
		}
		return conf;
	}

	public void tick() {
		for (BaseSTBBlock stb : map.values()) {
			stb.onServerTick();
		}
	}

	public List<? extends BaseSTBBlock> list() {
		return new ArrayList<BaseSTBBlock>(map.values());
	}
}
