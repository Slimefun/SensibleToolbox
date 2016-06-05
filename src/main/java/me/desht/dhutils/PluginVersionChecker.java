package me.desht.dhutils;

import org.bukkit.plugin.Plugin;

public class PluginVersionChecker {
	public PluginVersionChecker(Plugin plugin, PluginVersionListener listener) {

		String currentVersion = plugin.getDescription().getVersion();
		String lastVersion = listener.getPreviousVersion();

		if (currentVersion != null && !lastVersion.equals(currentVersion)) {
			listener.onVersionChanged(getRelease(lastVersion), getRelease(currentVersion));
			listener.setPreviousVersion(currentVersion);
		}
	}

	/**
	 * Get the internal version number for the given string version, which is
	 * <major> * 1,000,000 + <minor> * 1,000 + <release>.  This assumes minor and
	 * release each won't go above 999, hopefully a safe assumption!
	 *
	 * @param ver the version string as returned by {@link org.bukkit.plugin.PluginDescriptionFile#getVersion()}
	 * @return the integer version number
	 */
	public static int getRelease(String ver) {
		String[] a = ver.replaceAll("-.+$", "").split("\\.");
		try {
			int major = Integer.parseInt(a[0]);
			int minor;
			int rel;
			if (a.length < 2) {
				minor = 0;
			} else {
				minor = Integer.parseInt(a[1]);
			}
			if (a.length < 3) {
				rel = 0;
			} else {
				rel = Integer.parseInt(a[2]);
			}
			return major * 1000000 + minor * 1000 + rel;
		} catch (NumberFormatException e) {
			LogUtils.warning("Version string [" + ver + "] doesn't look right!");
			return 0;
		}
	}
}
