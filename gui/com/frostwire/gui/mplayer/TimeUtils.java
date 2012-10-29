package com.frostwire.gui.mplayer;

public class TimeUtils {

	public static int getHours( int rawSeconds ) {
		return rawSeconds / 3600;
	}
	
	public static int getMinutes(int rawSeconds) {
		return (rawSeconds - (TimeUtils.getHours(rawSeconds)*3600)) / 60;
	}
	
	public static int getSeconds(int rawSeconds) {
		
		return rawSeconds - (TimeUtils.getHours(rawSeconds)*3600)
						  - (TimeUtils.getMinutes(rawSeconds)*60);
	}
	
	public static String getTimeFormatedString(int rawSeconds) {
		
		String time;
		
		int hours = getHours(rawSeconds);
		int minutes = getMinutes(rawSeconds);
		int seconds = getSeconds(rawSeconds);
		
		if (hours > 0)
			time = String.format("%02d:%02d:%02d", hours, minutes, seconds);
		else
			time = String.format("%02d:%02d", minutes, seconds);
		
		return time;
		
	}
}
