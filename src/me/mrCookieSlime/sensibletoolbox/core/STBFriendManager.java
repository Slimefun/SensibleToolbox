package me.mrCookieSlime.sensibletoolbox.core;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import me.desht.sensibletoolbox.dhutils.Debugger;
import me.desht.sensibletoolbox.dhutils.LogUtils;
import me.mrCookieSlime.sensibletoolbox.SensibleToolboxPlugin;
import me.mrCookieSlime.sensibletoolbox.api.FriendManager;

import org.bukkit.configuration.file.YamlConfiguration;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class STBFriendManager implements FriendManager {
    private static final String FRIEND_DIR = "friends";

    private static final FilenameFilter ymlFilter = new FilenameFilter() {
        @Override
        public boolean accept(File dir, String name) {
            return name.endsWith(".yml");
        }
    };

    private final File saveDir;
    private final Set<UUID> saveNeeded = Sets.newHashSet();
    private final Map<UUID,Set<UUID>> friendMap = Maps.newHashMap();

    public STBFriendManager(SensibleToolboxPlugin plugin) {
        saveDir = new File(plugin.getDataFolder(), FRIEND_DIR);
        if (!saveDir.exists()) {
            if (!saveDir.mkdir()) {
                LogUtils.warning("can't create directory: " + saveDir);
            }
        }

        load();
    }

    @Override
    public void addFriend(UUID id1, UUID id2) {
        getFriends(id1).add(id2);
        saveNeeded.add(id1);
        Debugger.getInstance().debug("add friend: " + id1 + " -> " + id2);
    }

    @Override
    public void removeFriend(UUID id1, UUID id2) {
        getFriends(id1).remove(id2);
        saveNeeded.add(id1);
        Debugger.getInstance().debug("remove friend: " + id1 + " -> " + id2);
    }

    @Override
    public boolean isFriend(UUID id1, UUID id2) {
        return getFriends(id1).contains(id2);
    }

    @Override
    public Set<UUID> getFriends(UUID id) {
        Set<UUID> res = friendMap.get(id);
        if (res == null) {
            res = Sets.newHashSet();
            friendMap.put(id, res);
        }
        return res;
    }

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

    public void save() {
        for (UUID id : saveNeeded) {
            YamlConfiguration conf = new YamlConfiguration();
            Set<UUID> idSet = getFriends(id);
            List<String> ids = Lists.newArrayListWithCapacity(idSet.size());
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
        saveNeeded.clear();
    }

    public static String removeExtension(String s) {
        String separator = File.separator;
        String filename;

        // Remove the path upto the filename.
        int lastSeparatorIndex = s.lastIndexOf(separator);
        if (lastSeparatorIndex == -1) {
            filename = s;
        } else {
            filename = s.substring(lastSeparatorIndex + 1);
        }

        // Remove the extension.
        int extensionIndex = filename.lastIndexOf(".");
        if (extensionIndex == -1)
            return filename;

        return filename.substring(0, extensionIndex);
    }
}
