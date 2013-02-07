package com.frostwire.gui.bittorrent;

import com.frostwire.gui.components.Slide;

public class SlideDownload extends HttpDownload {

    private final Slide slide;
    
    public SlideDownload(Slide slide) {
        super(slide.httpDownloadURL, slide.title, slide.saveFileAs, slide.size, slide.md5, true, true);
        this.slide = slide;
    }
    

    @Override
    protected void onComplete() {
        
    }
}
