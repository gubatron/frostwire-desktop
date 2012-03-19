/**
 * JToothpaste - Copyright (C) 2007 Matthias
 * Schuhmann
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

package de.log;

public class Category {
    
    private static Category singleton = null;
    private Category(String s) {        
    }
    
    public static Category getInstance(Class x) {
        return getInstance(x.getName());
    }
    
    public static Category getInstance(String s) {
        if (singleton == null) {
            singleton = new Category(s);
        }   
        return singleton;
    }
    
    public void debug(String s) {
        System.out.println(s);
    }
    
    public void error(String s,Object o) {
        System.err.println(s);
    }
    
    public void error(String s) {
        System.err.println(s);
    }
    
    public void warn(String s) {
        System.out.println(s);
    }
    public void info(String s) {
        System.out.println(">Info: " + s);
    }
}
