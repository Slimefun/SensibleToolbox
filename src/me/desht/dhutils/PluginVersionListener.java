package me.desht.dhutils;

public interface PluginVersionListener {
	public void onVersionChanged(int oldVersion, int newVersion);
	public String getPreviousVersion();
	public void setPreviousVersion(String currentVersion);
}
