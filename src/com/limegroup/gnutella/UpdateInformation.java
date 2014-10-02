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

package com.limegroup.gnutella;

/**
 * Simple interface for retrieving the most recent update info.
 */
public interface UpdateInformation extends DownloadInformation {
    
    public static int STYLE_BETA = 0;
    public static int STYLE_MINOR = 1;
    public static int STYLE_MAJOR = 2;
    public static int STYLE_CRITICAL = 3;
    public static int STYLE_FORCE = 4;
    
    public String getUpdateURL();
    
    public String getUpdateText();
    
    public String getUpdateTitle();
    
    public String getUpdateVersion();
    
    public String getButton1Text();
    
    public String getButton2Text();
    
    public int getUpdateStyle();
}
