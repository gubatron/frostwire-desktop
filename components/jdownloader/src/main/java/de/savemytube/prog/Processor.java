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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.JOptionPane;

import de.http.ProxyDefintion;
import de.http.WGet;
import de.log.Category;
import de.savemytube.flv.FLV;
import de.savemytube.prog.util.*;
import de.savemytube.prog.util.abstracts.AbstractMovieParams;


public class Processor {
    
    static Category log = Category.getInstance(Processor.class); 
    
    public boolean process(String url,boolean audio,boolean video,String toFolder,ProxyDefintion proxyDef) throws Exception {
        
        WGet wget = new WGet();                      
        try {
            // URL
            //http://de.youtube.com/watch?v=p28bWameXNM
            URL u = new URL(url);
            byte[] b = null;
            if (proxyDef != null && proxyDef.getProxy() != null && proxyDef.getProxy().length() > 0) {
               b = wget.getResourceProxy(u,proxyDef);
            }
            else {
                b = wget.getResource(u); 
            }
            String result = new String(b);
            
            // what media?
            AbstractMovieParams mp = null;
            if (url.toLowerCase().indexOf("youtube") > -1) {
                mp = new YouTubeMovieParams(result,u.getHost());
            }
            else {
                
            }
                
            // Extract Parameter                     
            mp.parse();
            log.debug(mp.getFLVUrl());
            log.debug(mp.getFilename());
            log.info("Downloading " + mp.getFilename());
            
            // Save Flv
            URL urlFlv = new URL(mp.getFLVUrl());
            b =  wget.getResource(urlFlv);      
            wget.saveToFile(toFolder + "/"+mp.getCleanFilename(), b);
            log.info("saving to folder " + toFolder);
            log.info("Download ready");            
            
            processFLV(toFolder + "/" + mp.getCleanFilename(), audio, video);
            
        } catch (MalformedURLException e) {                 
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw e;
            //return false;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();            
            throw e;
            //return false;
        }
        catch(Exception e) {
            e.printStackTrace();            
            throw e;
            //return false;
        }
        return true;
    }
    
    public void processFLV(String filename,boolean audio,boolean video) {
        FLV flv = new FLV(filename,audio,video);
    }
    
}
