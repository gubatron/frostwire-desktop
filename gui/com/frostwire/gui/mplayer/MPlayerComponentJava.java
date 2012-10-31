package com.frostwire.gui.mplayer;

import java.awt.Canvas;
import java.awt.Component;

public class MPlayerComponentJava extends Canvas implements MPlayerComponent {

	private static final long serialVersionUID = -5860833860676831251L;
	
	public MPlayerComponentJava() {
		
    }
	
	@Override
	public Component getComponent() {
		return this;
	}

	@SuppressWarnings("deprecation")
	@Override
	public long getWindowID() {
		long hWnd = 0;
		
		try {
			Class<?> cl = Class.forName("sun.awt.windows.WComponentPeer");
	        java.lang.reflect.Field f = cl.getDeclaredField("hwnd");
	        f.setAccessible(true); //little reflection hack to access the hwnd from windows.
	        hWnd = f.getLong(getPeer());
    	
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return hWnd;
	}

	@Override
	public boolean toggleFullScreen() {
		return false;
	}
}
