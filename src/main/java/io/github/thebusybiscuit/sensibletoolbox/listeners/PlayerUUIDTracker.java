package io.github.thebusybiscuit.sensibletoolbox.listeners;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

import io.github.thebusybiscuit.sensibletoolbox.SensibleToolboxPlugin;
import me.desht.dhutils.Debugger;

public class PlayerUUIDTracker extends STBBaseListener implements Runnable {

    private static final String MAP_FILE = "playermap.yml";
    private static final long SAVE_INTERVAL = 30; // seconds
    private final YamlConfiguration map = new YamlConfiguration();
    private boolean saveNeeded = false;

    public PlayerUUIDTracker(SensibleToolboxPlugin plugin) {
        super(plugin);
        loadMap();
        Bukkit.getScheduler().runTaskTimer(plugin, this, 20L, 20L * SAVE_INTERVAL);
    }

    public void loadMap() {
        File file = new File(plugin.getDataFolder(), MAP_FILE);
        if (file.exists()) {
            try {
                map.load(file);
                saveNeeded = false;
            } catch (IOException | InvalidConfigurationException e) {
                e.printStackTrace();
            }
        }
    }

    public void saveMap() {
        if (saveNeeded) {
            File file = new File(plugin.getDataFolder(), MAP_FILE);
            try {
                map.save(file);
                Debugger.getInstance().debug("Saved UUID->Name map to " + file);
                saveNeeded = false;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public String getPlayerName(UUID uuid) {
        return map.getString(uuid.toString());
    }

    public String getPlayerName(Player p) {
        return map.getString(p.getUniqueId().toString());
    }

    public void updatePlayer(Player p) {
        String name = getPlayerName(p);
        if (name == null || !name.equals(p.getName())) {
            map.set(p.getUniqueId().toString(), p.getName());
            Debugger.getInstance().debug("Updated UUID->Name map: " + p.getUniqueId() + " = " + p.getName());
            saveNeeded = true;
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        updatePlayer(event.getPlayer());
    }

    @Override
    public void run() {
        saveMap();
    }
}
