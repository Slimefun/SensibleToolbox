package io.github.thebusybiscuit.sensibletoolbox.core;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

public class IDTracker {

    private final Map<Integer, Object> map = new HashMap<>();
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
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        else {
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
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int getNextID() {
        int id = nextID++;
        save();
        return id;
    }

    public Object get(int id) {
        return map.get(id);
    }

    public boolean contains(int id) {
        return map.containsKey(id);
    }

    public void add(int id, Object data) {
        map.put(id, data);
    }

    public int add(Object data) {
        int id = getNextID();
        map.put(id, data);
        return id;
    }
}
