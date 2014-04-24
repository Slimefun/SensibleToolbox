package me.desht.sensibletoolbox.util;

import com.comphenix.attribute.Attributes;
import com.google.common.base.Joiner;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.UUID;

/**
 * Serialize a Bukkit inventory to/from a string.
 *
 * Credit for this goes to Comphenix: https://gist.github.com/aadnk/8138186
 * Modified by desht to explicitly serialize any attribute data discovered on items.
 */
public class BukkitSerialization {
	public static String toBase64(Inventory inventory) {
		return toBase64(inventory, 0);
	}

	public static String toBase64(Inventory inventory, int maxItems) {
		if (maxItems <= 0) maxItems = inventory.getSize();
		else if (maxItems > inventory.getSize()) maxItems = inventory.getSize();

		try {
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);

			// Write the size of the inventory
			dataOutput.writeInt(maxItems);

			// Save every element in the list
			for (int i = 0; i < maxItems; i++) {
				ItemStack stack = inventory.getItem(i);
				Attributes attributes = stack == null ? null : new Attributes(stack);
				dataOutput.writeObject(stack);
				if (attributes != null) {
					dataOutput.writeInt(attributes.size());
					for (Attributes.Attribute a : attributes.values()) {
						String s = Joiner.on(";;").join(
								a.getUUID().toString(), a.getOperation(), a.getName(),
								a.getAmount(), a.getAttributeType().getMinecraftId()
						);
						dataOutput.writeObject(s);
					}
				} else {
					dataOutput.writeInt(0);
				}
			}

			// Serialize that array
			dataOutput.close();
			return Base64Coder.encodeLines(outputStream.toByteArray());
		} catch (IOException e) {
			throw new IllegalStateException("Unable to save item stacks.", e);
		}
	}

	public static Inventory fromBase64(String data) throws IOException {
		try {
			ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
			BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
			int maxItems = dataInput.readInt();
			int invSize = STBUtil.roundUp(maxItems, 9);  // Bukkit inventory size must be multiple of 9
			Inventory inventory = Bukkit.getServer().createInventory(null, invSize);

			// Read the serialized inventory
			for (int i = 0; i < maxItems; i++) {
				ItemStack stack = (ItemStack) dataInput.readObject();
				int nAttrs = dataInput.readInt();
				if (nAttrs > 0) {
					Attributes attributes = new Attributes(stack);
					for (int n = 0; n < nAttrs; n++) {
						String s = (String) dataInput.readObject();
						String[] fields = s.split(";;");
						attributes.add(Attributes.Attribute.newBuilder().
								name(fields[2]).
								amount(Double.parseDouble(fields[3])).
								uuid(UUID.fromString(fields[0])).
								operation(Attributes.Operation.valueOf(fields[1])).
								type(Attributes.AttributeType.fromId(fields[4])).
								build()
						);
					}
					stack = attributes.getStack();
				}
				if (stack != null) {
					inventory.setItem(i, stack);
				}
			}
			dataInput.close();
			return inventory;
		} catch (ClassNotFoundException e) {
			throw new IOException("Unable to decode class type.", e);
		}
	}
}