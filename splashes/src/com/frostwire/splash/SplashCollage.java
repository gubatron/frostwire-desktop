package com.frostwire.splash;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

import javax.imageio.ImageIO;

public final class SplashCollage {
	
	private static int SPLASH_WIDTH = 0;
	private static int SPLASH_HEIGHT = 0;
	
	public static void usage(String error) {
		System.out.println("\n\nERROR: " + error);
		
		System.out.println("\n\ncreate_splash_collage -- Create a JPG with a collage of all the splashes for a version of FrostWire.");
		System.out.println("Usage:");
		System.out.println("\t./create_splash_collage <version>");
		
		System.exit(1);
	}
	
	public static final void main(String[] args) {
		File[] imagePaths = validateSplashFolderAndGetImagePaths(args);

		Dimension dimension = calculateCollageDimensions(imagePaths);
		
		String versionNumber = args[0].substring(args[0].lastIndexOf("/")+1);
		createSplashCollage(dimension, imagePaths, new File(args[0],"collage."+versionNumber+".jpg"));
	}

	private static void createSplashCollage(Dimension dimension,
			File[] imagePaths, File outputFilename) {
		
		BufferedImage bufferedImage = new BufferedImage(dimension.width, dimension.height,BufferedImage.TYPE_INT_RGB);
		Graphics2D graphics = bufferedImage.createGraphics();

		int row = 0;
		int col = 0;
		
		int x,y=0;
		
		for (File f : imagePaths) {
			if (col == 0) {
				x = 0;
			} else {
				x = SPLASH_WIDTH;
			}
			
			y = row * SPLASH_HEIGHT;
			
			if (col == 0) {
				col = 1;
			} else {
				col = 0;
				row++;
			}

			BufferedImage currentSplash = null;
			try {
				currentSplash = ImageIO.read(f);
			} catch (IOException e) {
				usage("could not create BufferedImage out of " + f.getAbsolutePath());
			}
			
			graphics.drawImage(currentSplash, x, y, null);
		}
		
		try {
			outputFilename.createNewFile();
			ImageIO.write(bufferedImage, "jpeg", outputFilename);
		} catch (IOException e) {
			usage("Could not write splash image at " + outputFilename);
		}
		
		System.out.println("Created new collage image " + outputFilename.getAbsolutePath());
		
		try {
			Runtime.getRuntime().exec("open " + outputFilename.getAbsolutePath());
		} catch (IOException e) {
		}
		
	}

	private static Dimension calculateCollageDimensions(File[] imagePaths) {
		
		Dimension result = new Dimension();
		
		int rows = (int) Math.ceil(imagePaths.length / 2.0);
		
		try {
			BufferedImage sampleSplash = ImageIO.read(imagePaths[0]);
			int sampleHeight = sampleSplash.getHeight();
			
			result.height = rows * sampleHeight;
			result.width = 2 * sampleSplash.getWidth();
			
			SPLASH_WIDTH = sampleSplash.getWidth();
			SPLASH_HEIGHT = sampleSplash.getHeight();
			
		} catch (IOException e) {
			return null;
		}
		
		return result;
	}

	private static File[] validateSplashFolderAndGetImagePaths(String[] args) {
		if (args.length < 1) {
			usage("Please enter the version number.");
		}
		
		File versionDir = new File(args[0]);
		if (!versionDir.isDirectory()) {
			usage("'"+args[0] + "' is not a directory, or could not be found. The version number should represent a folder with the splashes.");
		}
		
		File splashesDir = new File(versionDir,"com/frostwire/splash");
		
		if (!splashesDir.exists()) {
			usage("'"+args[0] + "' is not a valid splashes directory. It should have the folders com/frostwire/splash");
		}
		
		
		FilenameFilter splashFilenameFilter = new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				String nameWithoutExtension = name.substring(0,name.indexOf("."));
				
				try {
					Integer.valueOf(nameWithoutExtension);
				} catch (Exception e) {
					return false;
				}
				
				return name.endsWith("jpg") || name.endsWith("gif") || name.endsWith("png");
			}
		};
		
		String[] imageNames = splashesDir.list(splashFilenameFilter);
		
		if (imageNames.length == 0) {
			usage("'"+args[0] + "' is not a valid splashes directory. "+args[0]+"/com/frostwire/splash/ It does not contain any images.");
		}
		
		return splashesDir.listFiles(splashFilenameFilter);
	}
}