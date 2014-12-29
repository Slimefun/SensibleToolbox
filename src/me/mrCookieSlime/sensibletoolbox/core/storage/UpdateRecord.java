package me.mrCookieSlime.sensibletoolbox.core.storage;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.UUID;

public class UpdateRecord {
    private final Operation op;
    private final UUID worldID;
    private final int x;
    private final int y;
    private final int z;
    private String type;
    private String data;

    public static UpdateRecord finishingRecord() {
        return new UpdateRecord(Operation.FINISH, null);
    }

    public static UpdateRecord commitRecord() {
        return new UpdateRecord(Operation.COMMIT, null);
    }

    public UpdateRecord(Operation op, Location loc) {
        this.op = op;
        if (loc != null) {
            this.worldID = loc.getWorld().getUID();
            this.x = loc.getBlockX();
            this.y = loc.getBlockY();
            this.z = loc.getBlockZ();
        } else {
            this.worldID = null;
            this.x = this.y = this.z = 0;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UpdateRecord that = (UpdateRecord) o;

        if (x != that.x) return false;
        if (y != that.y) return false;
        if (z != that.z) return false;
        if (worldID != null ? !worldID.equals(that.worldID) : that.worldID != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = worldID != null ? worldID.hashCode() : 0;
        result = 31 * result + x;
        result = 31 * result + y;
        result = 31 * result + z;
        return result;
    }

    public Operation getOp() {
        return op;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
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
            case FINISH:
            case COMMIT:
                return op.toString();
            default:
                return String.format("%s %s,%d,%d,%d %s", op.toString(), worldID, x, y, z, type);
        }
    }

    public Location getLocation() {
        return new Location(Bukkit.getWorld(worldID), x, y, z);
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
