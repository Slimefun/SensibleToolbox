package io.github.thebusybiscuit.sensibletoolbox.api.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

/**
 * Serialize a Bukkit inventory to/from a string.
 * <p/>
 * Credit for this goes to Comphenix: https://gist.github.com/aadnk/8138186
 * Modified by desht to explicitly serialize any attribute data discovered on items.
 */
public class BukkitSerialization {

    public static String toBase64(Inventory inventory) {
        return toBase64(inventory, 0);
    }

    public static String toBase64(Inventory inventory, int maxItems) {
        if (maxItems <= 0 || maxItems > inventory.getSize()) {
            maxItems = inventory.getSize();
        }

        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);

            // Write the size of the inventory
            dataOutput.writeInt(maxItems);

            // Save every element in the list
            for (int i = 0; i < maxItems; i++) {
                ItemStack stack = inventory.getItem(i);
                dataOutput.writeObject(stack);
            }

            // Serialize that array
            dataOutput.close();
            return Base64Coder.encodeLines(outputStream.toByteArray());
        }
        catch (IOException e) {
            throw new IllegalStateException("Unable to save item stacks.", e);
        }
    }

    public static Inventory fromBase64(String data) throws IOException {
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
            int maxItems = dataInput.readInt();
            int invSize = STBUtil.roundUp(maxItems, 9); // Bukkit inventory size must be multiple of 9
            Inventory inventory = Bukkit.getServer().createInventory(null, invSize);

            // Read the serialized inventory
            for (int i = 0; i < maxItems; i++) {
                ItemStack stack = (ItemStack) dataInput.readObject();

                if (stack != null) {
                    inventory.setItem(i, stack);
                }
            }
            dataInput.close();
            return inventory;
        }
        catch (ClassNotFoundException e) {
            throw new IOException("Unable to decode class type.", e);
        }
    }
}