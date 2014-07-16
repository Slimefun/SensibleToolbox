package me.desht.sensibletoolbox.core.enderstorage;

import org.bukkit.ChatColor;

import java.io.File;

public class GlobalHolder extends EnderStorageHolder {
    public GlobalHolder(EnderStorageManager manager, int frequency) {
        super(manager, frequency);
    }

    @Override
    public String getInventoryTitle() {
        return ChatColor.DARK_PURPLE + "E-Storage " + ChatColor.DARK_RED + "[Global ƒ" + getFrequency() + "]";
    }

    @Override
    public File getSaveFile() {
        File global = new File(getManager().getStorageDir(), "global");
        return new File(global, Integer.toString(getFrequency()));
    }

    @Override
    public String toString() {
        return "Global Ender Storage #" + getFrequency();
    }
}
