package me.mrCookieSlime.CSCoreLib.general.Math;

import org.bukkit.Location;

public class Calculator {
	
	public static int formToLine(int i) {
		int lines = 1;
		
		if (i > 9) {
			lines++;
		}
		if (i > 9*2) {
			lines++;
		}
		if (i > 9*3) {
			lines++;
		}
		if (i > 9*4) {
			lines++;
		}
		if (i > 9*5) {
			lines++;
		}
		if (i > 9*6) {
			lines++;
		}
		
		return lines;
	}
	
	public static Location centerPosition(Location l ) {
		
		double x = l.getX();
		double z = l.getZ();
		
		String[] rawX = String.valueOf(x).split(".");
		String[] rawZ = String.valueOf(z).split(".");
		
		String newX = rawX[0] + ".5";
		String newZ = rawZ[0] + ".5";
		
		l.setX(Double.parseDouble(newX));
		l.setZ(Double.parseDouble(newZ));
		
		return l;
	}

}
