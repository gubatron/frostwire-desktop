package com.frostwire.gnutella.gui.android;

import java.util.Date;

public final class Log {

	private static String getPrefix(String TAG) {
		return new Date() + " " + TAG + ": ";
	}
	
	public static void d(String TAG, String msg) {
		System.out.println("[ERRR] " + getPrefix(TAG) + msg);
	}
	
	public static void w(String TAG, String msg) {
		System.out.println("[WARN] " + getPrefix(TAG) + msg);
	}
	
	public static void e(String TAG, String msg) {
		System.out.println("[ERRR] " + getPrefix(TAG) + msg);
	}
	
	public static void e(String TAG, String msg, Throwable t) {
		System.out.println("[ERRR] " + getPrefix(TAG) + msg);
		t.printStackTrace();
		System.out.println("");
	}
	
	public static void w(String TAG, Throwable t) {
		System.out.println("[WARN] " + getPrefix(TAG) + t.getMessage());
		t.printStackTrace();
		System.out.println("");
	}
	
	public static void w(String TAG, String msg, Throwable t) {
		System.out.println("[WARN] " + getPrefix(TAG) + msg);
		t.printStackTrace();
		System.out.println("");
	}

	public static void v(String TAG, String msg) {
		System.out.println("[VERB] " + getPrefix(TAG) + msg);
	}
	
	public static void i(String TAG, String msg) {
		System.out.println("[INFO] " + getPrefix(TAG) + msg);
	}
}
