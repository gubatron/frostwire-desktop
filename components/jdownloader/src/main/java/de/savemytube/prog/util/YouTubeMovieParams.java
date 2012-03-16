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

package de.savemytube.prog.util;


import de.log.Category;
import de.savemytube.prog.util.abstracts.AbstractMovieParams;

public class YouTubeMovieParams extends AbstractMovieParams{
    
    private String video_id;
    private String t;

    private static final String VIDEO_ID = "video_id";
    private static final String T = "\"t\"";
    private static final String HOST = "BASE_YT_URL";
    private static final String PLAYER = "get_video";
    
        
    public YouTubeMovieParams(String html,String host) {
        super(html,host);                
    }
    public void parse() {        
        String[] lines = super.getHtml().split("\n");
        for (int i = 0; i < lines.length; i++) {           
            String line = lines[i];    
            
//          var swfArgs = {"BASE_YT_URL": "http://de.youtube.com/", "video_id": "eaVoUOBsECY", "l": 193, "sk": "89k-BX1px09CNK6V8mVA-QC", "t": "OEgsToPDskJ4fO1EmkWuTeCbyd8ZcI0x", "hl": "de", "plid": "AARFBOKIe9-uiu4NAAAAoAAACAA"};
            if (line.indexOf("swfArgs") > 0) {
                String[] lineSub = line.split(",|:");
                log.debug(line);
                parseLine(lineSub);
            }  
            if (line.indexOf("<title>") > 0) {
                String filename = "";
                String[] lineSub = line.split(">");
                filename = lineSub[1];
                lineSub = filename.split("<");
                filename = lineSub[0];
                filename = clean(filename);
                filename = filename+".flv";
                super.setFilename(filename);
                String filenameClean = cleanString(filename);
                filenameClean = filename.replaceAll(" ","_");
                setCleanFilename(filenameClean);
                
            }
        }
        
        log.debug(video_id+t);    
                
    }
    
    public void parseLine(String[] parts) {
        for (int i = 0; i < parts.length; i++) {           
           String s = parts[i];
           if (s.indexOf(VIDEO_ID) > -1) {
               video_id = clean(parts[i+1]);
           }
           
           if (s.indexOf(T) > -1) {
               t = clean(parts[i+1]);
           }
           
           
                   
        }
    }
    
    public String clean(String s) {
        s = s.replaceAll("\"","");
        s = s.replaceAll("YouTube -","");
        s = s.replaceAll("YouTube","");
        s = s.trim();
        return s;
    }
    
    
    public String getFLVUrl() {
        return "http://"+getHost() + "/"+PLAYER+"?" + VIDEO_ID +"="+ video_id + "&" + "t="+ t;        
    }
    
    public String getT() {
        return t;
    }
    public void setT(String t) {
        this.t = t;
    }
    public String getVideo_id() {
        return video_id;
    }
    public void setVideo_id(String video_id) {
        this.video_id = video_id;
    }


}
