package com.frostwire.gui.bittorrent;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.limewire.util.IOUtils;
import org.limewire.util.SystemUtils;

import com.frostwire.gui.components.Slide;
import com.frostwire.util.DigestUtils;
import com.frostwire.util.HttpClient;
import com.frostwire.util.HttpClientFactory;
import com.frostwire.util.HttpClientType;

public class SlideDownload extends HttpDownload {

	private static final Log LOG = LogFactory.getLog(SlideDownload.class);
    private final Slide slide;
    
    public SlideDownload(Slide slide) {
        super(slide.httpDownloadURL, slide.title, slide.saveFileAs, slide.size, slide.md5, true, true);
        this.slide = slide;
    }
    

    @Override
    protected void onComplete() {
        if (slide.execute) {
            if (verifySignature(getSaveLocation(), slide.httpDownloadURL)) {
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
    
    

    private boolean verifySignature(File saveLocation, String executableDownloadURL) {
    	String certificateURL = getCertificateURL(executableDownloadURL);
    	HttpClient httpClient = HttpClientFactory.newInstance(HttpClientType.PureJava);
    	
    	try {
    		String certificateInBase64 = httpClient.get(certificateURL);   		
    		return SystemUtils.verifyExecutableSignature(saveLocation.getAbsolutePath(), certificateInBase64.getBytes());
    	} catch (Exception e) {
    		LOG.error("Could not verify executable signature:\n" + e.getMessage(), e);
    		return false;
    	}
    }
    
    private String getCertificateURL(String url) {
        String urlMD5 = DigestUtils.getMD5(url);
        if (urlMD5 != null) {
        	//return "http://certs.frostwire.com/"+urlMD5;
        	return "http://s3.amazonaws.com/certs.frostwire.com/"+urlMD5;
        } else {
        	return null;
        }
    }
}