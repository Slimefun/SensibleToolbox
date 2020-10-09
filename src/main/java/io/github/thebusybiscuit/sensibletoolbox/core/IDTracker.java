package io.github.thebusybiscuit.sensibletoolbox.core;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import javax.annotation.Nonnull;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import io.github.thebusybiscuit.sensibletoolbox.blocks.machines.SCURelay;

/**
 * The {@link IDTracker} tracks a numeric id that is automatically incremented.
 * It is currently only used by the {@link SCURelay}.
 * 
 * @author desht
 * @author TheBusyBiscuit
 *
 * @param <T>
 *            The Type of data to track
 */
public class IDTracker<T> {

    private final Map<Integer, T> map = new HashMap<>();
    private final Plugin plugin;
    private final String name;
    private int nextID;

    public IDTracker(@Nonnull Plugin plugin, @Nonnull String name) {
        this.name = name;
        this.plugin = plugin;

        YamlConfiguration conf = new YamlConfiguration();
        File file = new File(plugin.getDataFolder(), name + ".yml");

        if (file.exists()) {
            try {
                conf.load(file);
                nextID = conf.getInt("nextID");
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, e, () -> "Could not read the next id from the config file: " + name);
            }
        } else {
            nextID = 1;
            save();
        }
    }

    private void save() {
        File file = new File(plugin.getDataFolder(), name + ".yml");
        YamlConfiguration conf = new YamlConfiguration();
        conf.set("nextID", nextID);

        try {
            conf.save(file);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, e, () -> "Failed to save id tracker file: " + name);
        }
    }

    private int getNextID() {
        int id = nextID;
        nextID++;
        save();
        return id;
    }

    public T get(int id) {
        return map.get(id);
    }

    public boolean contains(int id) {
        return map.containsKey(id);
    }

    public void add(int id, T data) {
        map.put(id, data);
    }

    public int add(T data) {
        int id = getNextID();
        map.put(id, data);
        return id;
    }
}
