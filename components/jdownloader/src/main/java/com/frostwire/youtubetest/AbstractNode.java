package com.frostwire.youtubetest;

public interface AbstractNode {

    String getName();

    boolean isEnabled();

    void setEnabled(boolean b);

    long getCreated();

    long getFinishedDate();

}
