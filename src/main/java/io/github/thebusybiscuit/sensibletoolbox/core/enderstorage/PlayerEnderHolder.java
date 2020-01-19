package io.github.thebusybiscuit.sensibletoolbox.core.enderstorage;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;

import io.github.thebusybiscuit.sensibletoolbox.util.UnicodeSymbol;

import java.io.File;

public class PlayerEnderHolder extends STBEnderStorageHolder {
    private final OfflinePlayer player;

    protected PlayerEnderHolder(EnderStorageManager manager, OfflinePlayer player, int frequency) {
        super(manager, frequency);
        this.player = player;
    }

    public OfflinePlayer getPlayer() {
        return player;
    }

    @Override
    public File getSaveFile() {
        File f = new File(getManager().getStorageDir(), getPlayer().getUniqueId().toString());
        return new File(f, Integer.toString(getFrequency()));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        PlayerEnderHolder that = (PlayerEnderHolder) o;

        if (!player.equals(that.player)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + player.hashCode();
        return result;
    }

    @Override
    public String getInventoryTitle() {
        return ChatColor.DARK_PURPLE + "E-Storage " + ChatColor.DARK_RED + "[Personal " + UnicodeSymbol.NUMBER.toUnicode() + getFrequency() + "]";
    }

    @Override
    public String toString() {
        return "Player Ender Storage " + player.getName() + "#" + getFrequency();
    }

    @Override
    public boolean isGlobal() {
        return false;
    }
}
