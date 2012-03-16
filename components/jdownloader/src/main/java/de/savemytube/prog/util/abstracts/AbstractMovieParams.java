package de.savemytube.prog.util.abstracts;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.log.Category;
import de.savemytube.prog.util.YouTubeMovieParams;

public abstract class AbstractMovieParams {

    private String html; 
    private String host;
    private String filename;
    private String cleanFilename;
     
   
   
    public Category log;
    
    public AbstractMovieParams(String html,String host) {
        this.html = html;
        this.host = host;       
        log = Category.getInstance(this.getClass());
        
    }
    
    public String cleanString(String s) {
        return s.replaceAll("[^a-zA-Z 0-9]","_");
    }
    
    public String match(String input,String pattern){           
        String ret = null;
        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(input);
        boolean matchfound=m.find();
                
        if (matchfound) {
            ret = m.group(1);
        }    
        
        return ret;
    }   
    public abstract void parse();
    
    public abstract void parseLine(String[] parts);
    
    public abstract String clean(String s);
    
    
    public abstract String getFLVUrl();

    public String getFilename() {
        return filename;
    }
    
    public String getCleanFilename() {
        return cleanFilename;
    }
    
    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getHost() {
        return host;
    }
    public void setHost(String host) {
        this.host = host;
    }

    public String getHtml() {
        return html;
    }

    public void setHtml(String html) {
        this.html = html;
    }

    public void setCleanFilename(String cleanFilename) {
        this.cleanFilename = cleanFilename;
    }


}
