package me.desht.sensibletoolbox.nametags;

import java.util.Map;

import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

// These can be found in the following project:
//  https://github.com/aadnk/PacketWrapper
import me.desht.sensibletoolbox.nametags.wrapper.WrapperPlayServerAttachEntity;
import me.desht.sensibletoolbox.nametags.wrapper.WrapperPlayServerEntityDestroy;
import me.desht.sensibletoolbox.nametags.wrapper.WrapperPlayServerEntityTeleport;
import me.desht.sensibletoolbox.nametags.wrapper.WrapperPlayServerSpawnEntity;
import me.desht.sensibletoolbox.nametags.wrapper.WrapperPlayServerSpawnEntityLiving;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.google.common.base.Preconditions;
import com.google.common.collect.MapMaker;

/**
 * Represents a spawner of name tags.
 *
 * @author Kristian
 */
public class NameTagSpawner {
	private static final int Y_OFFSET = 55;

	private static final int WITHER_SKULL = 66;

	// Shared entity ID allocator
	private static int SHARED_ENTITY_ID = Short.MAX_VALUE;

	// The starting entity ID
	private int startEntityId;
	private int nameTagCount;

	// Previous locations
	private Map<Player, Vector[]> playerLocations = new MapMaker().weakKeys().makeMap();

	/**
	 * Construct a new name tag spawner.
	 * <p>
	 * Specify a number of name tags to spawn.
	 *
	 * @param nameTags - the maximum number of name tags we will spawn at any
	 *            given time.
	 */
	public NameTagSpawner(int nameTagCount) {
		this.startEntityId = SHARED_ENTITY_ID;
		this.nameTagCount = nameTagCount;

		// We need to reserve two entity IDs per name tag
		SHARED_ENTITY_ID += nameTagCount * 2;
	}

	/**
	 * Retrieve the maximum number of name tags we can spawn.
	 *
	 * @return The maximum number.
	 */
	public int getNameTagCount() {
		return nameTagCount;
	}

	/**
	 * Clear every name tag this spawner can create.
	 *
	 * @param observer - the observer.
	 */
	public void clearNameTags(Player observer) {
		int[] indices = new int[nameTagCount];

		for (int i = 0; i < indices.length; i++)
			indices[i] = i;
		clearNameTags(observer, indices);
	}

	/**
	 * Remove a name tag for a given observer.
	 *
	 * @param indices - the indices.
	 * @param observer - the observer.
	 */
	public void clearNameTags(Player observer, int... indices) {
		WrapperPlayServerEntityDestroy destroy = new WrapperPlayServerEntityDestroy();
		int[] ids = new int[indices.length * 2];

		// The entities to remove
		for (int i = 0; i < indices.length; i++) {
			Preconditions.checkPositionIndex(indices[i], nameTagCount, "indices");
			ids[i * 2] = getHorseId(indices[i]);
			ids[i * 2 + 1] = getSkullId(indices[i] * 2);
		}
		destroy.setEntities(ids);
		destroy.sendPacket(observer);
	}

	/**
	 * Retrieve the location of a given name tag.
	 *
	 * @param index - the index.
	 * @param observer - the observing player.
	 * @return The location, or NULL if the name tag has not been sent.
	 */
	public Location getLocation(int index, Player observer) {
		Vector[] locations = playerLocations.get(observer);

		if (locations != null && locations[index] != null) {
			return locations[index].toLocation(observer.getWorld());
		}
		return null;
	}

	/**
	 * Set the location and message of a name tag.
	 *
	 * @param index - index of the name tag. Cannot exceeed
	 *            {@link #getNameTagCount()}.
	 * @param observer - the observing player.
	 * @param location - the location in the same world as the player.
	 * @param dY - Y value to add to the final location.
	 * @param message - the message to display.
	 */
	public void setNameTag(int index, Player observer, Location location, double dY, String message) {
		WrapperPlayServerAttachEntity attach = new WrapperPlayServerAttachEntity();
		WrapperPlayServerSpawnEntityLiving horse = createHorsePacket(index, location, dY, message);
		WrapperPlayServerSpawnEntity skull = createSkullPacket(index, location, dY);

		// The horse is riding on the skull
		attach.setEntityId(horse.getEntityID());
		attach.setVehicleId(skull.getEntityID());

		horse.sendPacket(observer);
		skull.sendPacket(observer);
		attach.sendPacket(observer);

		// Save location
		getLocations(observer)[index] = new Vector(location.getX(), location.getY() + dY,
				location.getZ());
	}

	/**
	 * Move a name tag to a given location.
	 *
	 * @param index - the index of the tag.
	 * @param location - the new location.
	 */
	public void moveNameTag(int index, Player observer, Location location) {
		WrapperPlayServerEntityTeleport teleportHorse = new WrapperPlayServerEntityTeleport();
		teleportHorse.setEntityID(getHorseId(index));
		teleportHorse.setX(location.getX());
		teleportHorse.setY(location.getY() + Y_OFFSET);
		teleportHorse.setZ(location.getZ());

		WrapperPlayServerEntityTeleport teleportSkull = new WrapperPlayServerEntityTeleport(
				teleportHorse.getHandle().deepClone());
		teleportHorse.setEntityID(teleportHorse.getEntityID() + 1);

		teleportHorse.sendPacket(observer);
		teleportSkull.sendPacket(observer);
		getLocations(observer)[index] = location.toVector();
	}

	/**
	 * Retrieve the current vector of name tag locations.
	 *
	 * @param player - the player.
	 * @return The locations.
	 */
	private Vector[] getLocations(Player player) {
		Vector[] result = playerLocations.get(player);

		if (result == null) {
			result = new Vector[nameTagCount];
			playerLocations.put(player, result);
		}
		return result;
	}

	// Construct the invisible horse packet
	private WrapperPlayServerSpawnEntityLiving createHorsePacket(int index, Location location,
			double dY, String message) {
		WrapperPlayServerSpawnEntityLiving horse = new WrapperPlayServerSpawnEntityLiving();
		horse.setEntityID(getHorseId(index));
		horse.setType(EntityType.HORSE);
		horse.setX(location.getX());
		horse.setY(location.getY() + dY + Y_OFFSET);
		horse.setZ(location.getZ());

		WrappedDataWatcher wdw = new WrappedDataWatcher();
		wdw.setObject(10, message);
		wdw.setObject(11, (byte) 1);
		wdw.setObject(12, -1700000);
		horse.setMetadata(wdw);
		return horse;
	}

	// Construct the wither skull packet
	private WrapperPlayServerSpawnEntity createSkullPacket(int index, Location location, double dY) {
		WrapperPlayServerSpawnEntity skull = new WrapperPlayServerSpawnEntity();
		skull.setEntityID(getSkullId(index));
		skull.setType(WITHER_SKULL);
		skull.setX(location.getX());
		skull.setY(location.getY() + dY + Y_OFFSET);
		skull.setZ(location.getZ());
		return skull;
	}

	private int getHorseId(int index) {
		return startEntityId + index * 2;
	}

	private int getSkullId(int index) {
		return startEntityId + index * 2 + 1;
	}
}
