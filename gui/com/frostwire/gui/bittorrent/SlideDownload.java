package com.frostwire.gui.bittorrent;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.limewire.util.IOUtils;

import com.frostwire.gui.components.Slide;

public class SlideDownload extends HttpDownload {

    private final Slide slide;
    
    public SlideDownload(Slide slide) {
        super(slide.httpDownloadURL, slide.title, slide.saveFileAs, slide.size, slide.md5, true, true);
        this.slide = slide;
    }
    

    @Override
    protected void onComplete() {
        if (slide.execute) {
            if (verifySignature(getSaveLocation())) {
                executeSlide(slide);
            }
        }
    }

    private void executeSlide(Slide slide) {
        List<String> command = new ArrayList<String>();
        command.add(getSaveLocation().getAbsolutePath());
        
        if (slide.executeParameters != null) {
            command.addAll(Arrays.asList(slide.executeParameters.split(" ")));
        }
        
        BufferedReader br = null;
        ProcessBuilder pb = new ProcessBuilder(command);
        try {
            Process p = pb.start();
            
            //consume all output to avoid deadlock in some verisons of windows
            br = new BufferedReader(new InputStreamReader(p.getInputStream()));

            String line = null;
            while ((line = br.readLine()) != null) {
            }
            
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            IOUtils.closeQuietly(br);
        }
    }

    private boolean verifySignature(File saveLocation) {
        return true;
    }
}