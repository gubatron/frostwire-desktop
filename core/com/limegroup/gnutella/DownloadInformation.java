package com.limegroup.gnutella;


public interface DownloadInformation {
    
    public URN getUpdateURN();
    public String getTTRoot();
    public String getUpdateCommand();
    public String getUpdateFileName();
    public long getSize();
    
}