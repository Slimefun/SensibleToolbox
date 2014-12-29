package me.mrCookieSlime.CSCoreLib;

import me.mrCookieSlime.CSCoreLib.events.Listeners.GuideListener;
import me.mrCookieSlime.CSCoreLib.events.Listeners.ItemUseListener;
import me.mrCookieSlime.CSCoreLib.events.Listeners.MapListener;
import me.mrCookieSlime.CSCoreLib.events.Listeners.MenuClickListener;

import org.bukkit.plugin.Plugin;

public class CSCoreLib {
	
	/**
	 * This is a lightweight Library for your Plugin.
	 * You can use it to learn to develop, experiment with Java
	 * or just to save Time coding things again and again.
	 * 
	 * To use it, just download the jar -> open Eclipse -> open your Project
	 * -> unzip the downloaded .jar-file -> drag and drop the me-folder into your src-folder
	 * Now you're ready to start messing arround with this Library.
	 * Also checkout my GitHub page for new Updates..
	 * 
	 * https://github.com/mrCookieSlime/CS-CoreLib/releases
	 * 
	 * @author mrCookieSlime
	 * @license All Rights Reserved
	 * @version 1.0.2
	 * 
	 */
	
	public static boolean registered = false;
	
	public static boolean isRegistered() {
		return registered;
	}
	
	public static void registerListeners(Plugin plugin) {
		new MenuClickListener(plugin);
		new MapListener(plugin);
		new GuideListener(plugin);
		new ItemUseListener(plugin);
		registered = true;
	}

}
