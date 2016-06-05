package me.mrCookieSlime.sensibletoolbox.core.enderstorage;

import me.mrCookieSlime.sensibletoolbox.util.UnicodeSymbol;

import org.bukkit.ChatColor;

import java.io.File;

public class GlobalEnderHolder extends STBEnderStorageHolder {
    public GlobalEnderHolder(EnderStorageManager manager, int frequency) {
        super(manager, frequency);
    }

    @Override
    public String getInventoryTitle() {
        return ChatColor.DARK_PURPLE + "E-Storage " + ChatColor.DARK_RED + "[Global " + UnicodeSymbol.NUMBER.toUnicode() + getFrequency() + "]";
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

    @Override
    public boolean isGlobal() {
        return true;
    }

}
