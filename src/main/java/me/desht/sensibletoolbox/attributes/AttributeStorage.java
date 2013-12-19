package me.desht.sensibletoolbox.attributes;

import java.util.UUID;

import org.bukkit.inventory.ItemStack;
import me.desht.sensibletoolbox.attributes.Attributes.Attribute;
import me.desht.sensibletoolbox.attributes.Attributes.AttributeType;
import me.desht.sensibletoolbox.attributes.Attributes.Operation;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

/**
 * Store meta-data in an ItemStack as attributes.
 * @author Kristian
 */
public class AttributeStorage {
	private ItemStack target;
	private final UUID uniqueKey;

	private AttributeStorage(ItemStack target, UUID uniqueKey) {
		this.target = Preconditions.checkNotNull(target, "target cannot be NULL");
		this.uniqueKey = Preconditions.checkNotNull(uniqueKey, "uniqueKey cannot be NULL");
	}

	/**
	 * Construct a new attribute storage system.
	 * <p>
	 * The key must be the same in order to retrieve the same data.
	 * @param target - the item stack where the data will be stored.
	 * @param uniqueKey - the unique key used to retrieve the correct data.
	 */
	public static AttributeStorage newTarget(ItemStack target, UUID uniqueKey) {
		return new AttributeStorage(target, uniqueKey);
	}

	/**
	 * Retrieve the data stored in the item's attribute.
	 * @param defaultValue - the default value to return if no data can be found.
	 * @return The stored data, or defaultValue if not found.
	 */
	public String getData(String defaultValue) {
		Attribute current = getAttribute(new Attributes(target), uniqueKey);
		return current != null ? current.getName() : null;
	}

	/**
	 * Set the data stored in the attributes.
	 * @param data - the data.
	 */
	public void setData(String data) {
		Attributes attributes = new Attributes(target);
		Attribute current = getAttribute(attributes, uniqueKey);

		if (current == null) {
			attributes.add(
					Attribute.newBuilder().
							name(data).
							amount(0).
							uuid(uniqueKey).
							operation(Operation.ADD_NUMBER).
							type(AttributeType.GENERIC_ATTACK_DAMAGE).
							build()
			);
		} else {
			current.setName(data);
		}
		this.target = attributes.getStack();
	}

	/**
	 * Retrieve the target stack. May have been changed.
	 * @return The target stack.
	 */
	public ItemStack getTarget() {
		return target;
	}

	/**
	 * Retrieve an attribute by UUID.
	 * @param attributes - the attribute.
	 * @param id - the UUID to search for.
	 * @return The first attribute associated with this UUID, or NULL.
	 */
	private Attribute getAttribute(Attributes attributes, UUID id) {
		for (Attribute attribute : attributes.values()) {
			if (Objects.equal(attribute.getUUID(), id)) {
				return attribute;
			}
		}
		return null;
	}
}