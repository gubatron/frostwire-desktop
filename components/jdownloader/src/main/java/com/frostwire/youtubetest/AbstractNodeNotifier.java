package com.frostwire.youtubetest;

public interface AbstractNodeNotifier<E extends AbstractNode> {

    void nodeUpdated(E source);
}
