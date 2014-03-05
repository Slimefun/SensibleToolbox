package me.desht.sensibletoolbox.storage;

import me.desht.sensibletoolbox.api.STBBlock;
import me.desht.sensibletoolbox.blocks.BaseSTBBlock;
import org.apache.commons.lang.Validate;
import org.bukkit.Location;

import java.util.UUID;

public class UpdateRecord {
	private final Operation op;
	private final UUID worldID;
	private final int x;
	private final int y;
	private final int z;
	private final String type;
	private final String data;

	public static UpdateRecord finishingRecord() {
		return new UpdateRecord(Operation.FINISH, null, null, null);
	}

	public static UpdateRecord commitRecord() {
		return new UpdateRecord(Operation.COMMIT, null, null, null);
	}

	public UpdateRecord(Operation op, Location loc, String type, BaseSTBBlock stb) {
		Validate.isTrue(!op.hasData() || stb != null);
		this.op = op;
		if (stb == null) {
			this.worldID = null;
			this.data = this.type = null;
			this.x = this.y = this.z = 0;
		} else {
			this.worldID = loc.getWorld().getUID();
			this.x = loc.getBlockX();
			this.y = loc.getBlockY();
			this.z = loc.getBlockZ();
			this.type = type;
			this.data = stb.freeze().saveToString();
		}
	}

	public Operation getOp() {
		return op;
	}

	public String getType() {
		return type;
	}

	public String getData() {
		return data;
	}

	public UUID getWorldID() {
		return worldID;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public int getZ() {
		return z;
	}

	@Override
	public String toString() {
		switch (op) {
			case FINISH: case COMMIT: return op.toString();
			default: return String.format("%s %s,%d,%d,%d %s", op.toString(), worldID, x, y, z, type);
		}
	}

	public enum Operation {
		INSERT,
		UPDATE,
		DELETE,
		FINISH,
		COMMIT;

		public boolean hasData() {
			return this != FINISH && this != COMMIT;
		}
	}
}
