/*
 * Created by Angel Leon (@gubatron), Alden Torres (aldenml)
 * Copyright (c) 2011, 2012, FrostWire(R). All rights reserved.
 *
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

package com.frostwire.gui.library;

/**
 * 
 * @author gubatron
 * @author aldenml
 *
 */
public class FileShareCell implements Comparable<FileShareCell> {

    private final LibraryFilesTableDataLine dataLine;
    private final String path;
    private final boolean shared;

    public FileShareCell(LibraryFilesTableDataLine libraryFilesTableDataLine, String path, boolean shared) {
        this.dataLine = libraryFilesTableDataLine;
        this.path = path;
        this.shared = shared;
    }

    public LibraryFilesTableDataLine getDataLine() {
        return dataLine;
    }
    
    public String getPath() {
        return path;
    }
    
    public boolean isShared() {
        return shared;
    }

    @Override
    public int compareTo(FileShareCell other) {
        return Boolean.valueOf(isShared()).compareTo(other.isShared());
    }   
}