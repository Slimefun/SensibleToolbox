package me.desht.sensibletoolbox.storage;

import org.bukkit.Chunk;
import org.bukkit.Location;

public class ChunkCoords {
	private final int x, z;

	public ChunkCoords(int x, int z) {
		this.x = x;
		this.z = z;
	}

	public ChunkCoords(Location loc) {
		this.x = loc.getBlockX() >> 4;
		this.z = loc.getBlockZ() >> 4;
	}

	public ChunkCoords(BlockPosition pos) {
		this(pos.getX() >> 4, pos.getZ() >> 4);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		ChunkCoords that = (ChunkCoords) o;

		if (x != that.x) return false;
		if (z != that.z) return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = x;
		result = 31 * result + z;
		return result;
	}

	public ChunkCoords(Chunk c) {
		this.x = c.getX();
		this.z = c.getZ();
	}

	public int getX() {
		return x;
	}

	public int getZ() {
		return z;
	}
}
