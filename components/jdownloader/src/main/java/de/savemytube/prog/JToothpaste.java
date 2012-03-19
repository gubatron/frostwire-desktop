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

package de.savemytube.prog;

import de.log.Category;
import de.savemytube.prog.gui.FrmMain;

public class JToothpaste {

    static Category log = Category.getInstance(JToothpaste.class); 
    /**
     * @param args
     */        
    public static void main(String[] args) {
        
        // Console or Gui?
        if (args.length > 0) {        
            //String url = "http://de.youtube.com/watch?v=eaVoUOBsECY";
            String url = args[0];
            Processor proc = new Processor();
            try {
                proc.process(url, true, true,"./",null);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            log.info(url);
        }  
        else {
            FrmMain frm = new FrmMain();
            frm.show();
        }
        

    }

}
