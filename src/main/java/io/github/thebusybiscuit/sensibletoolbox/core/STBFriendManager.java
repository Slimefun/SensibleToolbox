package io.github.thebusybiscuit.sensibletoolbox.core;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nonnull;

import io.github.thebusybiscuit.sensibletoolbox.helpers.Validate;
import org.bukkit.configuration.file.YamlConfiguration;

import io.github.thebusybiscuit.sensibletoolbox.SensibleToolboxPlugin;
import io.github.thebusybiscuit.sensibletoolbox.api.FriendManager;
import me.desht.dhutils.Debugger;
import me.desht.dhutils.text.LogUtils;

public class STBFriendManager implements FriendManager {

    private static final String FRIEND_DIR = "friends";
    private static final FilenameFilter ymlFilter = (dir, name) -> name.endsWith(".yml");

    private final File saveDir;
    private final Map<UUID, Set<UUID>> friendMap = new HashMap<>();
    private final Set<UUID> savingQueue = new HashSet<>();

    public STBFriendManager(@Nonnull SensibleToolboxPlugin plugin) {
        saveDir = new File(plugin.getDataFolder(), FRIEND_DIR);

        if (!saveDir.exists() && !saveDir.mkdir()) {
            LogUtils.warning("can't create directory: " + saveDir);
        }

        load();
    }

    @Override
    public void addFriend(UUID id1, UUID id2) {
        getFriends(id1).add(id2);
        savingQueue.add(id1);
        Debugger.getInstance().debug("add friend: " + id1 + " -> " + id2);
    }

    @Override
    public void removeFriend(UUID id1, UUID id2) {
        getFriends(id1).remove(id2);
        savingQueue.add(id1);
        Debugger.getInstance().debug("remove friend: " + id1 + " -> " + id2);
    }

    @Override
    public boolean isFriend(@Nonnull UUID id1, @Nonnull UUID id2) {
        return getFriends(id1).contains(id2);
    }

    @Override
    @Nonnull
    public Set<UUID> getFriends(@Nonnull UUID id) {
        Validate.notNull(id, "Cannot get friends for null!");

        return friendMap.computeIfAbsent(id, key -> new HashSet<>());
    }

    @Override
    public void load() {
        for (File f : saveDir.listFiles(ymlFilter)) {
            try {
                YamlConfiguration conf = new YamlConfiguration();
                conf.load(f);
                String name = removeExtension(f.getName());
                UUID id1 = UUID.fromString(name);

                for (String k : conf.getStringList("friends")) {
                    UUID id2 = UUID.fromString(k);
                    addFriend(id1, id2);
                }
            } catch (Exception e) {
                LogUtils.warning("failed to load friend data for " + f + ": " + e.getMessage());
            }
        }
    }

    @Override
    public void save() {
        for (UUID id : savingQueue) {
            YamlConfiguration conf = new YamlConfiguration();
            Set<UUID> idSet = getFriends(id);
            List<String> ids = new ArrayList<>(idSet.size());

            for (UUID uuid : idSet) {
                ids.add(uuid.toString());
            }

            conf.set("friends", ids);
            File f = new File(saveDir, id.toString() + ".yml");

            try {
                conf.save(f);
            } catch (IOException e) {
                LogUtils.warning("failed to save friend data for " + f + ": " + e.getMessage());
            }
        }

        savingQueue.clear();
    }

    @Nonnull
    private String removeExtension(@Nonnull String name) {
        String separator = File.separator;
        String fileName;

        // Remove the path upto the filename.
        int lastSeparatorIndex = name.lastIndexOf(separator);

        if (lastSeparatorIndex == -1) {
            fileName = name;
        } else {
            fileName = name.substring(lastSeparatorIndex + 1);
        }

        // Remove the extension.
        int extensionIndex = fileName.lastIndexOf(".");

        if (extensionIndex == -1) {
            return fileName;
        } else {
            return fileName.substring(0, extensionIndex);
        }
    }
}
