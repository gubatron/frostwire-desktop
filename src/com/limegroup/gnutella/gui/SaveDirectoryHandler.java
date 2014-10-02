/*
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.limegroup.gnutella.gui;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Random;

import org.limewire.util.CommonUtils;
import org.limewire.util.FileUtils;
import org.limewire.util.OSUtils;

import com.limegroup.gnutella.settings.QuestionsHandler;

/**
 * This class handles prompting the user to enter a valid save directory.
 */
public final class SaveDirectoryHandler {   

    public enum ERROR_CODE {INVALID, NOT_HOME};
    /**
     * Ensure that this class cannot be constructed from outside this class.
     */
    private SaveDirectoryHandler() {}

    /**
     * Utility method for checking whether or not the save directory is valid.
     * 
     * @param saveDir the save directory to check for validity
     * @return <tt>true</tt> if the save directory is valid, otherwise 
     *  <tt>false</tt>
     */
    public static boolean isSaveDirectoryValid(File saveDir) {
        if(saveDir == null || !saveDir.exists() || !saveDir.isDirectory())
            return false;

        FileUtils.setWriteable(saveDir);
        
        Random generator = new Random();
        File testFile = null;
        for(int i = 0; i < 10 && testFile == null; i++) {
            StringBuilder name = new StringBuilder();
            for(int j = 0; j < 8; j++) {
                name.append((char)('a' + generator.nextInt('z'-'a')));
            }
            name.append(".tmp");
            
            testFile = new File(saveDir, name.toString());
            if (testFile.exists()) {
                testFile = null; // try again!
            }
        }
        
        if (testFile == null) {
            return false;
        }
        
        RandomAccessFile testRAFile = null;
        try {
            testRAFile = new RandomAccessFile(testFile, "rw");
         
            // Try to write something just to make extra sure we're OK.
            testRAFile.write(7);
            testRAFile.close();
        } catch (FileNotFoundException e) {
            // If we could not open the file, then we can't write to that 
            // directory.
            return false;
        } catch(IOException e) {
            // The directory is invalid if there was an error writing to it.
            return false;
        } finally {
            // Delete our test file.
            testFile.delete();
            try {
                if(testRAFile != null)
                    testRAFile.close();
            } catch (IOException ignored) {}
        }
        
        return saveDir.canWrite();
    }

    /**
     * Makes sure that the user has a valid save directory.
     */
    public static void handleSaveDirectory() {    
//        File saveDir = SharingSettings.getSaveDirectory();
//        if(!isSaveDirectoryValid(saveDir) || !showVistaWarningIfNeeded(saveDir))
//            showSaveDirectoryWindow();
    }
    
    public static boolean isGoodVistaDirectory(File f) {
        if (!OSUtils.isWindowsVista())
            return true;
        try {
            return FileUtils.isReallyInParentPath(CommonUtils.getUserHomeDir(), f);
        } catch (IOException iox) {
            return true; // probably bad, but not vista-specific
        }
    }
    
    /**
     * @param f the directory the user wants to save to
     * @return true if its ok to use that directory
     */
    public static boolean showVistaWarningIfNeeded(File f) {
        if (isGoodVistaDirectory(f))
            return true;
        return GUIMediator.showYesNoMessage(
                I18n.tr("Saving downloads to {0} may not function correctly.  To be sure downloads are saved properly you should save them to a sub-folder of {1}.  Would you like to choose another location?",f, CommonUtils.getUserHomeDir()), 
                QuestionsHandler.VISTA_SAVE_LOCATION, DialogOption.YES
                ) == DialogOption.NO;
    }
}

