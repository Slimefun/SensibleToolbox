package me.mrCookieSlime.CSCoreLib.general;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Clock {
	
	public static Date getCurrentDate() {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd-HH-mm");
		Date date = new Date();
		try {
			return format.parse(format.format(date));
		} catch (ParseException e) {
			return null;
		}
	}
	
	public static String getFormattedTime() {
		return new SimpleDateFormat("yyyy-MM-dd-HH-mm").format(getCurrentDate());
	}
	
	public static String format(Date date) {
		return new SimpleDateFormat("yyyy-MM-dd-HH-mm").format(date);
	}
	
	public static Date getFutureDate(int days, int hours, int minutes) {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd-HH-mm");
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(getCurrentDate());
		calendar.add(Calendar.DATE, days);
		calendar.add(Calendar.HOUR, hours);
		calendar.add(Calendar.MINUTE, minutes);
		try {
			return format.parse(format.format(calendar.getTime()));
		} catch (ParseException e) {
			return null;
		}
	}

}
