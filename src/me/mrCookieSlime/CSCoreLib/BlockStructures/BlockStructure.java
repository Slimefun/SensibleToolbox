package me.mrCookieSlime.CSCoreLib.BlockStructures;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;

public class BlockStructure {
	
	Location l;
	List<Location> list;
	List<Material> types;
	List<Byte> metas;
	
	public BlockStructure(Location start) {
		this.l = start;
		this.list = new ArrayList<Location>();
		this.types = new ArrayList<Material>();
		this.metas = new ArrayList<Byte>();
	}
	
	public void setStructure(List<Location> list, List<Material> types, List<Byte> metas) {
		this.list = list;
		this.types = types;
		this.metas = metas;
	}
	
	public void add(Location l, Material type, byte meta) {
		list.add(l);
		types.add(type);
		metas.add(meta);
	}
	
	public void addRelative(int x, int y, int z, Material type, byte meta) {
		list.add(getRelative(x, y, z));
		types.add(type);
		metas.add(meta);
	}
	
	@SuppressWarnings("deprecation")
	public void build() {
		for (int i = 0; i < list.size(); i++) {
			list.get(i).getBlock().setType(types.get(i));
			list.get(i).getBlock().setData(metas.get(i));;
		}
	}
	
	public Location getRelative(int x, int y, int z) {
		return l.getBlock().getRelative(x, y, z).getLocation();
	}

}
