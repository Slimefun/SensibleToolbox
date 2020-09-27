package io.github.thebusybiscuit.sensibletoolbox.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.annotation.Nonnull;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

/**
 * Serialize a Bukkit {@link Inventory} to or from a {@link String}.
 * <p/>
 * Credit for this goes to Comphenix: https://gist.github.com/aadnk/8138186
 * 
 * @author Comphenix
 * @author desht
 * @author TheBusyBiscuit
 */
public final class BukkitSerialization {

    private BukkitSerialization() {}

    public static String toBase64(@Nonnull Inventory inventory) {
        return toBase64(inventory, 0);
    }

    public static String toBase64(@Nonnull Inventory inventory, int maxItems) {
        Validate.notNull(inventory, "Cannot serialize a 'null' Inventory!");
        if (maxItems <= 0 || maxItems > inventory.getSize()) {
            maxItems = inventory.getSize();
        }

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream(); BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream)) {

            // Write the size of the inventory
            dataOutput.writeInt(maxItems);

            // Save every element in the list
            for (int i = 0; i < maxItems; i++) {
                ItemStack stack = inventory.getItem(i);
                dataOutput.writeObject(stack);
            }

            // Serialize that array
            return Base64Coder.encodeLines(outputStream.toByteArray());
        }
        catch (IOException e) {
            throw new IllegalStateException("Unable to save item stacks.", e);
        }
    }

    public static Inventory fromBase64(@Nonnull String data) throws IOException {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data)); BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream)) {

            int maxItems = dataInput.readInt();

            // Bukkit inventory size must be multiple of 9
            int invSize = STBUtil.roundUp(maxItems, 9);
            Inventory inventory = Bukkit.getServer().createInventory(null, invSize);

            // Read the serialized inventory
            for (int i = 0; i < maxItems; i++) {
                ItemStack stack = (ItemStack) dataInput.readObject();

                if (stack != null) {
                    inventory.setItem(i, stack);
                }
            }

            return inventory;
        }
        catch (ClassNotFoundException e) {
            throw new IOException("Unable to decode class type.", e);
        }
    }
}