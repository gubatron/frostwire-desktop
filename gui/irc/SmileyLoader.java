package irc;

import java.awt.Image;
import java.awt.Toolkit;
import java.net.URL;

public class SmileyLoader implements ImageLoader {
	//so we load these guys only once, lazily
	private ClassLoader _classLoader = null;
	private Toolkit _toolkit = null;
	
	//Lazy load and cache instances of class loader and AWT Toolkit
	//for faster smiley loading.
	public SmileyLoader() {
		if (_classLoader == null)
			_classLoader = ClassLoader.getSystemClassLoader();
		if (_toolkit == null)
			_toolkit = Toolkit.getDefaultToolkit();
	}
	
	public Image getImage(String source) {
		String originalSource = source;
		//make sure they gave us a smiley we'll be able to find
		if (source == null || !source.startsWith("smileys/")) {
			return null; //SmileyTable will ignore nulls
		}
		
		//turn "smileys/happy.gif" into "irc/images/smileys/happy.gif"
		source = "irc/images/" + source; 
		
		URL imageUrl = null;
		
		//If you can't get a class loader from this class, use static version
		if (_classLoader ==  null) {
			imageUrl = ClassLoader.getSystemResource(source);
		} else {
			imageUrl = _classLoader.getResource(source);
		}
		
		if (imageUrl == null) {
			
			if (_classLoader != null) {
				imageUrl = _classLoader.getSystemResource(source);
			}
			
			if (imageUrl == null) {
				System.out.println("MISSING SMILEY: " + originalSource);
				return null;
			}
		}

		//create the Image form the URL
		return _toolkit.createImage(imageUrl);
	}		  
  }