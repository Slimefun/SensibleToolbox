package me.desht.sensibletoolbox.enderstorage;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;

import java.io.File;

public class PlayerHolder extends EnderStorageHolder {
    private final OfflinePlayer player;

    protected PlayerHolder(EnderStorageManager manager, OfflinePlayer player, int frequency) {
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
    public String getInventoryTitle() {
        return ChatColor.DARK_PURPLE + "Ender " + ChatColor.DARK_RED + "[Personal ƒ" + getFrequency() + "]";
    }

    @Override
    public String toString() {
        return "Player Ender Storage " + player.getName() + "#" + getFrequency();
    }
}
