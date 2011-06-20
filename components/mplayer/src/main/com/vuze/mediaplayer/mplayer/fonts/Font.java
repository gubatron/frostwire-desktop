package com.vuze.mediaplayer.mplayer.fonts;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class Font {
	
	private static Map<String,Font> loadedFonts;
	
	static {
		loadedFonts = new HashMap<String, Font>();
	}
	
	public static Font getFont(String name) {
		synchronized (loadedFonts) {
			Font f = loadedFonts.get(name);
			if(f != null) {
				return f;
			} else {
				f = new Font(name);
				if(f != null) {
					loadedFonts.put(name, f);
				}
				return f;
			}
		}
	}
	
	private String name;
	private File   fontFile;
	
	private Font(String name) {
		InputStream is = this.getClass().getResourceAsStream(name);
		if(is != null) {

			try {
				fontFile = File.createTempFile(name, ".ttf");
				fontFile.deleteOnExit();
				
				FileOutputStream fos = new FileOutputStream(fontFile);
				byte[] buffer = new byte[8192];
				int len = 0;
				while((len = is.read(buffer)) != -1) {
					fos.write(buffer, 0, len);
				}
				fos.close();
			} catch (Exception e) {
				fontFile = null;
			}			
		}
	}
	
	public String getFontPath() {
		if(fontFile != null) {
			return fontFile.getAbsolutePath();
		}
		return null;
	}

}
