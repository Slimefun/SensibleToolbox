package me.mrCookieSlime.CSCoreLib.Guide;

import java.util.ArrayList;
import java.util.List;

public class Guides {
	
	public static List<PluginGuide> guides = new ArrayList<PluginGuide>();
	
	public static List<PluginGuide> list() {
		return guides;
	}
	
	public static PluginGuide get(int id) {
		return guides.get(id);
	}
	
	public static boolean set(int index, PluginGuide guide) {
		if ((guides.size() - 1) >= index) {
			if (guides.get(index) != null) {
				guides.set(index, guide);
				return true;
			}
			else {
				return false;
			}
		}
		else {
			for (int i = 0; i <= index; i++) {
				if (i == index) {
					guides.add(guide);
				}
				else if (i >= guides.size()) {
					guides.add(null);
				}
			}
			return true;
		}
	}
	
	public static void refresh(int id, PluginGuide guide) {
		guides.set(id, guide);
	}

}
