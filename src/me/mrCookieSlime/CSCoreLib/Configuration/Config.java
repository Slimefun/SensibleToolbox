package me.mrCookieSlime.CSCoreLib.Configuration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

public class Config {
	
	File file;
	FileConfiguration config;
	
	public Config(File configFile) {
		this.file = configFile;
		this.config = YamlConfiguration.loadConfiguration(this.file);
	}
	
	public File getFile() {
		return this.file;
	}
	
	public FileConfiguration getConfiguration() {
		return this.config;
	}
	
	public void setValue(String path, Object value) {
		config.set(path, value);
		try {
			config.save(file);
		} catch (IOException e) {
		}
	}
	
	public boolean setDefaultValue(String path, Object value) {
		if (!contains(path)) {
			config.set(path, value);
			try {
				config.save(file);
				return true;
			} catch (IOException e) {
				return false;
			}
		}
		else {
			return false;
		}
	}
	
	public boolean contains(String path) {
		return config.contains(path);
	}
	
	public Object getValue(String path) {
		return config.get(path);
	}
	
	public ItemStack getItem(String path) {
		return config.getItemStack(path);
	}
	
	public String getRandomStringfromList(String path) {
		return getStringList(path).get(new Random().nextInt(getStringList(path).size()));
	}
	
	public int getRandomIntfromList(String path) {
		return getIntList(path).get(new Random().nextInt(getIntList(path).size()));
	}
	
	public String getString(String path) {
		return config.getString(path);
	}
	
	public int getInt(String path) {
		return config.getInt(path);
	}
	
	public boolean getBoolean(String path) {
		return config.getBoolean(path);
	}
	
	public List<String> getStringList(String path) {
		return config.getStringList(path);
	}
	
	public List<Integer> getIntList(String path) {
		List<Integer> ints = new ArrayList<Integer>();
		for (String string: config.getStringList(path)) {
			ints.add(Integer.parseInt(string));
		}
		return ints;
	}
	
	public void createFile() {
		try {
			this.file.createNewFile();
		} catch (IOException e) {
		}
	}
	
	public Location getLocation(String path) {
		return new Location(
			Bukkit.getWorld(
			config.getString(path + ".world")),
			config.getDouble(path + ".x"),
			config.getDouble(path + ".y"),
			config.getDouble(path + ".z")
		);
	}

}
